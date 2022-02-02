package edu.kit.ifv.mobitopp.matsim.converter;

import java.util.HashSet;
import java.util.Set;

import edu.kit.ifv.mobitopp.visum.VisumRoadNetwork;
import edu.kit.ifv.mobitopp.visum.VisumTransportSystem;
import edu.kit.ifv.mobitopp.visum.VisumTransportSystemSet;

public class MatsimModeConverter {

	private final VisumRoadNetwork visum;

	public MatsimModeConverter(VisumRoadNetwork visum) {
		super();
		this.visum = visum;
	}

	public Set<String> from(VisumTransportSystemSet tss) {
		Set<VisumTransportSystem> transport = tss.transportSystems;

		Set<String> result = new HashSet<String>();

		if (isCar(transport)) {
			result.add("car");
		}
		if (isPublicTransport(transport)) {
			result.add("pt");
		}
		if (containsTransportSystem(transport, "BIKE")) {
			result.add("bike");
		}
		if (containsTransportSystem(transport, "PED")) {
			result.add("walk");
		}
		if (containsTransportSystem(transport, "PUTW")) {
			result.add("transit_walk");
		}
		return result;
	}

	public boolean isCar(Set<VisumTransportSystem> transport) {
		return containsTransportSystem(transport, "CAR")
				||	containsTransportSystem(transport, "HGV");
	}

	private boolean isPublicTransport(Set<VisumTransportSystem> transport) {
		return containsTransportSystem(transport, "BUS")
				|| containsTransportSystem(transport, "RAIL")
				|| containsTransportSystem(transport, "SUB")
				|| containsTransportSystem(transport, "TRAM")
				|| containsTransportSystem(transport, "LR");
	}

	private boolean containsTransportSystem(Set<VisumTransportSystem> transport, String code) {
		return visum.containsTransportSystem(code) && transport.contains(visum.getTransportSystem(code));
	}
}
