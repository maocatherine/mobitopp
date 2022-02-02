package edu.kit.ifv.mobitopp.simulation;

import edu.kit.ifv.mobitopp.simulation.activityschedule.LeisureWalkActivityPeriodFixer;
import edu.kit.ifv.mobitopp.simulation.activityschedule.randomizer.DefaultActivityDurationRandomizer;
import edu.kit.ifv.mobitopp.simulation.destinationChoice.*;
import edu.kit.ifv.mobitopp.simulation.destinationchoice.DestinationChoiceClayton;
import edu.kit.ifv.mobitopp.simulation.modeChoice.BasicModeAvailabilityModel;
import edu.kit.ifv.mobitopp.simulation.modeChoice.ModeAvailabilityModel;
import edu.kit.ifv.mobitopp.simulation.modeChoice.ModeAvailabilityModelAddingCarsharing;
import edu.kit.ifv.mobitopp.simulation.modeChoice.ModeChoiceModel;
import edu.kit.ifv.mobitopp.simulation.modeChoice.stuttgart.ModeChoiceStuttgart;
import edu.kit.ifv.mobitopp.simulation.modeChoice.stuttgart.ModeSelectorParameterFirstTrip;
import edu.kit.ifv.mobitopp.simulation.modeChoice.stuttgart.ModeSelectorParameterOtherTrip;
import edu.kit.ifv.mobitopp.simulation.person.DefaultTripFactory;
import edu.kit.ifv.mobitopp.simulation.person.PersonStateSimple;
import edu.kit.ifv.mobitopp.simulation.person.TripFactory;
import edu.kit.ifv.mobitopp.simulation.tour.TourBasedModeChoiceModelDummy;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class Mobitopp extends Simulation {

	public Mobitopp(SimulationContext context) {
		super(context);
	}

	@Override
	protected DemandSimulator simulator() {
		ModeAvailabilityModel modeAvailabilityModel = new BasicModeAvailabilityModel(impedance());
		DestinationChoiceModel destinationSelector = destinationChoiceModel(impedance(), modeAvailabilityModel);
		ModeChoiceModel modeSelector = modeSelector(modeAvailabilityModel);
		ZoneBasedRouteChoice routeChoice = new NoRouteChoice();
		ReschedulingStrategy rescheduling = new ReschedulingSkipTillHome(context().simulationDays());
		System.out.println("Initializing simulator...");
		TripFactory tripFactory = new DefaultTripFactory();
		return new DemandSimulatorPassenger(destinationSelector,
				new TourBasedModeChoiceModelDummy(modeSelector), routeChoice,
				new LeisureWalkActivityPeriodFixer(),
				new DefaultActivityDurationRandomizer(context().seed()), tripFactory, rescheduling,
				PersonStateSimple.UNINITIALIZED, context());
	}


	private ModeChoiceModel modeSelector(ModeAvailabilityModel modeAvailabilityModel) {
		File firstTripFile = getModeChoiceFile("firstTrip");
		File otherTripFile = getModeChoiceFile("otherTrip");
		ModeChoiceModel modeSelectorFirst = new ModeChoiceStuttgart(impedance(),
				new ModeSelectorParameterFirstTrip(firstTripFile));
		ModeChoiceModel modeSelectorOther = new ModeChoiceStuttgart(impedance(),
				new ModeSelectorParameterOtherTrip(otherTripFile));
		return new ModeSelectorFirstOther(modeAvailabilityModel, modeSelectorFirst, modeSelectorOther);
	}

	private File getModeChoiceFile(String fileName) {
		return context().modeChoiceParameters().valueAsFile(fileName);
	}

	private DestinationChoiceModel destinationChoiceModel(
			ImpedanceIfc impedance, ModeAvailabilityModel modeAvailabilityModel) {
		Map<String, String> destinationChoiceFiles = context().configuration().getDestinationChoice();

		return new DestinationChoiceWithFixedLocations(
				zoneRepository().zones(),
				new DestinationChoiceClayton(
						new DestinationChoiceModelStuttgart(impedance, destinationChoiceFiles.get("cost")),
						modeAvailabilityModel,
						impedance, new CarRangeReachableZonesFilter(impedance),
						false));
//                        new CarRangeReachableZonesFilter(impedance),
//                        new AttractivityCalculatorCostNextPole(zoneRepository().zones(),
//                                impedance, destinationChoiceFiles.get("cost"), 0.5f)));
	}

	public static void main(String... args) throws IOException {
		if (1 > args.length) {
			System.out.println("Usage: ... <configuration file>");
			System.exit(-1);
		}

		File configurationFile = new File(args[0]);
		LocalDateTime start = LocalDateTime.now();
		startSimulation(configurationFile);
		LocalDateTime end = LocalDateTime.now();
		Duration runtime = Duration.between(start, end);
		System.out.println("Simulation took " + runtime);
	}

	public static void startSimulation(File configurationFile) throws IOException {
		SimulationContext context = new ContextBuilder().buildFrom(configurationFile);
		startSimulation(context);
	}

	public static void startSimulation(SimulationContext context) {
		new Mobitopp(context).simulate();
	}
}
