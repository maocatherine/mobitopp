package edu.kit.ifv.mobitopp.visum;


import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Stream;

import edu.kit.ifv.mobitopp.visum.routes.Row;

public class VisumNetworkReader extends VisumBaseReader {

	static final double alwaysAllowed = 1.0;
	private File file;
  private VisumFileReader visumReader;


	public VisumNetworkReader(NetfileLanguage language) {
		super(language);
	}
	
	public VisumNetworkReader() {
	  this(StandardNetfileLanguages.german());
	}

	public VisumNetwork readNetwork(String filename) {

		File file = new File(filename);

		return readNetwork(file);
	}

	public VisumNetwork readNetwork(File file) {
	this.file = file;
	visumReader = new CachedVisumReader();
  long startTime = System.currentTimeMillis();
	long lastCurrentTime = startTime;

System.out.println("reading data...");
		System.out.println("done");

	long currentTime = System.currentTimeMillis();

	System.out.println("Reading raw data took " + ((currentTime-lastCurrentTime) / 1000) + " seconds");

		System.out.println("\n\n\n");

System.out.println("reading tables...");

	lastCurrentTime = System.currentTimeMillis();

		VisumTransportSystems transportSystems = readTransportSystems();
		VisumLinkTypes linkTypes = readLinkTypes(transportSystems);

System.out.println(" reading nodes...");
		Map<Integer, VisumNode> nodes = readNodes();

System.out.println(" reading links...");
		Map<Integer, VisumLink> links = readLinks(nodes, transportSystems, linkTypes);

System.out.println(" reading turns...");
		Map<Integer, List<VisumTurn>> turns = readTurns(nodes, transportSystems);

System.out.println(" reading zones...");
		Map<Integer, VisumZone> zones = readZones();

System.out.println(" reading connectors...");
		Map<Integer, List<VisumConnector>> connectors = readConnectors(nodes, zones, transportSystems);

	currentTime = System.currentTimeMillis();

	System.out.println("Parsing network data took " + ((currentTime-lastCurrentTime) / 1000) + " seconds");

	lastCurrentTime = currentTime;

System.out.println(" public transport network...");
System.out.println(" reading other...");
		Map<Integer, VisumVehicleUnit> vehicleUnits = readVehicleUnits(transportSystems);
		Map<Integer, VisumVehicleCombination> vehicleCombinations = readVehicleCombinations(vehicleUnits);

System.out.println(" reading stop hierarchy...");
		Map<Integer, VisumPtStop> ptStops = readPtStations();
		Map<Integer, VisumPtStopArea> ptStopAreas = readPtStopAreas(nodes, ptStops);
		Map<Integer, VisumPtStopPoint> ptStopPoints = readPtStopPoints(nodes, links, ptStopAreas, transportSystems);
		Map<StopAreaPair, VisumPtTransferWalkTimes> walkTimes = readTransferWalkTimesMatrix(ptStopAreas);

System.out.println(" reading other...");
		Map<String, VisumPtLine> ptLines = readPtLines(transportSystems);
		Map<String, VisumPtLineRoute> ptLineRoutes = readPtLineRoutes(ptLines);
		readPtLineRouteElements(ptLineRoutes, ptStopPoints, nodes);

System.out.println(" reading other...");
		Map<String,VisumPtTimeProfile> ptTimeProfiles = readPtTimeProfile(ptLineRoutes);
    List<VisumPtVehicleJourney> ptVehicleJourneys = readPtVehicleJourneys(ptLineRoutes,
        ptTimeProfiles, vehicleCombinations);

	currentTime = System.currentTimeMillis();

	System.out.println("Parsing public transport network data took " + ((currentTime-lastCurrentTime) / 1000) + " seconds");

	lastCurrentTime = currentTime;

System.out.println(" reading polygons...");

		SortedMap<Integer,VisumSurface> areas = readSurfaces();

	currentTime = System.currentTimeMillis();

	System.out.println("Parsing polygons took " + ((currentTime-lastCurrentTime) / 1000) + " seconds");

	lastCurrentTime = currentTime;

System.out.println(" reading custom data...");
		Map<Integer, VisumChargingFacility> chargingFacilities = readChargingFacilities();
		Map<Integer, VisumChargingPoint> chargingPoints = readChargingPoints();

		Map<Integer, VisumCarSharingStation> carSharingStationsStadtmobil = readCarSharingStadtmobil();
		Map<Integer, VisumCarSharingStation> carSharingStationsFlinkster = readCarSharingFlinkster();

    Map<String, Map<Integer, VisumCarSharingStation>> carSharingStations = new HashMap<>();

		carSharingStations.put("Stadtmobil",  Collections.unmodifiableMap(carSharingStationsStadtmobil));
		carSharingStations.put("Flinkster",  Collections.unmodifiableMap(carSharingStationsFlinkster));

	currentTime = System.currentTimeMillis();

	System.out.println("Parsing custom data " + ((currentTime-lastCurrentTime) / 1000) + " seconds");

	lastCurrentTime = currentTime;

System.out.println(" reading territories...");
		Map<Integer, VisumTerritory> territories = readTerritories(areas);

	currentTime = System.currentTimeMillis();

	System.out.println("Parsing territory data " + ((currentTime-lastCurrentTime) / 1000) + " seconds");

	lastCurrentTime = currentTime;

		System.gc();


		VisumNetwork network = new VisumNetwork(
																	transportSystems,
																	linkTypes, 
																	nodes,
																	links,
																	turns,
																	zones,
																	connectors,
																	vehicleCombinations,
																	ptStops,
																	ptStopAreas,
																	ptStopPoints,
																	walkTimes,
																	ptLines,
																	ptLineRoutes,
																	ptTimeProfiles,
																	ptVehicleJourneys,
																	areas,
																	chargingFacilities,
																	chargingPoints,
																	carSharingStations,
																	territories
														);

		currentTime = System.currentTimeMillis();

		System.out.println("Reading  data took " + ((currentTime-startTime) / 1000) + " seconds");

		return network;
	}

	private VisumTransportSystems readTransportSystems() {
	  String tableName = table(Table.transportSystems);
    Stream<Row> content = loadContentOf(tableName);
		VisumTransportSystemReader reader = new VisumTransportSystemReader(language);
		return reader.readTransportSystems(content);
	}

  Stream<Row> loadContentOf(String tableName) {
    return visumReader.read(file, tableName);
  }

  private VisumLinkTypes readLinkTypes(VisumTransportSystems allSystems) {
    VisumLinkTypeReader reader = new VisumLinkTypeReader(language);
    Stream<Row> rows = loadContentOf(table(Table.linkTypes));
    return reader.readLinkTypes(allSystems, rows);
  }

	static int walkSpeed(Row row, NetfileLanguage language) {
		String publicWalkSpeed = language.resolve(StandardAttributes.publicTransportWalkSpeed);
		String individualWalkSpeed = language.resolve(StandardAttributes.individualWalkSpeed);
		if (row.containsAttribute(publicWalkSpeed)) {
			Integer publicTransport = parseSpeed(row.get(publicWalkSpeed), language);
			if (row.containsAttribute(individualWalkSpeed)) {
				Integer individualTransport = parseSpeed(row.get(individualWalkSpeed), language);
				if (publicTransport.equals(individualTransport)) {
					return publicTransport;
				}
				System.err.println("Different speed values für walk speed in public transport walk type and individual traffic walk type");
				return 0;
			}
			return publicTransport;
		}
		if (row.containsAttribute(individualWalkSpeed)) {
			return parseSpeed(row.get(individualWalkSpeed), language);
		}
		return 0;
	}

  private Map<Integer, VisumNode> readNodes() {
    VisumNodeReader reader = new VisumNodeReader(language);
    Stream<Row> rows = loadContentOf(table(Table.nodes));
    return reader.readNodes(rows);
  }

  private Map<Integer, VisumLink> readLinks(
      Map<Integer, VisumNode> nodes, VisumTransportSystems transportSystems,
      VisumLinkTypes linkTypes) {
    Stream<Row> content = loadContentOf(table(Table.links));
    return new VisumLinkReader(language, nodes, transportSystems, linkTypes).readLinks(content);
  }

  private Map<Integer, List<VisumTurn>> readTurns(
      Map<Integer, VisumNode> nodes, VisumTransportSystems allSystems) {
		Stream<Row> content = loadContentOf(table(Table.turns));
    Map<Integer, List<VisumTurn>> data = new VisumTurnsReader(language, nodes, allSystems)
        .readTurns(content);
    assignTurnsToNodes(nodes, data);
		return data;
	}

  public void assignTurnsToNodes(
      Map<Integer, VisumNode> nodes, Map<Integer, List<VisumTurn>> data) {
    for (Integer nodeId : nodes.keySet()) {
      VisumNode node = nodes.get(nodeId);
      if (data.containsKey(nodeId)) {
        List<VisumTurn> turns = data.get(nodeId);
        node.setTurns(turns);
      } else {
        System.out.println("\n\n\n nodeId= " + nodeId + " has no turns!!!\n\n\n");
      }
		}
  }

  Map<Integer, VisumZone> readZones() {
    Stream<Row> content = loadContentOf(table(Table.zones));
    return new VisumZoneReader(language).readZones(content);
  }

  private Map<Integer, List<VisumConnector>> readConnectors(
      Map<Integer, VisumNode> nodes, Map<Integer, VisumZone> zones,
      VisumTransportSystems allSystems) {
    Stream<Row> content = loadContentOf(table(Table.connectors));
    return new VisumConnectorReader(language, nodes, zones, allSystems).readConnectors(content);
  }

  private Map<Integer, VisumVehicleUnit> readVehicleUnits(VisumTransportSystems allSystems) {
    Stream<Row> content = loadContentOf(table(Table.vehicleUnit));
    Map<Integer, VisumVehicleUnit> vehicleUnits = new VisumVehicleUnitReader(language, allSystems)
        .readVehicleUnits(content);
    if (vehicleUnits.isEmpty()) {
      System.out.println("Vehicle units are missing!");
    }
    return vehicleUnits;
  }

  private Map<Integer, VisumVehicleCombination> readVehicleCombinations(
      Map<Integer, VisumVehicleUnit> vehicleUnits) {
    Stream<Row> content = loadContentOf(table(Table.vehicleUnitToCombinations));
    Map<Integer, List<VisumVehicleCombinationUnit>> units2combinations = new VisumVehicleUnitsToCombinationsReader(
        language, vehicleUnits).readMapping(content);

    Stream<Row> combinationContent = loadContentOf(table(Table.vehicleCombinations));
    Map<Integer, VisumVehicleCombination> combinations = new VisumVehicleCombinationReader(language,
        units2combinations).readCombinations(combinationContent);
    if (combinations.isEmpty()) {
      System.out.println("Vehicle combinations are missing!");
    }
    return combinations;
  }

  private Map<Integer, VisumPtStop> readPtStations() {
    Stream<Row> content = loadContentOf(table(Table.station));
    return new VisumPtStationReader(language).readPtStops(content);
  }

  private Map<Integer, VisumPtStopArea> readPtStopAreas(
      Map<Integer, VisumNode> nodes, Map<Integer, VisumPtStop> ptStops) {
    Stream<Row> content = loadContentOf(table(Table.stopArea));
    return new VisumPtStopAreaReader(language, nodes, ptStops).readPtStopAreas(content);
  }

  private Map<Integer, VisumPtStopPoint> readPtStopPoints(
      Map<Integer, VisumNode> nodes, Map<Integer, VisumLink> links,
      Map<Integer, VisumPtStopArea> ptStopAreas, VisumTransportSystems allSystems) {
    Stream<Row> content = loadContentOf(table(Table.stop));
    return new VisumPtStopPointReader(language, nodes, links, ptStopAreas, allSystems)
        .readPtStopPoints(content);
  }

  private Map<StopAreaPair, VisumPtTransferWalkTimes> readTransferWalkTimesMatrix(
      Map<Integer, VisumPtStopArea> ptStopAreas) {
    Stream<Row> content = loadContentOf(table(Table.transferWalkTimes));
    return new VisumPtTransferWalkTimesReader(language, ptStopAreas).readTransferWalkTimes(content);
  }

  private Map<String, VisumPtLine> readPtLines(VisumTransportSystems systems) {
    Stream<Row> content = loadContentOf(table(Table.line));
    return new VisumPtLineReader(language, systems).readPtLines(content);
  }

  private Map<String, VisumPtLineRoute> readPtLineRoutes(Map<String, VisumPtLine> ptLines) {
    Stream<Row> content = loadContentOf(table(Table.lineRoute));
    Map<String, VisumPtLineRoute> result = new VisumPtLineRouteReader(language, ptLines,
        this::direction).readPtLineRoutes(content);
    assignRoutesToLines(ptLines, result);
    return result;
  }

  void assignRoutesToLines(Map<String, VisumPtLine> ptLines, Map<String, VisumPtLineRoute> result) {
    result
        .values()
        .stream()
        .collect(groupingBy(r -> r.line.name))
        .entrySet()
        .forEach(e -> ptLines.get(e.getKey()).setLineRoutes(e.getValue()));
  }

  protected VisumPtLineRouteDirection direction(String lineRouteDirection) {
		return isInDirection(lineRouteDirection) ? VisumPtLineRouteDirection.H
		                    																						: VisumPtLineRouteDirection.R;
	}

	private boolean isInDirection(String lineRouteDirection) {
		return "H".equals(lineRouteDirection) || ">".equals(lineRouteDirection);
	}

  private void readPtLineRouteElements(
      Map<String, VisumPtLineRoute> ptLineRoutes, Map<Integer, VisumPtStopPoint> ptStopPoints,
      Map<Integer, VisumNode> nodes) {
    Stream<Row> content = loadContentOf(table(Table.lineRouteElement));

    Map<VisumPtLineRoute, SortedMap<Integer, VisumPtLineRouteElement>> data = new VisumPtLineRouteElementReader(
        language, ptLineRoutes, ptStopPoints, nodes).readElements(content);

    data.entrySet().forEach(e -> e.getKey().setElements(e.getValue()));
  }

  private Map<String, VisumPtTimeProfile> readPtTimeProfile(
      Map<String, VisumPtLineRoute> ptLineRoutes) {
    Stream<Row> content = loadContentOf(table(Table.timeProfileElement));
    Map<String, Map<Integer, VisumPtTimeProfileElement>> elements = new VisumPtTimeProfileElementReader(
        language, ptLineRoutes).readElements(content);

    Stream<Row> profileContent = loadContentOf(table(Table.timeProfile));
    return new VisumPtTimeProfileReader(language, ptLineRoutes, elements)
        .readProfile(profileContent);
  }

  private List<VisumPtVehicleJourney> readPtVehicleJourneys(
      Map<String, VisumPtLineRoute> ptLineRoutes, Map<String, VisumPtTimeProfile> ptTimeProfiles,
      Map<Integer, VisumVehicleCombination> vehicleCombinations) {
    Stream<Row> content = loadContentOf(table(Table.vehicleJourneyPart));
    Map<Integer, List<VisumPtVehicleJourneySection>> sections = new VisumPtVehicleJourneySectionReader(
        language, vehicleCombinations).readSections(content);

    if (sections.isEmpty()) {
      System.out.println("Vehicle journey parts are missing!");
    }

    Stream<Row> journeyContent = loadContentOf(table(Table.vehicleJourney));
    return new VisumPtVehicleJourneyReader(language, ptLineRoutes, ptTimeProfiles, sections)
        .readJourneys(journeyContent);
  }
  
  private SortedMap<Integer, VisumSurface> readSurfaces() {
    Map<Integer, VisumPoint> points = readPoints();
    Map<Integer, SortedMap<Integer, VisumPoint>> intermediatePoints = readIntermediatePoints();
    Map<Integer, VisumEdge> lines = readEdges(points, intermediatePoints);
    Map<Integer, VisumFace> rings = readFaces(lines);

    Stream<Row> table = loadContentOf(table(Table.surface));
    return new VisumSurfaceReader(language, rings).readSurfaces(table);
  }

  private Map<Integer, VisumPoint> readPoints() {
    Stream<Row> rows = loadContentOf(table(Table.point));
    return new VisumPointReader(language).readPoints(rows);
  }

  private Map<Integer, SortedMap<Integer, VisumPoint>> readIntermediatePoints() {
    Stream<Row> content = loadContentOf(table(Table.intermediatePoint));
    return new VisumIntermediatePointReader(language).readPoints(content);
  }

  private Map<Integer, VisumEdge> readEdges(
      Map<Integer, VisumPoint> points,
      Map<Integer, SortedMap<Integer, VisumPoint>> intermediatePoints) {
    Stream<Row> content = loadContentOf(table(Table.edges));
    return new VisumEdgeReader(language, points, intermediatePoints).readEdges(content);
  }

  private Map<Integer, VisumFace> readFaces(Map<Integer, VisumEdge> lines) {
    Stream<Row> content = loadContentOf(table(Table.faces));
    return new VisumFaceReader(language, lines).readFaces(content);
  }

  Map<Integer, VisumChargingFacility> readChargingFacilities() {
		Stream<Row> content = loadContentOf(poiCategory());
    POICategories categories = POICategories.from(content);
		if (categories.containsCode(chargingStations())) {
			return readChargingStations(categories);
		}
		return Collections.emptyMap();
	}

  private Map<Integer, VisumChargingFacility> readChargingStations(POICategories categories) {
    int nr = categories.numberByCode(chargingStations());
    Stream<Row> rows = loadContentOf(poiCategoryPrefix() + nr);
    return new VisumChargingFacilityReader(language).readStations(rows);
  }

  Map<Integer, VisumChargingPoint> readChargingPoints() {
    Stream<Row> content = loadContentOf(poiCategory());
    POICategories categories = POICategories.from(content);
    if (categories.containsCode(chargingPoints())) {
      return readChargingPoints(categories);
    }
    return emptyMap();
  }

  private Map<Integer, VisumChargingPoint> readChargingPoints(POICategories categories) {
    int nr = categories.numberByCode(chargingPoints());
    Stream<Row> content = loadContentOf(poiCategoryPrefix() + nr);
    return new VisumChargingPointReader(language).readPoints(content);
  }

  private Map<Integer, VisumCarSharingStation> readCarSharingStadtmobil() {
    final String categoryName = attribute(StandardAttributes.carsharingStadtmobil);
    POICategories categories = POICategories.from(loadContentOf(poiCategory()));
    if (!categories.containsCode(categoryName)) {
      return emptyMap();
    }
    int nr = categories.numberByCode(categoryName);
    Stream<Row> content = loadContentOf(poiCategoryPrefix() + nr);
    return new VisumCarsharingReader(language).readCarsharingStations(content);
  }

  private Map<Integer, VisumCarSharingStation> readCarSharingFlinkster() {
    final String categoryName = attribute(StandardAttributes.carsharingFlinkster);
    POICategories categories = POICategories.from(loadContentOf(poiCategory()));
    if (!categories.containsCode(categoryName)) {
      return emptyMap();
    }
    int nr = categories.numberByCode(categoryName);
    Stream<Row> content = loadContentOf(poiCategoryPrefix() + nr);
    return new VisumCarsharingReader(language).readCarsharingStations(content);
  }

  private Map<Integer, VisumTerritory> readTerritories(SortedMap<Integer, VisumSurface> polygons) {
    Stream<Row> rows = loadContentOf(table(Table.territories));
    return new VisumTerritoryReader(language, polygons).readTerritories(rows);
  }

}
