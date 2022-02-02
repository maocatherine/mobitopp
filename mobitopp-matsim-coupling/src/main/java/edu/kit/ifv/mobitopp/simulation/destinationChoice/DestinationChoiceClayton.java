package edu.kit.ifv.mobitopp.simulation.destinationchoice;

import java.util.*;

import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.simulation.ActivityType;
import edu.kit.ifv.mobitopp.simulation.ImpedanceIfc;
import edu.kit.ifv.mobitopp.simulation.Mode;
import edu.kit.ifv.mobitopp.simulation.Person;
import edu.kit.ifv.mobitopp.simulation.activityschedule.ActivityIfc;
import edu.kit.ifv.mobitopp.simulation.destinationChoice.DestinationChoiceModelChoiceSet;
import edu.kit.ifv.mobitopp.simulation.destinationChoice.DestinationChoiceUtilityFunction;
import edu.kit.ifv.mobitopp.simulation.destinationChoice.ReachableZonesFilter;
import edu.kit.ifv.mobitopp.simulation.modeChoice.ModeAvailabilityModel;
import edu.kit.ifv.mobitopp.time.Time;
import edu.kit.ifv.mobitopp.util.logit.DefaultLogitModel;
import edu.kit.ifv.mobitopp.util.logit.LogitModel;


public class DestinationChoiceClayton
        implements DestinationChoiceModelChoiceSet {

    protected final ImpedanceIfc impedance;
    protected final DestinationChoiceUtilityFunction utilityFunction;
    protected final ModeAvailabilityModel modeAvailabilityModel;
    protected final ReachableZonesFilter reachableZonesModel;

    protected final boolean tourBased;

    private final LogitModel<Zone> logitModel = new DefaultLogitModel<Zone>();

    public DestinationChoiceClayton(
            DestinationChoiceUtilityFunction utilityFunction,
            ModeAvailabilityModel modeAvailabilityModel,
            ImpedanceIfc impedance,
            ReachableZonesFilter reachableZonesModel,
            boolean tourBased
    ) {
        this.utilityFunction = utilityFunction;
        this.impedance = impedance;
        this.modeAvailabilityModel = modeAvailabilityModel;
        this.reachableZonesModel = reachableZonesModel;
        this.tourBased = tourBased;
    }

    @Override
    public boolean isTourBased() {
        return tourBased;
    }


    public Zone selectDestination(
            Person person,
            Optional<Mode> tourMode,
            ActivityIfc previousActivity,
            ActivityIfc nextActivity,
            Set<Zone> possibleTargetZones, double randomNumber
    ) {
        assert person != null;
        assert previousActivity != null;
        assert nextActivity != null;
        assert !nextActivity.activityType().isFixedActivity();
        assert possibleTargetZones.size() > 0;

        final double MAX_TRAVELDISTANCE = 6;

        ActivityType activityType = nextActivity.activityType();

        Zone currentZone = previousActivity.zone();
        Zone nextFixedZone = person.nextFixedActivityZone(person.currentActivity());

        Set<Mode> availableModes = this.modeAvailabilityModel.availableModes(
                person,
                currentZone,
                previousActivity
        );
        Time startDate = previousActivity.calculatePlannedEndDate();
        Collection<Zone> filteredTargetZones = this.reachableZonesModel.filter(possibleTargetZones,
                person,
                currentZone,
                nextFixedZone,
                availableModes,
                startDate,
                MAX_TRAVELDISTANCE);

        Map<Zone, Set<Mode>> availableZones = new LinkedHashMap<Zone, Set<Mode>>();

        for (Zone zone : filteredTargetZones) {
            availableZones.put(zone, availableModes);
        }

        Map<Zone, Double> utilities = utilityFunction.calculateUtilities(
                person,
                nextActivity,
                currentZone,
                availableZones,
                activityType
        );

        return logitModel.select(utilities, randomNumber);
    }

}

