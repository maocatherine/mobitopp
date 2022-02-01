package edu.kit.ifv.mobitopp.populationsynthesis.opportunities;

import java.awt.geom.Point2D;

import edu.kit.ifv.mobitopp.data.ZoneId;
import edu.kit.ifv.mobitopp.data.ZoneRepository;
import edu.kit.ifv.mobitopp.simulation.ActivityType;
import edu.kit.ifv.mobitopp.simulation.Location;
import edu.kit.ifv.mobitopp.simulation.opportunities.Opportunity;
import edu.kit.ifv.mobitopp.util.dataimport.Row;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PoiParser {

	private static final String zoneId = "zoneId";
	private static final String activityType = "activityType";
	private static final String locationX = "locationX";
	private static final String locationY = "locationY";
	private static final String attractivity = "attractivity";

	private final ZoneRepository zoneRepository;
	private final RoadLocator roadLocator;

	public Opportunity parse(Row row) {
		ZoneId zone = zoneOf(row);
		ActivityType activityType = activityTypeOf(row);
		Location location = locationOf(row, zone);
		int attractivity = attractivityOf(row);
		return new Opportunity(zone, activityType, location, attractivity);
	}

	private ZoneId zoneOf(Row row) {
		return zoneRepository.getId(row.get(zoneId));
	}

	private ActivityType activityTypeOf(Row row) {
		return ActivityType.getTypeFromInt(row.valueAsInteger(activityType));
	}

	private Location locationOf(Row row, ZoneId zone) {
		double x = row.valueAsDouble(locationX);
		double y = row.valueAsDouble(locationY);
		Point2D.Double coordinate = new Point2D.Double(x, y);
		RoadPosition position = roadLocator.getRoadPosition(zone, coordinate);
		int link = position.getLink();
		double roadPosition = position.getPosition();
		return new Location(coordinate, link, roadPosition);
	}

	private int attractivityOf(Row row) {
		float attractivityFloat = row.valueAsFloat(attractivity);
		int attractivityInt = Math.round(attractivityFloat);
		return attractivityInt;
	}

}
