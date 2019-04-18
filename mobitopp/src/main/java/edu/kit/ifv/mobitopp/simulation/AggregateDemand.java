package edu.kit.ifv.mobitopp.simulation;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Consumer;

import edu.kit.ifv.mobitopp.data.IntegerMatrix;
import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.data.ZoneId;
import edu.kit.ifv.mobitopp.routing.Path;
import edu.kit.ifv.mobitopp.simulation.activityschedule.ActivityIfc;
import edu.kit.ifv.mobitopp.simulation.person.FinishedTrip;
import edu.kit.ifv.mobitopp.simulation.tour.Subtour;
import edu.kit.ifv.mobitopp.simulation.tour.Tour;

public class AggregateDemand implements PersonListener {

  private final Consumer<IntegerMatrix> writer;
  private final IntegerMatrix matrix;

  public AggregateDemand(Consumer<IntegerMatrix> writer, List<ZoneId> ids) {
    super();
    this.writer = writer;
    matrix = new IntegerMatrix(createMatrixColumns(ids));
  }

  private List<Integer> createMatrixColumns(List<ZoneId> ids) {
    return ids.stream().map(ZoneId::getMatrixColumn).collect(toList());
  }
  
  @Override
  public void notifyEndTrip(Person person, FinishedTrip trip, ActivityIfc activity) {
    ZoneId origin = trip.origin().zone().getInternalId();
    ZoneId destination = trip.destination().zone().getInternalId();
    matrix.increment(origin, destination);
  }
  
  @Override
  public void notifyFinishSimulation() {
    writer.accept(matrix);
  }

  @Override
  public void notifyFinishCarTrip(Person person, Car car, FinishedTrip trip, ActivityIfc activity) {
  }

  @Override
  public void notifyStartActivity(Person person, ActivityIfc activity) {
  }

  @Override
  public void notifySelectCarRoute(Person person, Car car, TripData trip, Path route) {
  }

  @Override
  public void writeSubourinfoToFile(Person person, Tour tour, Subtour subtour, Mode tourMode) {
  }

  @Override
  public void writeTourinfoToFile(Person person, Tour tour, Zone tourDestination, Mode tourMode) {
  }

  @Override
  public void notifyStateChanged(StateChange stateChange) {
  }

}
