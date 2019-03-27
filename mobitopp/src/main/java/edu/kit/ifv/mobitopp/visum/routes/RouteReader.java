package edu.kit.ifv.mobitopp.visum.routes;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * This class parses elements of a visum route assignment. The route assignment data is given as
 * {@code VisumTable}. The class will generate a set of routes from zone to zone identified by their
 * origin and destination.
 * 
 * @author bp1110
 */
public class RouteReader {

  private final Map<OdPair, ZoneRoute> routes;
  private State state;
  private RouteBuilder current;

  public RouteReader() {
    super();
    state = State.start;
    routes = new TreeMap<>();
  }

  /**
   * Transforms a visum route assignment result into {@link ZoneRoute}s. It maps the
   * {@link ZoneRoute}s to the corresponding origin and destination pair ({@code OdPair}).
   * 
   * @param rows containing visum route assignment result
   * @return mapping of {@link OdPair}s to {@link ZoneRoute}s
   */
  public Map<OdPair, ZoneRoute> transform(Stream<Row> rows) {
    rows.sequential().forEach(this::doTransform);
    addRoute();
    return routes;
  }

  private void doTransform(Row row) {
    state = state.getCurrentState(row);
    state.parse(row, this);
  }

  void startNewRoute(Row row) {
    String origin = row.get("QBEZNR");
    String destination = row.get("ZBEZNR");
    current = new RouteBuilder(origin, destination);
  }

  void addZone(String zone) {
    current.addZone(zone);
  }

  void addRoute() {
    OdPair odPair = current.buildOdPair();
    if (routes.containsKey(odPair)) {
      return;
    }
    ZoneRoute zoneRoute = current.buildZoneRoute();
    routes.put(odPair, zoneRoute);
    System.out.println(String.format("OdPair: %s - %s", odPair.origin(), odPair.destination()));
  }
}