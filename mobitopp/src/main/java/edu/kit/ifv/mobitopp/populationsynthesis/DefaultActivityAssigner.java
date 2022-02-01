package edu.kit.ifv.mobitopp.populationsynthesis;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.kit.ifv.mobitopp.data.PanelDataRepository;
import edu.kit.ifv.mobitopp.data.PatternActivityWeek;
import edu.kit.ifv.mobitopp.data.tourbasedactivitypattern.TourBasedActivityPattern;
import edu.kit.ifv.mobitopp.data.tourbasedactivitypattern.TourBasedActivityPatternCreator;
import edu.kit.ifv.mobitopp.util.panel.HouseholdOfPanelData;
import edu.kit.ifv.mobitopp.util.panel.HouseholdOfPanelDataId;
import edu.kit.ifv.mobitopp.util.panel.PersonOfPanelData;
import edu.kit.ifv.mobitopp.util.panel.PersonOfPanelDataId;

public class DefaultActivityAssigner implements ActivityScheduleAssigner {

    private final PanelDataRepository panelRepository;
    private final ActivityScheduleCreator scheduleCreator;
    private Map<PersonOfPanelDataId, TourBasedActivityPattern> cachedActivityPattern = new LinkedHashMap<>();

    public DefaultActivityAssigner(
            PanelDataRepository panelRepository, ActivityScheduleCreator scheduleCreator) {
        this.panelRepository = panelRepository;
        this.scheduleCreator = scheduleCreator;
    }

    private PersonOfPanelDataId createPersonPanelIdFor(
            PersonBuilder person, HouseholdOfPanelDataId householdId) {
        int personNumber = person.getId().getPersonNumber();
        return new PersonOfPanelDataId(householdId, personNumber);
    }

    @Override
    public void assignActivitySchedule(HouseholdForSetup household) {
        //Gauge the persons from given household. Each of them assign activity.
        short year = household.getId().getYear();
        long householdNumber = household.getId().getHouseholdNumber();
        HouseholdOfPanelDataId householdId = new HouseholdOfPanelDataId(year, householdNumber);
        HouseholdOfPanelData panelHousehold = panelRepository.getHousehold(householdId);
        for (PersonBuilder person : household.getPersons()) {
            doAssignActivityProgramTo(person, householdId, panelHousehold, household);
        }
    }

    private void doAssignActivityProgramTo(
            PersonBuilder person, HouseholdOfPanelDataId householdId,
            HouseholdOfPanelData panelHousehold, HouseholdForSetup household) {
        PersonOfPanelDataId personId = createPersonPanelIdFor(person, householdId);
        PersonOfPanelData panelPerson = panelRepository.getPerson(personId);

        //Activity pattern for a week. Calling the createActivitySchedule method in DefaultActivityScheduleCreator.
        PatternActivityWeek activityPattern = scheduleCreator
                .createActivitySchedule(panelPerson, panelHousehold, household);
        TourBasedActivityPattern activitySchedule;
        if (cachedActivityPattern.containsKey(panelPerson.getId())) {
            activitySchedule = cachedActivityPattern.get(panelPerson.getId());
        } else {
            activitySchedule = TourBasedActivityPatternCreator.fromPatternActivityWeek(activityPattern);
            cachedActivityPattern.put(panelPerson.getId(), activitySchedule);
        }
        person.setPatternActivityWeek(activitySchedule);
    }
}
