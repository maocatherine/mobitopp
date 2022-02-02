package edu.kit.ifv.mobitopp.simulation;

import java.util.Optional;

import edu.kit.ifv.mobitopp.data.ZoneId;
import edu.kit.ifv.mobitopp.data.local.InMemoryMatrices;
import edu.kit.ifv.mobitopp.publictransport.connectionscan.PublicTransportRoute;
import edu.kit.ifv.mobitopp.publictransport.model.Stop;
import edu.kit.ifv.mobitopp.time.Time;

public class ImpedanceMatSimIteration implements ImpedanceIfc {

	private final ImpedanceIfc other;
	private final InMemoryMatrices travelTime;

	public ImpedanceMatSimIteration(ImpedanceIfc other, InMemoryMatrices travelTime) {
		super();
		this.other = other;
		this.travelTime = travelTime;
	}

	@Override
	public float getTravelTime(ZoneId origin, ZoneId destination, Mode mode, Time date) {
		if (usesCar(mode)) {
			return travelTime.getTravelTime(origin.getMatrixColumn(), destination.getMatrixColumn(), date);
		}
		return other.getTravelTime(origin, destination, mode, date);
	}

	private boolean usesCar(Mode mode) {
		return StandardMode.CAR.equals(mode) || StandardMode.CARSHARING_FREE.equals(mode)
				|| StandardMode.CARSHARING_STATION.equals(mode) || StandardMode.PASSENGER.equals(mode);
	}

	@Override
	public Optional<PublicTransportRoute> getPublicTransportRoute(
			Location origin, Location destination, Mode mode, Time date) {
		return other.getPublicTransportRoute(origin, destination, mode, date);
	}
	
	@Override
	public Optional<PublicTransportRoute> getPublicTransportRoute(
			Stop start, Stop end, Mode mode, Time date) {
		return other.getPublicTransportRoute(start, end, mode, date);
	}

	@Override
	public float getTravelCost(ZoneId origin, ZoneId destination, Mode mode, Time date) {
		return other.getTravelCost(origin, destination, mode, date);
	}

	@Override
	public float getDistance(ZoneId origin, ZoneId destination) {
		return other.getDistance(origin, destination);
	}

	@Override
	public float getParkingCost(ZoneId destination, Time date) {
		return other.getParkingCost(destination, date);
	}

	@Override
	public float getParkingStress(ZoneId destination, Time date) {
		return other.getParkingStress(destination, date);
	}

	@Override
	public float getConstant(ZoneId origin, ZoneId destination, Time date) {
		return other.getConstant(origin, destination, date);
	}

	@Override
	public float getOpportunities(ActivityType activityType, ZoneId zoneOid) {
		return other.getOpportunities(activityType, zoneOid);
	}

}
