package edu.kit.ifv.mobitopp.simulation.modeChoice;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.data.ZoneId;
import edu.kit.ifv.mobitopp.simulation.Car;
import edu.kit.ifv.mobitopp.simulation.Household;
import edu.kit.ifv.mobitopp.simulation.ImpedanceIfc;
import edu.kit.ifv.mobitopp.simulation.Mode;
import edu.kit.ifv.mobitopp.simulation.Person;
import edu.kit.ifv.mobitopp.simulation.StandardChoiceSet;
import edu.kit.ifv.mobitopp.simulation.StandardMode;
import edu.kit.ifv.mobitopp.simulation.activityschedule.ActivityIfc;
import edu.kit.ifv.mobitopp.time.Time;


public class BasicModeAvailabilityModel
        implements ModeAvailabilityModel {
    private static final float METER_TO_KM = 1; //1e-3f;

    protected final ImpedanceIfc impedance;
    private float distanceToKmFactor;

    public BasicModeAvailabilityModel(
            final ImpedanceIfc impedance, final float distanceToKmFactor
    ) {
        this.impedance = impedance;
        this.distanceToKmFactor = distanceToKmFactor;
    }

    public BasicModeAvailabilityModel(final ImpedanceIfc impedance) {
        this(impedance, METER_TO_KM);
    }

    public Set<Mode> availableModes(
            Person person,
            Zone currentZone,
            ActivityIfc previousActivity
    ) {
        return availableModes(person, currentZone, previousActivity, StandardChoiceSet.CHOICE_SET_WITHOUT_PT);
    }

    public Set<Mode> availableModes(
            Person person,
            Zone currentZone,
            ActivityIfc previousActivity,
            Collection<Mode> allModes
    ) {

        Set<Mode> choiceSet = new LinkedHashSet<Mode>(allModes);
        choiceSet.remove(StandardMode.CARSHARING_FREE);
        choiceSet.remove(StandardMode.CARSHARING_STATION);


        if (isAtHome(previousActivity)) {
            if (!carAvailable(person)) {
                choiceSet.remove(StandardMode.CAR);
            }

            if (!person.hasBike()) {
                choiceSet.remove(StandardMode.BIKE);
            }

        } else {

            Mode previousMode = previousActivity.mode().mainMode();

            if (previousMode.isFlexible()) {
                choiceSet.removeAll(StandardChoiceSet.FIXED_MODES);
            } else {
                choiceSet.clear();
                choiceSet.add(previousMode);
            }
        }

        return choiceSet;

    }

    //Here we remove walking as the mode if distance is more than 5km.
    public Set<Mode> filterAvailableModes(
            Person person,
            Zone origin,
            Zone destination,
            ActivityIfc previousActivity,
            ActivityIfc nextActivity,
            Collection<Mode> proposedChoiceSet
    ) {

        //final float DIVERSION_FACTOR = 1.5f;
        final float MAX_WALK_DISTANCE = 3.0f;

        Set<Mode> choiceSet = availableModes(person, origin, previousActivity, proposedChoiceSet);

//        if (choiceSet.contains(StandardMode.CAR)) {

        if (isAtHome(previousActivity)) {

            ZoneId originId = origin.getId();
            ZoneId destinationId = destination.getId();
            ZoneId nextPole = person.nextFixedActivityZone(person.currentActivity()).getId();
            ZoneId homeZone = person.homeZone().getId();

            float distance = this.impedance.getDistance(originId, destinationId)
                    + this.impedance.getDistance(destinationId, nextPole)
                    + this.impedance.getDistance(nextPole, homeZone);
            float distanceKm = distance * distanceToKmFactor;
//
//          Car car = person.household().nextAvailableCar(person, distanceKm);
//
//          float range = car.effectiveRange();

            if (distanceKm > MAX_WALK_DISTANCE) {
                choiceSet.remove(StandardMode.PEDESTRIAN);
            }
        }
//        }

        return choiceSet;
    }

    public Set<Mode> modesWithReasonableTravelTime(
            Person person,
            Zone origin,
            Zone destination,
            ActivityIfc previousActivity,
            ActivityIfc nextActivity,
            Collection<Mode> possibleModes,
            boolean keepAtLeastOne
    ) {

        final int MAX_TRAVELTIME = 60 * 24;

        assert !possibleModes.isEmpty();

        Mode fastestMode = null;
        double fastestTravelTime = Double.POSITIVE_INFINITY;

        Time date = previousActivity.calculatePlannedEndDate();

        Set<Mode> reasonableModes = new LinkedHashSet<Mode>();

        for (Mode mode : possibleModes) {

            ZoneId originId = origin.getId();
            ZoneId destinationId = destination.getId();
            double time = this.impedance.getTravelTime(originId, destinationId, mode, date);

            if (time <= fastestTravelTime) {

                if (time < fastestTravelTime || fastestMode == StandardMode.PASSENGER) {
                    fastestMode = mode;
                }

                fastestTravelTime = time;
            }

            if (time < MAX_TRAVELTIME) {
                reasonableModes.add(mode);
            }
        }

        assert fastestMode != null;

        if (keepAtLeastOne && reasonableModes.isEmpty()) {
            reasonableModes.add(fastestMode);
        }

        return reasonableModes;
    }

    protected boolean isAtHome(
            ActivityIfc previousActivity
    ) {

        return previousActivity.activityType().isHomeActivity();
    }

    protected boolean carAvailable(
            Person person
    ) {
        Household theHousehold = person.household();

        return person.hasDrivingLicense()
                && theHousehold.getNumberOfAvailableCars() > 0;
    }

    protected Set<Mode> singleChoice(Mode mode) {
        LinkedHashSet<Mode> choiceSet = new LinkedHashSet<>();
        choiceSet.add(mode);
        return choiceSet;
    }

    protected boolean previousModeIsFixed(Set<Mode> filteredModes) {
        return 1 == filteredModes.size();
    }


}
