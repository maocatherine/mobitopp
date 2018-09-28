package edu.kit.ifv.mobitopp.populationsynthesis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import edu.kit.ifv.mobitopp.data.PatternActivity;
import edu.kit.ifv.mobitopp.data.PatternActivityWeek;
import edu.kit.ifv.mobitopp.data.local.ExampleHouseholdOfPanelData;
import edu.kit.ifv.mobitopp.data.local.ExamplePersonOfPanelData;
import edu.kit.ifv.mobitopp.simulation.ActivityType;
import edu.kit.ifv.mobitopp.simulation.Household;
import edu.kit.ifv.mobitopp.time.DayOfWeek;
import edu.kit.ifv.mobitopp.util.panel.ActivityOfPanelData;
import edu.kit.ifv.mobitopp.util.panel.HouseholdOfPanelData;
import edu.kit.ifv.mobitopp.util.panel.PersonOfPanelData;

public class DefaultActivityScheduleCreatorTest {

	private static final int tripDuration = 1;
	private static final int duration = 2;
	private static final int startTime = 3;
	private HouseholdOfPanelData unusedPanelHousehold;
	private Household unusedHousehold;
	private ActivityOfPanelData work;
	private ActivityOfPanelData home;
	private DefaultActivityScheduleCreator creator;

	@Before
	public void initialise() {
		unusedPanelHousehold = ExampleHouseholdOfPanelData.household;
		unusedHousehold = mock(Household.class);
		work = new ActivityOfPanelData(tripDuration, ActivityType.WORK, duration, startTime);
		home = new ActivityOfPanelData(tripDuration, ActivityType.HOME, duration, startTime);

		creator = new DefaultActivityScheduleCreator();
	}

	@Test
	public void ensureFirstActivityIsAtHome() {
		String pattern = missingHomeAtStart();
		PatternActivityWeek activities = createActivitiesFrom(pattern);

		assertThat(activities.first().getActivityType(), is(equalTo(ActivityType.HOME)));
	}

	private String missingHomeAtStart() {
		return work.asCSV() + ";" + home.asCSV();
	}

	@Test
	public void ensureLastActivityIsAtHome() {
		String pattern = missingHomeAtEnd();

		PatternActivityWeek activities = createActivitiesFrom(pattern);

		assertThat(activities.last().getActivityType(), is(equalTo(ActivityType.HOME)));
	}

	private String missingHomeAtEnd() {
		return home.asCSV() + ";" + work.asCSV();
	}

	@Test
	public void useCachedActivities() {
		String pattern = correctTour();

		PatternActivityWeek activities = createActivitiesFrom(pattern);
		PatternActivityWeek cachedActivities = createActivitiesFrom(pattern);

		PatternActivityWeek completeTour = createCompleteTour();
		assertThat(activities, is(equalTo(completeTour)));
		assertThat(activities, sameInstance(cachedActivities));
	}

	private PatternActivityWeek createCompleteTour() {
		PatternActivityWeek week = new PatternActivityWeek();
		week.addPatternActivity(createActivityFor(ActivityType.HOME));
		week.addPatternActivity(createActivityFor(ActivityType.WORK));
		week.addPatternActivity(createActivityFor(ActivityType.HOME));
		return week;
	}

	private PatternActivity createActivityFor(ActivityType type) {
		return new PatternActivity(type, DayOfWeek.MONDAY, tripDuration, startTime, duration);
	}

	private String correctTour() {
		return home.asCSV() + ";" + work.asCSV() + ";" + home.asCSV();
	}

	private PatternActivityWeek createActivitiesFrom(String pattern) {
		PersonOfPanelData panelPerson = ExamplePersonOfPanelData.personWithPattern(pattern);
		return creator.createActivitySchedule(panelPerson, unusedPanelHousehold, unusedHousehold);
	}

}