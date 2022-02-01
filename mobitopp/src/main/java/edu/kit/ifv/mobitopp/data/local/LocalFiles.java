package edu.kit.ifv.mobitopp.data.local;

import static edu.kit.ifv.mobitopp.util.collections.StreamUtils.warn;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import edu.kit.ifv.mobitopp.data.CostMatrix;
import edu.kit.ifv.mobitopp.data.CreateFolder;
import edu.kit.ifv.mobitopp.data.DataRepositoryForPopulationSynthesis;
import edu.kit.ifv.mobitopp.data.DataRepositoryForSimulation;
import edu.kit.ifv.mobitopp.data.DataSource;
import edu.kit.ifv.mobitopp.data.DemandZoneRepository;
import edu.kit.ifv.mobitopp.data.DemographyRepository;
import edu.kit.ifv.mobitopp.data.InputSpecification;
import edu.kit.ifv.mobitopp.data.Network;
import edu.kit.ifv.mobitopp.data.PanelDataRepository;
import edu.kit.ifv.mobitopp.data.PersonLoader;
import edu.kit.ifv.mobitopp.data.StartDateSpecification;
import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.data.ZoneId;
import edu.kit.ifv.mobitopp.data.ZoneRepository;
import edu.kit.ifv.mobitopp.data.areatype.AreaTypeRepository;
import edu.kit.ifv.mobitopp.data.local.configuration.FileMatrixConfiguration;
import edu.kit.ifv.mobitopp.data.local.configuration.MatrixConfiguration;
import edu.kit.ifv.mobitopp.data.local.serialiser.ZoneRepositorySerialiser;
import edu.kit.ifv.mobitopp.dataimport.ChargingDataFactory;
import edu.kit.ifv.mobitopp.dataimport.DefaultPower;
import edu.kit.ifv.mobitopp.dataimport.DemographyBuilder;
import edu.kit.ifv.mobitopp.dataimport.StructuralData;
import edu.kit.ifv.mobitopp.network.SimpleRoadNetwork;
import edu.kit.ifv.mobitopp.populationsynthesis.DemandDataRepository;
import edu.kit.ifv.mobitopp.populationsynthesis.DemandRegionRepository;
import edu.kit.ifv.mobitopp.populationsynthesis.DemographyData;
import edu.kit.ifv.mobitopp.populationsynthesis.HouseholdForSetup;
import edu.kit.ifv.mobitopp.populationsynthesis.Population;
import edu.kit.ifv.mobitopp.populationsynthesis.SerialisingDemandRepository;
import edu.kit.ifv.mobitopp.populationsynthesis.region.DemandRegionRepositoryBuilder;
import edu.kit.ifv.mobitopp.populationsynthesis.serialiser.DemandDataDeserialiser;
import edu.kit.ifv.mobitopp.populationsynthesis.serialiser.DemandDataFolder;
import edu.kit.ifv.mobitopp.populationsynthesis.serialiser.PersonChanger;
import edu.kit.ifv.mobitopp.result.ResultWriter;
import edu.kit.ifv.mobitopp.simulation.CarSharingWriter;
import edu.kit.ifv.mobitopp.simulation.ChargingListener;
import edu.kit.ifv.mobitopp.simulation.ElectricChargingWriter;
import edu.kit.ifv.mobitopp.simulation.ImpedanceIfc;
import edu.kit.ifv.mobitopp.simulation.LocalPersonLoader;
import edu.kit.ifv.mobitopp.simulation.PublicTransportData;
import edu.kit.ifv.mobitopp.simulation.VehicleBehaviour;
import edu.kit.ifv.mobitopp.time.Time;
import edu.kit.ifv.mobitopp.visum.IdToOidMapper;
import edu.kit.ifv.mobitopp.visum.VisumNetwork;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalFiles implements DataSource {

    private static final String defaultZoneRepository = "output/zone-repository/";
    private static final boolean defaultMapIds = true;

    private File zoneRepositoryFolder;
    private File matrixConfigurationFile;
    private File demandDataFolder;
    private File zonePropertiesDataFile;
    private File attractivityDataFile;
    private File parkingFacilitiesDataFile;
    private File carSharingPropertiesDataFile;
    private File carSharingStationsDataFile;
    private File carSharingFreeFloatingDataFile;
    private File bikeSharingPropertiesDataFile;
    private ChargingType charging;
    private String defaultChargingPower;
    private boolean mapIds;


    public LocalFiles() {
        charging = ChargingType.unlimited;
        zoneRepositoryFolder = new File(defaultZoneRepository);
        mapIds = defaultMapIds;
    }

    /**
     * Load zone properties from data file, if available. Otherwise fall back to attractivities data file.
     */
    private File getZonePropertiesDataAsFile() {
        /* Fallback solution to return attractivitiesDataFile to ensure backward compatibility */
        if (zonePropertiesDataFile.exists()) {
            return zonePropertiesDataFile;
        }
        log.warn("referenced zonePropertiesDataFile does not exist - using attractivityDataFile instead!");
        return attractivityDataFile;
    }

    public String getZoneRepositoryFolder() {
        return Convert.asString(zoneRepositoryFolder);
    }

    public void setZoneRepositoryFolder(String zoneRepositoryFile) {
        this.zoneRepositoryFolder = Convert.asFile(zoneRepositoryFile);
    }

    public String getMatrixConfigurationFile() {
        return Convert.asString(matrixConfigurationFile);
    }

    public void setMatrixConfigurationFile(String matrixConfigurationFile) {
        this.matrixConfigurationFile = Convert.asFile(matrixConfigurationFile);
    }

    public String getDemandDataFolder() {
        return Convert.asString(demandDataFolder);
    }

    public void setDemandDataFolder(String demandDataFolder) {
        this.demandDataFolder = Convert.asFile(demandDataFolder);
    }

    public String getZonePropertiesDataFile() {
        return Convert.asString(zonePropertiesDataFile);
    }

    public void setZonePropertiesDataFile(String zonePropertiesDataFile) {
        this.zonePropertiesDataFile = Convert.asFile(zonePropertiesDataFile);
    }

    public String getAttractivityDataFile() {
        return Convert.asString(attractivityDataFile);
    }

    public void setAttractivityDataFile(String attractivityDataFile) {
        this.attractivityDataFile = Convert.asFile(attractivityDataFile);
    }

    public String getParkingFacilitiesDataFile() {
        return Convert.asString(parkingFacilitiesDataFile);
    }

    public void setParkingFacilitiesDataFile(String parkingFacilitiesDataFile) {
        this.parkingFacilitiesDataFile = Convert.asFile(parkingFacilitiesDataFile);
    }

    public String getCarSharingPropertiesDataFile() {
        return Convert.asString(carSharingPropertiesDataFile);
    }

    public void setCarSharingPropertiesDataFile(String carSharingPropertiesDataFile) {
        this.carSharingPropertiesDataFile = Convert.asFile(carSharingPropertiesDataFile);
    }

    public String getCarSharingStationsDataFile() {
        return Convert.asString(carSharingStationsDataFile);
    }

    public void setCarSharingStationsDataFile(String carSharingStationsDataFile) {
        this.carSharingStationsDataFile = Convert.asFile(carSharingStationsDataFile);
    }

    public String getCarSharingFreeFloatingDataFile() {
        return Convert.asString(carSharingFreeFloatingDataFile);
    }

    public void setCarSharingFreeFloatingDataFile(String carSharingFreeFloatingDataFile) {
        this.carSharingFreeFloatingDataFile = Convert.asFile(carSharingFreeFloatingDataFile);
    }

    public String getBikeSharingPropertiesDataFile() {
        return Convert.asString(bikeSharingPropertiesDataFile);
    }

    public void setBikeSharingPropertiesDataFile(String bikeSharingDataFile) {
        this.bikeSharingPropertiesDataFile = Convert.asFile(bikeSharingDataFile);
    }

    public ChargingType getCharging() {
        return charging;
    }

    public void setCharging(ChargingType charging) {
        this.charging = charging;
    }

    public String getDefaultChargingPower() {
        return defaultChargingPower;
    }

    public void setDefaultChargingPower(String defaultChargingPower) {
        this.defaultChargingPower = defaultChargingPower;
    }

    public boolean isMapIds() {
        return mapIds;
    }

    public void setMapIds(boolean mapIds) {
        this.mapIds = mapIds;
    }

    @Override
    public DataRepositoryForPopulationSynthesis forPopulationSynthesis(
            final VisumNetwork visumNetwork, final SimpleRoadNetwork roadNetwork,
            final DemographyData demographyData, final StructuralData zoneProperties,
            final PanelDataRepository panelDataRepository, final int numberOfZones,
            final StartDateSpecification input, final ResultWriter results,
            final AreaTypeRepository areaTypeRepository, final TypeMapping modeToType,
            final PersonChanger personChanger, final Function<ImpedanceIfc, ImpedanceIfc> wrapImpedance,
            final DemandRegionMapping demandRegionMapping)
            throws IOException {
        ChargingListener electricChargingWriter = new ElectricChargingWriter(results);
        Matrices matrices = matrices(modeToType);
        DemographyRepository demographyRepository = demographyRepository(demographyData);
        ZoneRepository zoneRepository = loadZonesFromVisum(visumNetwork, roadNetwork,
                areaTypeRepository, matrices);
        initialiseResultWriting(zoneRepository, results, electricChargingWriter);
        DemandZoneRepository demandZoneRepository = demandZoneRepository(zoneRepository, demographyData,
                numberOfZones, zoneProperties);
        ImpedanceIfc impedance = impedance(input, matrices, zoneRepository, wrapImpedance);
        DemandDataFolder demandData = demandDataFolder(zoneRepository, numberOfZones, personChanger);
        DemandDataRepository demandRepository = new SerialisingDemandRepository(
                demandData.serialiseAsCsv());
        DemandRegionRepository demandRegionRepository = demandRegionRepository(demandZoneRepository,
                demandRegionMapping, demographyRepository);
        return new LocalDataForPopulationSynthesis(matrices, demographyRepository,
                demandRegionRepository, demandZoneRepository, panelDataRepository, impedance,
                demandRepository, results);
    }

    private DemandRegionRepository demandRegionRepository(
            final DemandZoneRepository demandZoneRepository,
            final DemandRegionMapping demandRegionMapping,
            final DemographyRepository demographyRepository) {
        return new DemandRegionRepositoryBuilder(demandRegionMapping, demandZoneRepository,
                demographyRepository).create();
    }

    private DemandZoneRepository demandZoneRepository(
            final ZoneRepository zoneRepository, final DemographyData demographyData,
            final int numberOfZones, final StructuralData zoneProperties) {
        return LocalDemandZoneRepository
                .from(zoneRepository, demographyData, numberOfZones, zoneProperties);
    }

    private DemandDataFolder demandDataFolder(
            final ZoneRepository zoneRepository, final int numberOfZones,
            final PersonChanger personChanger)
            throws IOException {
        Map<ZoneId, Zone> mapping = new LocalZoneLoader(zoneRepository).mapZones(numberOfZones);
        ZoneRepository zonesToSimulate = new LocalZoneRepository(mapping);
        return DemandDataFolder.at(this.demandDataFolder, zoneRepository, zonesToSimulate, personChanger);
    }

    private Matrices matrices(TypeMapping modeToType) throws FileNotFoundException {
        MatrixConfiguration matrixConfiguration = loadMatrixConfiguration(modeToType);
        return new MatrixRepository(matrixConfiguration);
    }

    private MatrixConfiguration loadMatrixConfiguration(TypeMapping modeToType) throws FileNotFoundException {
        File configFile = matrixConfigurationFile;
        File matrixFolder = configFile.getParentFile();
        return FileMatrixConfiguration.from(configFile, matrixFolder, modeToType);
    }

    private DemographyRepository demographyRepository(DemographyData demographyData) {
        return new DemographyBuilder(demographyData);
    }

    private ZoneRepository loadZonesFromVisum(VisumNetwork visumNetwork,
                                              SimpleRoadNetwork roadNetwork, AreaTypeRepository areaTypeRepository, Matrices matrices)
            throws IOException {
        CostMatrix matrix = matrices.distanceMatrix(Time.start);
        Map<String, Integer> map = matrix
                .ids()
                .stream()
                .collect(toMap(ZoneId::getExternalId, ZoneId::getMatrixColumn));
        IdToOidMapper mapper = IdToOidMapper.createFrom(map);
        ZoneRepository fromVisum = LocalZoneRepository
                .from(visumNetwork, roadNetwork, charging, defaultPower(),
                        getZonePropertiesDataAsFile(), attractivityDataFile, parkingFacilitiesDataFile,
                        carSharingPropertiesDataFile, carSharingStationsDataFile,
                        carSharingFreeFloatingDataFile, bikeSharingPropertiesDataFile, areaTypeRepository,
                        mapper);
        ZoneRepositorySerialiser serialised = createSerialiser(areaTypeRepository);
        serialised.serialise(fromVisum);
        return fromVisum;
    }

    private void initialiseResultWriting(
            ZoneRepository zoneRepository, ResultWriter results,
            ChargingListener electricChargingWriter) {
        CarSharingWriter carSharingWriter = new CarSharingWriter(results);
        for (Zone zone : zoneRepository.getZones()) {
            zone.charging().register(electricChargingWriter);
            zone.carSharing().register(carSharingWriter);
        }
    }

    private DefaultPower defaultPower() {
        if (Convert.asFile(defaultChargingPower).exists()) {
            return new DefaultPower(defaultChargingPower);
        }
        log.warn("Default charging power file not specified using zero charging power.");
        return DefaultPower.zero;
    }

    private void addOpportunities(
            final ZoneRepository zoneRepository, final int numberOfZones,
            final PersonChanger personChanger) throws IOException {
        DemandDataFolder demandData = demandDataFolder(zoneRepository, numberOfZones, personChanger);
        try (DemandDataDeserialiser deserialiser = demandData.deserialiseFromCsv()) {
            deserialiser.addOpportunitiesTo(zoneRepository);
        } catch (Exception e) {
            throw warn(new IOException("Could not load demand data.", e), log);
        }
    }

    private ImpedanceIfc impedance(
            StartDateSpecification input, Matrices matrices, ZoneRepository zoneRepository,
            Function<ImpedanceIfc, ImpedanceIfc> wrapImpedance) {
        ImpedanceLocalData localImpedance = new ImpedanceLocalData(matrices, zoneRepository,
                input.startDate());
        return wrapImpedance.apply(localImpedance);
    }

    @Override
    public DataRepositoryForSimulation forSimulation(
            Supplier<Network> network, int numberOfZones, InputSpecification input,
            PublicTransportData data, ResultWriter results, ElectricChargingWriter electricChargingWriter,
            AreaTypeRepository areaTypeRepository, TypeMapping modeToType,
            Predicate<HouseholdForSetup> householdFilter, PersonChanger personChanger,
            Function<ImpedanceIfc, ImpedanceIfc> wrapImpedance) throws IOException {
        Matrices matrices = matrices(modeToType);
        ZoneRepository zoneRepository = loadZonesFromMobiTopp(network, areaTypeRepository, matrices);
        initialiseResultWriting(zoneRepository, results, electricChargingWriter);
        addOpportunities(zoneRepository, numberOfZones, personChanger);
        ImpedanceIfc localImpedance = impedance(input, matrices, zoneRepository, wrapImpedance);
        ImpedanceIfc impedance = data.impedance(localImpedance, zoneRepository);//impedance equals to localImpedance here
        VehicleBehaviour vehicleBehaviour = data.vehicleBehaviour(results);
        PersonLoader personLoader = personLoader(zoneRepository, numberOfZones, householdFilter,
                personChanger);
        triggerGarbageCollector();
        return new LocalDataForSimulation(matrices, zoneRepository, impedance, personLoader,
                vehicleBehaviour);
    }

    private void triggerGarbageCollector() {
        System.gc();
    }

    private ZoneRepository loadZonesFromMobiTopp(
            Supplier<Network> networkSupplier, AreaTypeRepository areaTypeRepository, Matrices matrices) throws IOException {
        ZoneRepositorySerialiser serialisedData = createSerialiser(areaTypeRepository);
        if (serialisedData.isAvailable()) {
            return serialisedData.load();
        }
        Network network = networkSupplier.get();
        return loadZonesFromVisum(network.visumNetwork, network.roadNetwork, areaTypeRepository, matrices);
    }

    private ZoneRepositorySerialiser createSerialiser(AreaTypeRepository areaTypeRepository) {
        ChargingDataFactory factory = createChargingFactory();
        return new ZoneRepositorySerialiser(zoneRepositoryFolder, factory, attractivityDataFile,
                bikeSharingPropertiesDataFile, areaTypeRepository);
    }

    private ChargingDataFactory createChargingFactory() {
        return charging.factory(defaultPower());
    }

    private PersonLoader personLoader(
            final ZoneRepository zoneRepository, final int numberOfZones,
            final Predicate<HouseholdForSetup> householdFilter, final PersonChanger personChanger) {
        try {
            return loadFromLocalFolder(zoneRepository, numberOfZones, householdFilter, personChanger);
        } catch (Exception e) {
            throw warn(new RuntimeException(e), log);
        }
    }

    private PersonLoader loadFromLocalFolder(
            final ZoneRepository zoneRepository, final int numberOfZones,
            final Predicate<HouseholdForSetup> householdFilter, final PersonChanger personChanger)
            throws Exception {
        DemandDataFolder demandDataFolder = demandDataFolder(zoneRepository, numberOfZones, personChanger);
        try (DemandDataDeserialiser deserialiser = demandDataFolder.deserialiseFromCsv()) {
            Population population = deserialiser.loadPopulation(householdFilter);
            return new LocalPersonLoader(population);
        }
    }

    @Override
    public void validate(TypeMapping modeToType) throws IOException {
        validateFiles();
        validateMatrices(modeToType);
    }

    private void validateFiles() throws IOException {
        validateDemandDataFolder();
        validateZoneRepositoryFolder();
        Validate.files(matrixConfigurationFile).doExist();
    }

    private void validateZoneRepositoryFolder() throws IOException {
        CreateFolder.at(zoneRepositoryFolder).ifMissing();
    }

    private void validateDemandDataFolder() throws IOException {
        CreateFolder.at(demandDataFolder).ifMissing();
    }

    private void validateMatrices(TypeMapping modeToType) {
        try {
            loadMatrixConfiguration(modeToType).validate();
        } catch (FileNotFoundException cause) {
            throw warn(new UncheckedIOException("Missing file check for matrix configuration.", cause), log);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + " [matrixConfigurationFile=" + matrixConfigurationFile
                + ", demandDataFolder=" + demandDataFolder + "zonePropertiesDataFile=" + getZonePropertiesDataAsFile()
                + ", attractivityDataFile=" + attractivityDataFile + ", parkingFacilitiesDataFile=" + parkingFacilitiesDataFile
                + ", carSharingstationsDataFile=" + carSharingStationsDataFile
                + ", charging=" + charging + ", defaultChargingPower=" + defaultChargingPower + "]";
    }

}
