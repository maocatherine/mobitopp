package edu.kit.ifv.mobitopp.visum;

import lombok.Builder;

@Builder
public class StandardNetfileLanguages {

    private final String carSystem;
    private final String individualWalkSystem;
    private final String publicTransportWalkSystem;

    public static StandardNetfileLanguages defaultSystems() {
        String carSystem = "CAR";
        String individualWalkSystem = "PED";
        String publicTransportWalkSystem = "PUTW";
        return new StandardNetfileLanguages(carSystem, individualWalkSystem, publicTransportWalkSystem);
    }

    public NetfileLanguage english() {
        DynamicNetfileLanguage language = new DynamicNetfileLanguage();
        language.add(Table.transportSystems, "TSYS");
        language.add(Table.linkTypes, "LINKTYPE");
        language.add(Table.nodes, "NODE");
        language.add(Table.links, "LINK");
        language.add(Table.turns, "TURN");
        language.add(Table.zones, "ZONE");
        language.add(Table.surface, "SURFACEITEM");
        language.add(Table.point, "POINT");
        language.add(Table.intermediatePoint, "EDGEITEM");
        language.add(Table.edges, "EDGE");
        language.add(Table.faces, "FACEITEM");
        language.add(Table.territories, "TERRITORY");
        language.add(Table.poiCategory, "POICATEGORY");
        language.add(Table.poiCategoryPrefix, "POIOFCAT_");
        language.add(Table.connectors, "CONNECTOR");
        language.add(Table.vehicleUnit, "VEHUNIT");
        language.add(Table.vehicleCombinations, "VEHCOMB");
        language.add(Table.vehicleUnitToCombinations, "VEHUNITTOVEHCOMB");
        language.add(Table.station, "STOP");
        language.add(Table.stopArea, "STOPAREA");
        language.add(Table.stop, "STOPPOINT");
        language.add(Table.transferWalkTimes, "TRANSFERWALKTIMESTOPAREA");
        language.add(Table.line, "LINE");
        language.add(Table.lineRoute, "LINEROUTE");
        language.add(Table.lineRouteElement, "LINEROUTEITEM");
        language.add(Table.timeProfile, "TIMEPROFILE");
        language.add(Table.timeProfileElement, "TIMEPROFILEITEM");
        language.add(Table.vehicleJourney, "VEHJOURNEY");
        language.add(Table.vehicleJourneyPart, "VEHJOURNEYSECTION");

        // TransportSystem
        language.add(StandardAttributes.code, "CODE");
        language.add(StandardAttributes.name, "NAME");
        language.add(StandardAttributes.type, "TYPE");

        // LinkType
        language.add(StandardAttributes.number, "NO");
        language.add(StandardAttributes.transportSystemSet, "TSYSSET");
        language.add(StandardAttributes.numberOfLanes, "NUMLANES");
        language.add(StandardAttributes.capacityCar, "CAPPRT");
        language.add(StandardAttributes.freeFlowSpeedCar, "V0PRT");

        // Node
        language.add(StandardAttributes.typeNumber, "TYPENO");
        language.add(StandardAttributes.xCoord, "XCOORD");
        language.add(StandardAttributes.yCoord, "YCOORD");
        language.add(StandardAttributes.zCoord, "ZCOORD");

        // Link
        language.add(StandardAttributes.fromNodeNumber, "FROMNODENO");
        language.add(StandardAttributes.toNodeNumber, "TONODENO");
        language.add(StandardAttributes.length, "LENGTH");
        language.add(StandardAttributes.individualWalkSpeed, "VMAX-IVSYS(" + individualWalkSystem + ")");
        language.add(StandardAttributes.publicTransportWalkSpeed, "VSTD-OEVSYS(" + publicTransportWalkSystem + ")");

        // Turn
        language.add(StandardAttributes.freeFlowTravelTimeCar, "T0PRT");
        language.add(StandardAttributes.viaNodeNumber, "VIANODENO");

        // Zone No attribute is available from visum 20.08.21
        language.add(StandardAttributes.parkingPlaces, "PARKRAUM");
        language.add(StandardAttributes.mainZoneNumber, "MAINZONENO");
        language.add(StandardAttributes.areaId, "SURFACEID");
        language.add(StandardAttributes.chargingStations, "LADESTATIONEN");
        language.add(StandardAttributes.car2GoTerritory, "CAR2GO_GEBIET");
        language.add(StandardAttributes.car2GoStartState, "CAR2GO_AUSGANGSZUSTAND");
        language.add(StandardAttributes.carSharingDensityCar2Go, "FZ_FL_C2G");
        language.add(StandardAttributes.carSharingDensityFlinkster, "FZ_FL_FL");
        language.add(StandardAttributes.carSharingDensityStadtmobil, "FZ_FL_SM");
        language.add(StandardAttributes.privateChargingProbability, "ANTEIL_STE");
        language.add(StandardAttributes.innerZonePublicTransportTravelTime, "DIAG_OEV");

        // Connector
        language.add(StandardAttributes.zoneNumber, "ZONENO");
        language.add(StandardAttributes.nodeNumber, "NODENO");
        language.add(StandardAttributes.direction, "DIRECTION");
        language.add(StandardAttributes.fromOrigin, "O");
        language.add(StandardAttributes.toDestination, "D");
        language.add(StandardAttributes.travelTimeCar, "T0_TSYS(" + carSystem + ")");

        // VehicleUnit
        language.add(StandardAttributes.vehicleCapacity, "TOTALCAP");
        language.add(StandardAttributes.seats, "SEATCAP");

        // VehicleCombination
        language.add(StandardAttributes.vehicleCombinationNumber, "VEHCOMBNO");
        language.add(StandardAttributes.vehicleUnitNumber, "VEHUNITNO");
        language.add(StandardAttributes.numberOfVehicleUnits, "NUMVEHUNITS");

        // StopArea
        language.add(StandardAttributes.stationNumber, "STOPNO");

        // Stop
        language.add(StandardAttributes.stopAreaNumber, "STOPAREANO");
        language.add(StandardAttributes.directed, "DIRECTED");
        language.add(StandardAttributes.linkNumber, "LINKNO");
        language.add(StandardAttributes.relativePosition, "RELPOS");

        // TransferWalkTimes
        language.add(StandardAttributes.fromStopArea, "FROMSTOPAREANO");
        language.add(StandardAttributes.toStopArea, "TOSTOPAREANO");
        language.add(StandardAttributes.time, "TIME");
        language.add(StandardAttributes.transportSystemCode, "TSYSCODE");

        // Pt LineRoute
        language.add(StandardAttributes.lineName, "LINENAME");
        language.add(StandardAttributes.directionCode, "DIRECTIONCODE");

        // Pt LineRouteElement
        language.add(StandardAttributes.stopNumber, "STOPPOINTNO");
        language.add(StandardAttributes.index, "INDEX");
        language.add(StandardAttributes.isRoutePoint, "ISROUTEPOINT");
        language.add(StandardAttributes.toLength, "POSTLENGTH");

        // TimeProfile
        language.add(StandardAttributes.lineRouteName, "LINEROUTENAME");
        language.add(StandardAttributes.timeProfileName, "TIMEPROFILENAME");
        language.add(StandardAttributes.lineRouteElementIndex, "LRITEMINDEX");
        language.add(StandardAttributes.getOff, "ALIGHT");
        language.add(StandardAttributes.board, "BOARD");
        language.add(StandardAttributes.arrival, "ARR");
        language.add(StandardAttributes.departure, "DEP");

        // VehicleJourney
//        language.add(StandardAttributes.vehicleJourneyNumber, "VEHJOURNEYNO");
//        language.add(StandardAttributes.fromTimeProfileElementIndex, "FROMTPROFITEMINDEX");
//        language.add(StandardAttributes.toTimeProfileElementIndex, "TOTPROFITEMINDEX");
//        language.add(StandardAttributes.vehicleDayNumber, "VALIDDAYSNO");

        language.add(StandardAttributes.vehicleJourneyNumber, "VEHJOURNEYNO");
        language.add(StandardAttributes.fromTimeProfileElementIndex, "FROMTPROFITEMINDEX");
        language.add(StandardAttributes.toTimeProfileElementIndex, "TOTPROFITEMINDEX");
        language.add(StandardAttributes.validDayNumber, "VALIDDAYSNO");
        language.add(StandardAttributes.validMonday, "ISVALID(MO)");
        language.add(StandardAttributes.validTuesday, "ISVALID(DI)");
        language.add(StandardAttributes.validWednesday, "ISVALID(MI)");
        language.add(StandardAttributes.validThursday, "ISVALID(DO)");
        language.add(StandardAttributes.validFriday, "ISVALID(FR)");
        language.add(StandardAttributes.validSaturday, "ISVALID(SA)");
        language.add(StandardAttributes.validSunday, "ISVALID(SO)");

        // Point
        language.add(StandardAttributes.id, "ID");

        // IntermediatePoint
        language.add(StandardAttributes.edgeId, "EDGEID");

        // Edge
        language.add(StandardAttributes.fromPointId, "FROMPOINTID");
        language.add(StandardAttributes.toPointId, "TOPOINTID");

        // Surfae
        language.add(StandardAttributes.enclave, "ENCLAVE");
        language.add(StandardAttributes.ringId, "FACEID");

        // ChargingStation
        language.add(StandardAttributes.chargingStationsCode, "Ladestationen");
        language.add(StandardAttributes.chargingType, "ART");
        language.add(StandardAttributes.lsId, "LS_ID");
        language.add(StandardAttributes.latitude, "LATITUDE");
        language.add(StandardAttributes.longitude, "LONGITUDE");
        language.add(StandardAttributes.vehicleType, "FZ");
        language.add(StandardAttributes.publicType, "PUB");
        language.add(StandardAttributes.place, "ORT");
        language.add(StandardAttributes.plz, "PLZ");
        language.add(StandardAttributes.street, "STRASSE");

        // ChargingPoints
        language.add(StandardAttributes.chargingPoints, "Ladepunkte");
        language.add(StandardAttributes.power, "LEISTUNG");

        // Carsharing
        language.add(StandardAttributes.carsharingStadtmobil, "CS_SM");
        language.add(StandardAttributes.carsharingFlinkster, "CS_FL");
        language.add(StandardAttributes.objectId, "OBJECTID");
        language.add(StandardAttributes.numberOfVehicles, "ANZAHL_FZ");
        language.add(StandardAttributes.town, "STADT");
        language.add(StandardAttributes.streetIso8859, "STRA\u00DFE");

        // Territory
        language.add(StandardAttributes.item, "ITEM");
        language.add(StandardAttributes.codeLc, "CODE_LC");
        language.add(StandardAttributes.correspondingZones, "CORRESPONDINGZONES");

        // Units
        language.add(Unit.velocity, "km/h");
        language.add(Unit.distance, "km");
        language.add(Unit.time, "s");
        return language;
    }

    public NetfileLanguage german() {
        DynamicNetfileLanguage language = new DynamicNetfileLanguage();
        language.add(Table.transportSystems, "VSYS");
        language.add(Table.linkTypes, "STRECKENTYP");
        language.add(Table.nodes, "KNOTEN");
        language.add(Table.links, "STRECKE");
        language.add(Table.turns, "ABBIEGER");
        language.add(Table.zones, "BEZIRK");
        language.add(Table.connectors, "ANBINDUNG");
        language.add(Table.vehicleUnit, "FZGEINHEIT");
        language.add(Table.vehicleCombinations, "FZGKOMB");
        language.add(Table.vehicleUnitToCombinations, "FZGEINHEITZUFZGKOMB");
        language.add(Table.station, "HALTESTELLE");
        language.add(Table.stopArea, "HALTESTELLENBEREICH");
        language.add(Table.stop, "HALTEPUNKT");
        language.add(Table.transferWalkTimes, "UEBERGANGSGEHZEITHSTBER");
        language.add(Table.line, "LINIE");
        language.add(Table.lineRoute, "LINIENROUTE");
        language.add(Table.lineRouteElement, "LINIENROUTENELEMENT");
        language.add(Table.timeProfile, "FAHRZEITPROFIL");
        language.add(Table.timeProfileElement, "FAHRZEITPROFILELEMENT");
        language.add(Table.vehicleJourney, "FAHRPLANFAHRT");
        language.add(Table.vehicleJourneyPart, "FAHRPLANFAHRTABSCHNITT");
        language.add(Table.surface, "FLAECHENELEMENT");
        language.add(Table.point, "PUNKT");
        language.add(Table.intermediatePoint, "ZWISCHENPUNKT");
        language.add(Table.edges, "KANTE");
        language.add(Table.faces, "TEILFLAECHENELEMENT");
        language.add(Table.territories, "GEBIET");
        language.add(Table.poiCategory, "POIKATEGORIE");
        language.add(Table.poiCategoryPrefix, "POIOFCAT_");

        // TransportSystem
        language.add(StandardAttributes.code, "CODE");
        language.add(StandardAttributes.name, "NAME");
        language.add(StandardAttributes.type, "TYP");

        // LinkType
        language.add(StandardAttributes.number, "NR");
        language.add(StandardAttributes.transportSystemSet, "VSYSSET");
        language.add(StandardAttributes.numberOfLanes, "ANZFAHRSTREIFEN");
        language.add(StandardAttributes.capacityCar, "KAPIV");
        language.add(StandardAttributes.freeFlowSpeedCar, "V0IV");

        // Node
        language.add(StandardAttributes.typeNumber, "TYPNR");
        language.add(StandardAttributes.xCoord, "XKOORD");
        language.add(StandardAttributes.yCoord, "YKOORD");
        language.add(StandardAttributes.zCoord, "ZKOORD");

        // Link
        language.add(StandardAttributes.fromNodeNumber, "VONKNOTNR");
        language.add(StandardAttributes.toNodeNumber, "NACHKNOTNR");
        language.add(StandardAttributes.length, "LAENGE");
        language.add(StandardAttributes.individualWalkSpeed, "VMAX-IVSYS(" + individualWalkSystem + ")");
        language.add(StandardAttributes.publicTransportWalkSpeed, "VSTD-OEVSYS(" + publicTransportWalkSystem + ")");

        // Abbieger
        language.add(StandardAttributes.freeFlowTravelTimeCar, "T0IV");
        language.add(StandardAttributes.viaNodeNumber, "UEBERKNOTNR");

        // Zone
        language.add(StandardAttributes.parkingPlaces, "PARKRAUM");
        language.add(StandardAttributes.mainZoneNumber, "OBEZNR");
        language.add(StandardAttributes.areaId, "FLAECHEID");
        language.add(StandardAttributes.chargingStations, "LADESTATIONEN");
        language.add(StandardAttributes.car2GoTerritory, "CAR2GO_GEBIET");
        language.add(StandardAttributes.car2GoStartState, "CAR2GO_AUSGANGSZUSTAND");
        language.add(StandardAttributes.carSharingDensityCar2Go, "FZ_FL_C2G");
        language.add(StandardAttributes.carSharingDensityFlinkster, "FZ_FL_FL");
        language.add(StandardAttributes.carSharingDensityStadtmobil, "FZ_FL_SM");
        language.add(StandardAttributes.privateChargingProbability, "ANTEIL_STE");
        language.add(StandardAttributes.innerZonePublicTransportTravelTime, "DIAG_OEV");

        // Connector
        language.add(StandardAttributes.zoneNumber, "BEZNR");
        language.add(StandardAttributes.nodeNumber, "KNOTNR");
        language.add(StandardAttributes.direction, "RICHTUNG");
        language.add(StandardAttributes.fromOrigin, "Q");
        language.add(StandardAttributes.toDestination, "Z");
        language.add(StandardAttributes.travelTimeCar, "T0-VSYS(" + carSystem + ")");

        // VehicleUnit
        language.add(StandardAttributes.vehicleCapacity, "GESAMTPL");
        language.add(StandardAttributes.seats, "SITZPL");

        // VehicleCombination
        language.add(StandardAttributes.vehicleCombinationNumber, "FZGKOMBNR");
        language.add(StandardAttributes.vehicleUnitNumber, "FZGEINHEITNR");
        language.add(StandardAttributes.numberOfVehicleUnits, "ANZFZGEINH");

        // StopArea
        language.add(StandardAttributes.stationNumber, "HSTNR");

        // Stop
        language.add(StandardAttributes.stopAreaNumber, "HSTBERNR");
        language.add(StandardAttributes.directed, "GERICHTET");
        language.add(StandardAttributes.linkNumber, "STRNR");
        language.add(StandardAttributes.relativePosition, "RELPOS");

        // TransferWalkTimes
        language.add(StandardAttributes.fromStopArea, "VONHSTBERNR");
        language.add(StandardAttributes.toStopArea, "NACHHSTBERNR");
        language.add(StandardAttributes.time, "ZEIT");
        language.add(StandardAttributes.transportSystemCode, "VSYSCODE");

        // Pt LineRoute
        language.add(StandardAttributes.lineName, "LINNAME");
        language.add(StandardAttributes.directionCode, "RICHTUNGCODE");

        // Pt LineRouteElement
        language.add(StandardAttributes.stopNumber, "HPUNKTNR");
        language.add(StandardAttributes.index, "INDEX");
        language.add(StandardAttributes.isRoutePoint, "ISTROUTENPUNKT");
        language.add(StandardAttributes.toLength, "NACHLAENGE");

        // TimeProfile
        language.add(StandardAttributes.lineRouteName, "LINROUTENAME");
        language.add(StandardAttributes.timeProfileName, "FZPROFILNAME");
        language.add(StandardAttributes.lineRouteElementIndex, "LRELEMINDEX");
        language.add(StandardAttributes.getOff, "AUS");
        language.add(StandardAttributes.board, "EIN");
        language.add(StandardAttributes.arrival, "ANKUNFT");
        language.add(StandardAttributes.departure, "ABFAHRT");

        // VehicleJourney
//        language.add(StandardAttributes.vehicleJourneyNumber, "FPLFAHRTNR");
//        language.add(StandardAttributes.fromTimeProfileElementIndex, "VONFZPELEMINDEX");
//        language.add(StandardAttributes.toTimeProfileElementIndex, "NACHFZPELEMINDEX");
//        language.add(StandardAttributes.vehicleDayNumber, "VTAGNR");
        language.add(StandardAttributes.vehicleJourneyNumber, "FPLFAHRTNR");
        language.add(StandardAttributes.fromTimeProfileElementIndex, "VONFZPELEMINDEX");
        language.add(StandardAttributes.toTimeProfileElementIndex, "NACHFZPELEMINDEX");
        language.add(StandardAttributes.validDayNumber, "VTAGNR");
        language.add(StandardAttributes.validMonday, "ISVALID(MO)");
        language.add(StandardAttributes.validTuesday, "ISVALID(DI)");
        language.add(StandardAttributes.validWednesday, "ISVALID(MI)");
        language.add(StandardAttributes.validThursday, "ISVALID(DO)");
        language.add(StandardAttributes.validFriday, "ISVALID(FR)");
        language.add(StandardAttributes.validSaturday, "ISVALID(SA)");
        language.add(StandardAttributes.validSunday, "ISVALID(SO)");


        // Point
        language.add(StandardAttributes.id, "ID");

        // IntermediatePoint
        language.add(StandardAttributes.edgeId, "KANTEID");

        // Edge
        language.add(StandardAttributes.fromPointId, "VONPUNKTID");
        language.add(StandardAttributes.toPointId, "NACHPUNKTID");

        // Surfae
        language.add(StandardAttributes.enclave, "ENKLAVE");
        language.add(StandardAttributes.ringId, "TFLAECHEID");

        // ChargingStation
        language.add(StandardAttributes.chargingStationsCode, "Ladestationen");
        language.add(StandardAttributes.chargingType, "ART");
        language.add(StandardAttributes.lsId, "LS_ID");
        language.add(StandardAttributes.latitude, "LATITUDE");
        language.add(StandardAttributes.longitude, "LONGITUDE");
        language.add(StandardAttributes.vehicleType, "FZ");
        language.add(StandardAttributes.publicType, "PUB");
        language.add(StandardAttributes.place, "ORT");
        language.add(StandardAttributes.plz, "PLZ");
        language.add(StandardAttributes.street, "STRASSE");

        // ChargingPoints
        language.add(StandardAttributes.chargingPoints, "Ladepunkte");
        language.add(StandardAttributes.power, "LEISTUNG");

        // Carsharing
        language.add(StandardAttributes.carsharingStadtmobil, "CS_SM");
        language.add(StandardAttributes.carsharingFlinkster, "CS_FL");
        language.add(StandardAttributes.objectId, "OBJECTID");
        language.add(StandardAttributes.numberOfVehicles, "ANZAHL_FZ");
        language.add(StandardAttributes.town, "STADT");
        language.add(StandardAttributes.streetIso8859, "STRA\u00DFE");

        // Territory
        language.add(StandardAttributes.item, "ITEM");
        language.add(StandardAttributes.codeLc, "CODE_LC");
        language.add(StandardAttributes.correspondingZones, "CORRESPONDINGZONES");

        language.add(Unit.velocity, "km/h");
        language.add(Unit.distance, "km");
        language.add(Unit.time, "s");
        return language;
    }
}
