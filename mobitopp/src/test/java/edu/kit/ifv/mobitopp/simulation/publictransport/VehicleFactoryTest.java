package edu.kit.ifv.mobitopp.simulation.publictransport;

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static edu.kit.ifv.mobitopp.publictransport.model.Data.fromSomeToAnother;
import static edu.kit.ifv.mobitopp.publictransport.model.JourneyBuilder.journey;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import edu.kit.ifv.mobitopp.publictransport.model.Connection;
import edu.kit.ifv.mobitopp.publictransport.model.Journey;
import edu.kit.ifv.mobitopp.publictransport.model.RoutePoints;
import edu.kit.ifv.mobitopp.publictransport.model.Stop;
import edu.kit.ifv.mobitopp.simulation.publictransport.model.Vehicle;

public class VehicleFactoryTest {

	private Journey journey;
	private VehicleFactory factory;

	@Before
	public void initialise() {
		journey = journey().build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void failsOnEmptyJourney() {
		createVehicle();
	}

	private Vehicle createVehicle() {
		factory = new VehicleFactory();
		return factory.createFrom(journey);
	}

	@Test
	public void vehicleStartsAtDepot() {
		journey.connections().add(someConnection());
		Vehicle vehicle = createVehicle();

		Stop currentStop = vehicle.currentStop();
		Optional<Connection> nextConnection = vehicle.nextConnection();

		assertThat(currentStop, is(equalTo(depot())));
		assertThat(nextConnection, hasValue(depotExit()));
	}

	private Connection someConnection() {
		return fromSomeToAnother();
	}

	private Stop depot() {
		return factory.depot();
	}

	private Connection depotExit() {
		return Connection.from(depot().id(), depot(), someConnection().start(),
				someConnection().departure(), someConnection().departure(), journey,
				RoutePoints.from(depot(), someConnection().start()));
	}
}
