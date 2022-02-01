package edu.kit.ifv.mobitopp.routing;

import edu.kit.ifv.mobitopp.visum.VisumPoint2;
import edu.kit.ifv.mobitopp.visum.VisumZone;

public class NodeFromVisumZone extends DefaultNode implements Node {

    private static final int externalZoneType = 5;

    private final VisumZone visumZone;
    private final boolean isSink;


    public NodeFromVisumZone(VisumZone zone) {
        this(zone, true);
    }

    private NodeFromVisumZone(VisumZone zone, boolean isSink) {
        super(String.valueOf(zone.id));
        this.visumZone = zone;
        this.isSink = isSink;
    }

    public static Node useExternalInRouteSearch(VisumZone zone) {
        return new NodeFromVisumZone(zone, isBlockedInRouteSearch(zone));
    }

    private static boolean isBlockedInRouteSearch(VisumZone zone) {
        return externalZoneType != zone.type;
    }

    public VisumPoint2 coord() {
        return visumZone.coord;
    }

    @Override
    public boolean isSink() {
        return isSink;
    }

    public boolean isTarget() {
        return false;
    }

}
