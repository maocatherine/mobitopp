package edu.kit.ifv.mobitopp.populationsynthesis;

import static edu.kit.ifv.mobitopp.util.collections.StreamUtils.warn;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import edu.kit.ifv.mobitopp.data.DataRepositoryForPopulationSynthesis;
import edu.kit.ifv.mobitopp.data.DemandRegion;
import edu.kit.ifv.mobitopp.data.DemandZone;
import edu.kit.ifv.mobitopp.data.DemandZoneRepository;
import edu.kit.ifv.mobitopp.data.FixedDistributionMatrix;
import edu.kit.ifv.mobitopp.data.PopulationForSetup;
import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.populationsynthesis.calculator.DemandDataCalculator;
import edu.kit.ifv.mobitopp.populationsynthesis.opportunities.OpportunityLocationSelector;
import edu.kit.ifv.mobitopp.populationsynthesis.region.PopulationSynthesisStep;
import edu.kit.ifv.mobitopp.populationsynthesis.serialiser.SerialiseDemography;
import edu.kit.ifv.mobitopp.result.Results;
import edu.kit.ifv.mobitopp.simulation.ActivityType;
import edu.kit.ifv.mobitopp.simulation.ImpedanceIfc;
import edu.kit.ifv.mobitopp.simulation.opportunities.OpportunityDataForZone;
import edu.kit.ifv.mobitopp.util.StopWatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PopulationSynthesis {

	private final SynthesisContext context;
	private final DemandCategories categories;
	private StopWatch performanceLogger;

	public PopulationSynthesis(SynthesisContext context) {
		super();
		this.context = context;
		categories = new DemandCategories();
		performanceLogger = new StopWatch(LocalDateTime::now);
	}

	protected SynthesisContext context() {
		return context;
	}

	protected DataRepositoryForPopulationSynthesis dataRepository() {
		return context.dataRepository();
	}

	protected Results results() {
		return context.resultWriter();
	}

	protected DemandZoneRepository demandZoneRepository() {
		return context.zoneRepository();
	}

	protected long seed() {
		return context.seed();
	}

	protected ImpedanceIfc impedance() {
		return context.impedance();
	}

	protected List<DemandZone> getZones() {
		return demandZoneRepository().getZones();
	}

	public void createPopulation() {
		performanceLogger.start();
		//Create locations like work, education, and shops. Including the total number of them in the area, the coordinates and the attractivities. Results are
		//Read from poi file defined in configuration.
		createLocations();
		measureTime("Create locations");

		//Locations of WORK/EDUCATION activity type. These are fixed locations.
		Map<ActivityType, FixedDistributionMatrix> fdMatrices = fixedDistributionMatrices();
		measureTime("Load fixed distribution matrices");
		assertFixedDistributionMatrices(fdMatrices);
		measureTime("Assert fixed distribution matrices");

		//Not sure what this does.
		executeBeforeCreation();
		measureTime("Execute before creation");

		//Create population with consideration of fixed locations.
		doCreatePopulation(fdMatrices);
		measureTime("Create population");

		//Not sure what this does again.
		doExecuteAfterCreation();
		measureTime("Execute after creation");

		finishExecution();
		measureTime("Finish execution");

		printPerformance();
	}

	private void measureTime(String label) {
		performanceLogger.measurePoint(label);
	}

	private void printPerformance() {
		log.info("Runtimes while creating population:");
		performanceLogger.forEach((m, d) -> log.info(m + " " + d));
	}

	private void createLocations() {
		log.info("creating destinations...");
		OpportunityLocationSelector locationSelector = createOpportunityLocationSelector();
		createLocations(locationSelector);
		log.info("creating DONE.");
	}

	protected abstract OpportunityLocationSelector createOpportunityLocationSelector();

	private void createLocations(OpportunityLocationSelector opportunityLocationSelector) {
		List<Zone> zones = demandZoneRepository().zoneRepository().getZones();
		zones
				.stream()
				.filter(Zone::isDestination)
				.peek(zone -> log.debug("zone " + zone.getId() + " locations available? " + zone.opportunities().locationsAvailable())) // TODO: remove this line? 
				.forEach(zone -> createLocationsForZone(opportunityLocationSelector, zone));
	}

	private void createLocationsForZone(
			OpportunityLocationSelector opportunityLocationSelector, Zone zone) {
		//Opportunity includes activity type and their attractivity. Activity type is from attractivity heading.
		OpportunityDataForZone opportunities = zone.opportunities();
		opportunities.createLocations(opportunityLocationSelector);

		results().write(categories.demanddataOpportunities, opportunities.forLogging());
		
		log.debug("zone " + zone.getId() + " locations available now? " + zone.opportunities().locationsAvailable());		
	}

	protected void executeBeforeCreation() {
	}

	private void doExecuteAfterCreation() {
		serialiseDemography();
		executeAfterCreation();
	}

	private void serialiseDemography() {
		EnumSet.allOf(RegionalLevel.class).forEach(this::serialise);
	}

	private void serialise(RegionalLevel level) {
		DemandRegionRepository repository = context.dataRepository().demandRegionRepository();
		List<DemandRegion> zones = repository.getRegionsOf(RegionalLevel.zone);
		SerialiseDemography serialiser = new SerialiseDemography(context.attributes(level), () -> zones,
				context.resultWriter(), level);
		serialiser.serialiseDemography();
	}

	protected void executeAfterCreation() {
	}

	void doCreatePopulation(Map<ActivityType, FixedDistributionMatrix> fdMatrices) {
		//SingleZoneDemandCalculator is called. This method is defined in BasicPopulationSynthesisIpf.java
		DemandDataCalculator calculator = createCalculator(fdMatrices);
		calculator.calculateDemand();
	}

	/**
	 * Override this method to create your own demand calculator. Otherwise, the zone based one will
	 * be called.
	 * 
	 * @param commuterMatrices
	 *          commuter matrices per activity type
	 * @return calculator to be used for calculating the demand
	 */
	protected abstract DemandDataCalculator createCalculator(
			Map<ActivityType, FixedDistributionMatrix> commuterMatrices);

	private void finishExecution() {
		try {
			dataRepository().finishExecution();
		} catch (IOException cause) {
			throw warn(new UncheckedIOException(cause), log);
		}
	}

	protected PopulationSynthesisStep storeData() {
		return this::storeData;
	}

	private void storeData(final DemandRegion region) {
		region.zones().forEach(dataRepository().demandDataRepository()::store);
		System.gc();
	}

	protected PopulationSynthesisStep cleanData() {
		return this::cleanData;
	}

	private void cleanData(final DemandRegion region) {
		region.zones().map(DemandZone::getPopulation).forEach(PopulationForSetup::clear);
		System.gc();
	}

	private Map<ActivityType, FixedDistributionMatrix> fixedDistributionMatrices() {
		//Filter the WORK/EDUCATION activity type locations. Read the fixed matrices directly from input.
		log.info("Load matrices....");
		Map<ActivityType, FixedDistributionMatrix> fixedDistributionMatrices = dataRepository()
				.fixedDistributionMatrices();
		log.info("...loaded!\n");
		return fixedDistributionMatrices;
	}

	private void assertFixedDistributionMatrices(
			Map<ActivityType, FixedDistributionMatrix> inMatrices) {
		verify(ActivityType.WORK, inMatrices);
		verify(ActivityType.EDUCATION, inMatrices);
	}

	private void verify(
			ActivityType activityType, Map<ActivityType, FixedDistributionMatrix> matrices) {
		if (!matrices.containsKey(activityType)) {
			throw warn(new IllegalStateException("Fixed distribution matrix missing for " + activityType), log);
		}
	}
}