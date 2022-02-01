package edu.kit.ifv.mobitopp.simulation.destinationAndModeChoice;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.simulation.ActivityType;
import edu.kit.ifv.mobitopp.simulation.Mode;
import edu.kit.ifv.mobitopp.simulation.Person;
import edu.kit.ifv.mobitopp.simulation.activityschedule.ActivityIfc;
import edu.kit.ifv.mobitopp.simulation.destinationChoice.DestinationChoiceModelChoiceSet;
import edu.kit.ifv.mobitopp.simulation.modeChoice.ModeAvailabilityModel;
import edu.kit.ifv.mobitopp.simulation.modeChoice.ModeChoiceModel;
import edu.kit.ifv.mobitopp.util.logit.DefaultLogitModel;
import edu.kit.ifv.mobitopp.util.logit.LogitModel;

public class DestinationAndModeChoiceSchaufenster
        implements DestinationChoiceModelChoiceSet
        , ModeChoiceModel {

    private final Map<ActivityType, DestinationAndModeChoiceUtility> utilityFunctions;

    private final ModeAvailabilityModel modeAvailabilityModel;

    private final LogitModel<Mode> modeChoiceModel = new DefaultLogitModel<Mode>();
    private final LogitModel<Zone> destinationChoiceModel = new DefaultLogitModel<Zone>();

    public DestinationAndModeChoiceSchaufenster(
            ModeAvailabilityModel modeAvailabilityModel,
            Map<ActivityType, DestinationAndModeChoiceUtility> utilityFunctions
    ) {
        this.utilityFunctions = Collections.unmodifiableMap(utilityFunctions);

        this.modeAvailabilityModel = modeAvailabilityModel;
    }

    public Zone selectDestination(
            Person person,
            Optional<Mode> tourMode,
            ActivityIfc previousActivity,
            ActivityIfc nextActivity,
            Set<Zone> choiceSet, double randomNumber
    ) {

        Set<Zone> zonesChoiceSet = new LinkedHashSet<Zone>(choiceSet);

        ActivityType activityType = nextActivity.activityType();

        assert previousActivity.isLocationSet();

        Zone source = previousActivity.zone();


        DestinationAndModeChoiceUtility utilityFunction = utilityFunctions.get(activityType);

        assert utilityFunction != null : activityType;

        Collection<Mode> availableModes = this.modeAvailabilityModel.availableModes(person, source, previousActivity);

        Map<Zone, Set<Mode>> availableModesPerZone = availableModesForDestination(zonesChoiceSet, availableModes,
                person, previousActivity, nextActivity, source);


        Map<Zone, Double> utilitiesZone = utilityFunction.calculateUtilitiesForUpperModel(
                zonesChoiceSet,
                person,
                previousActivity,
                nextActivity,
                source,
                availableModesPerZone
        );

        assert !utilitiesZone.isEmpty() : (activityType + "\n" + choiceSet + "\n" + availableModes);

        return destinationChoiceModel.select(utilitiesZone, randomNumber);
    }

    private Map<Zone, Set<Mode>> availableModesForDestination(
            Collection<Zone> zonesChoiceSet,
            Collection<Mode> availableModes,
            Person person,
            ActivityIfc previousActivity,
            ActivityIfc nextActivity,
            Zone currentZone
    ) {

        Map<Zone, Set<Mode>> resultingModes = new LinkedHashMap<Zone, Set<Mode>>();
        Map<Zone, Set<Mode>> surrogateModes = new LinkedHashMap<Zone, Set<Mode>>();

        int reachableZones = 0;

        for (Zone possibleDestination : zonesChoiceSet) {

            Set<Mode> modes = modeAvailabilityModel.filterAvailableModes(
                    person,
                    currentZone,
                    possibleDestination,
                    previousActivity,
                    nextActivity,
                    availableModes
            );

            Set<Mode> filteredModes = modeAvailabilityModel.modesWithReasonableTravelTime(
                    person,
                    currentZone,
                    possibleDestination,
                    previousActivity,
                    nextActivity,
                    modes,
                    false
            );

            if (!filteredModes.isEmpty()) {
                reachableZones++;
            }

            if (filteredModes.isEmpty()) {

                Set<Mode> surrogateMode = modeAvailabilityModel.modesWithReasonableTravelTime(
                        person,
                        currentZone,
                        possibleDestination,
                        previousActivity,
                        nextActivity,
                        modes,
                        true
                );

                assert !surrogateMode.isEmpty();

                surrogateModes.put(possibleDestination, surrogateMode);
            }

            resultingModes.put(possibleDestination, filteredModes);
        }

        return (reachableZones > 0) ? resultingModes : surrogateModes;
    }


    public Mode selectMode(
            Person person,
            Zone source,
            Zone destination,
            ActivityIfc previousActivity,
            ActivityIfc nextActivity,
            Set<Mode> choiceSet,
            double randomNumber
    ) {
        ActivityType activityType = nextActivity.activityType();

        DestinationAndModeChoiceUtility utilityFunction = utilityFunctions.get(activityType);

        assert utilityFunction != null : activityType;

        Set<Mode> availableModes = modeAvailabilityModel.filterAvailableModes(person, source,
                destination, previousActivity, nextActivity, choiceSet);

        Set<Mode> filteredModes = modeAvailabilityModel.modesWithReasonableTravelTime(
                person,
                source,
                destination,
                previousActivity,
                nextActivity,
                availableModes,
                true
        );

        assert !filteredModes.isEmpty();

        Map<Mode, Double> utilitiesMode = utilityFunction.calculateUtilitiesForLowerModel(
                filteredModes,
                person,
                previousActivity,
                nextActivity,
                source,
                destination
        );


        return modeChoiceModel.select(utilitiesMode, randomNumber);

    }


}
