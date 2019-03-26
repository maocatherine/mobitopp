package edu.kit.ifv.mobitopp.simulation.person;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.simulation.ActivityType;
import edu.kit.ifv.mobitopp.simulation.ImpedanceIfc;
import edu.kit.ifv.mobitopp.simulation.Mode;
import edu.kit.ifv.mobitopp.simulation.PersonListener;
import edu.kit.ifv.mobitopp.simulation.TripData;
import edu.kit.ifv.mobitopp.simulation.Trip;
import edu.kit.ifv.mobitopp.simulation.carsharing.CarSharingCar;
import edu.kit.ifv.mobitopp.simulation.carsharing.CarSharingDataForZone;
import edu.kit.ifv.mobitopp.time.Time;

public class CarSharingFreeFloatingTripTest {

  private TripSetup setup;
  private ImpedanceIfc impedance;
  private SimulationPerson person;
  private Time currentTime;
  private TripData data;
  private Zone zone;
  private PersonListener listener;
  private CarSharingCar car;
  private CarSharingDataForZone carSharingData;

  @BeforeEach
  public void initialise() {
    setup = TripSetup.create();
    impedance = setup.impedance;
    person = setup.person;
    data = setup.tripData;
    zone = setup.zone;
    currentTime = setup.currentTime;
    listener = setup.results;
    car = mock(CarSharingCar.class);
    carSharingData = mock(CarSharingDataForZone.class);
    zone.setCarSharing(carSharingData);
  }

  @Test
  void allocateVehicle() throws Exception {
    CarSharingCar carSharingCar = mock(CarSharingCar.class);
    setup.configureCurrentActivity(ActivityType.HOME);
    when(person.isCarDriver()).thenReturn(false);
    when(carSharingData.isFreeFloatingCarSharingCarAvailable(person)).thenReturn(true);
    when(carSharingData.bookFreeFloatingCar(person)).thenReturn(carSharingCar);
    CarSharingFreeFloatingTrip carSharingTrip = newTrip();

    carSharingTrip.prepareTrip(impedance, currentTime);

    verify(person).useCar(carSharingCar, currentTime);
    verify(carSharingData).bookFreeFloatingCar(person);
  }

  @Test
  void returnCarInFreeFloatingArea() throws Exception {
    setup.configureNextActivity(ActivityType.HOME);
    configureMode();
    configureCarUsage();
    configureFreeFloatingZone(true);

    Trip privateCarTrip = newTrip();

    FinishedTrip finishedTrip = privateCarTrip.finish(currentTime, listener);

    assertThat(finishedTrip.vehicleId(), isEmpty());
    verify(person).releaseCar(currentTime);
    verify(car).returnCar(zone);
  }

  @Test
  void doNotReturnCarOutOfFreeFloatingArea() throws Exception {
    setup.configureNextActivity(ActivityType.HOME);
    configureMode();
    configureCarUsage();
    configureFreeFloatingZone(false);

    Trip privateCarTrip = newTrip();

    FinishedTrip finishedTrip = privateCarTrip.finish(currentTime, listener);

    assertThat(finishedTrip.vehicleId(), isEmpty());
    verify(person).whichCar();
    verify(person).isCarDriver();
    verifyNoMoreInteractions(person);
    verifyZeroInteractions(car);
  }

  private CarSharingFreeFloatingTrip newTrip() {
    return new CarSharingFreeFloatingTrip(data, person);
  }

  private void configureFreeFloatingZone(boolean value) {
    when(carSharingData.isFreeFloatingZone(car)).thenReturn(value);
  }

  private void configureMode() {
    when(data.mode()).thenReturn(Mode.CARSHARING_FREE);
  }

  private void configureCarUsage() {
    when(person.isCarDriver()).thenReturn(true);
    when(person.whichCar()).thenReturn(car);
    when(person.releaseCar(currentTime)).thenReturn(car);
  }
}