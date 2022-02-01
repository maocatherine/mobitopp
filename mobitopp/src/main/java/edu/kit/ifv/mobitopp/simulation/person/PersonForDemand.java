package edu.kit.ifv.mobitopp.simulation.person;

import static edu.kit.ifv.mobitopp.util.collections.StreamUtils.warn;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import edu.kit.ifv.mobitopp.data.PatternActivityWeek;
import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.data.person.PersonId;
import edu.kit.ifv.mobitopp.data.tourbasedactivitypattern.TourBasedActivityPattern;
import edu.kit.ifv.mobitopp.populationsynthesis.FixedDestinations;
import edu.kit.ifv.mobitopp.simulation.ActivityType;
import edu.kit.ifv.mobitopp.simulation.Car;
import edu.kit.ifv.mobitopp.simulation.Employment;
import edu.kit.ifv.mobitopp.simulation.FixedDestination;
import edu.kit.ifv.mobitopp.simulation.Gender;
import edu.kit.ifv.mobitopp.simulation.Graduation;
import edu.kit.ifv.mobitopp.simulation.Household;
import edu.kit.ifv.mobitopp.simulation.ImpedanceIfc;
import edu.kit.ifv.mobitopp.simulation.Location;
import edu.kit.ifv.mobitopp.simulation.ModifiableActivityScheduleWithState;
import edu.kit.ifv.mobitopp.simulation.Person;
import edu.kit.ifv.mobitopp.simulation.PersonAttributes;
import edu.kit.ifv.mobitopp.simulation.ReschedulingStrategy;
import edu.kit.ifv.mobitopp.simulation.Trip;
import edu.kit.ifv.mobitopp.simulation.activityschedule.ActivityIfc;
import edu.kit.ifv.mobitopp.simulation.activityschedule.ActivityPeriodFixer;
import edu.kit.ifv.mobitopp.simulation.activityschedule.ActivitySchedule;
import edu.kit.ifv.mobitopp.simulation.activityschedule.ActivityScheduleWithState;
import edu.kit.ifv.mobitopp.simulation.activityschedule.DefaultActivitySchedule;
import edu.kit.ifv.mobitopp.simulation.activityschedule.randomizer.ActivityStartAndDurationRandomizer;
import edu.kit.ifv.mobitopp.simulation.bikesharing.Bike;
import edu.kit.ifv.mobitopp.simulation.car.PrivateCar;
import edu.kit.ifv.mobitopp.simulation.modeChoice.ModeChoicePreferences;
import edu.kit.ifv.mobitopp.simulation.tour.TourFactory;
import edu.kit.ifv.mobitopp.time.Time;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonForDemand implements Person, Serializable {

    private static final long serialVersionUID = 2936969065679820132L;

    private final Household household;

    private final PersonId id;

    private final short age;

    private final Employment employment;
    private final Gender gender;
    private final Graduation graduation;

    private final int income;

    private final boolean hasBike;
    private final boolean hasAccessToCar;
    private final boolean hasPersonalCar;
    private final boolean hasCommuterTicket;
    private final boolean hasLicense;

    private final Map<String, Boolean> mobilityProviderCustomership;
    private final ModeChoicePreferences modeChoicePrefsSurvey;
    private final ModeChoicePreferences modeChoicePreferences;
    private final ModeChoicePreferences travelTimeSensitivity;

    private final FixedDestinations fixedDestinations;

    /**
     * Planned activity program
     **/
    //private PatternActivityWeek activityPattern;
    private TourBasedActivityPattern tourPattern;

    /**
     * Realised activity program
     **/
    private transient ModifiableActivityScheduleWithState activitySchedule;

    private transient Trip currentTrip;

    private PrivateCar personalCar = null;

    private enum CarUsage {NONE, DRIVER, PASSENGER, PARKED}

    private enum BikeUsage {NONE, DRIVER, PARKED}

    private Car car;
    private CarUsage currentCarUsage = CarUsage.NONE;

    private Bike bike;
    private BikeUsage currentBikeUsage;

    public PersonForDemand(
            PersonId id,
            Household household,
            int age,
            Employment employment,
            Gender gender,
            Graduation graduation,
            int income,
            boolean hasBike,
            boolean hasAccessToCar,
            boolean hasPersonalCar,
            boolean hasCommuterTicket,
            boolean hasLicense,
            TourBasedActivityPattern tourPattern,
            FixedDestinations fixedDestinations,
            Map<String, Boolean> mobilityProviderCustomership,
            ModeChoicePreferences modeChoicePrefsSurvey,
            ModeChoicePreferences modeChoicePreferences,
            ModeChoicePreferences travelTimeSensitivity
    ) {
        this.id = id;

        this.household = household;
        this.tourPattern = tourPattern;

        this.age = (short) age;

        this.employment = employment;
        this.gender = gender;
        this.graduation = graduation;

        this.income = income;

        this.hasBike = hasBike;
        this.hasAccessToCar = hasAccessToCar;
        this.hasPersonalCar = hasPersonalCar;
        this.hasCommuterTicket = hasCommuterTicket;
        this.hasLicense = hasLicense;

        this.fixedDestinations = fixedDestinations;

        this.mobilityProviderCustomership = Collections.unmodifiableMap(mobilityProviderCustomership);
        this.modeChoicePrefsSurvey = modeChoicePrefsSurvey;
        this.modeChoicePreferences = modeChoicePreferences;
        this.travelTimeSensitivity = travelTimeSensitivity;

    }

    public int getOid() {
        return id.getOid();
    }

    @Override
    public boolean isCarDriver() {

        return this.currentCarUsage == CarUsage.DRIVER;
    }

    @Override
    public boolean isCarPassenger() {

        return this.currentCarUsage == CarUsage.PASSENGER;
    }

    private boolean isUsingCar() {
        return isCarDriver() || isCarPassenger() || hasParkedCar();
    }

    @Override
    public Car whichCar() {
        assert isUsingCar();

        return this.car;
    }

    @Override
    public void useCar(Car car, Time time) {

        assert car != null;
        assert !car.isUsed();
        assert !isUsingCar() : forDebug();

        this.car = car;
        this.currentCarUsage = CarUsage.DRIVER;
        car.use(this, time);
    }

    @Override
    public void useCarAsPassenger(Car car) {
        assert car != null;
        assert car.canCarryPassengers(); // car needs a driver
        assert !isUsingCar() : ("car = " + this.car);

        this.car = car;
        this.currentCarUsage = CarUsage.PASSENGER;
        car.useAsPassenger(this);
    }

    @Override
    public void leaveCar() {

        Car car = this.car;

        this.car = null;
        this.currentCarUsage = CarUsage.NONE;
        car.leave(this);
    }

    @Override
    public Car releaseCar(Time time) {

        assert this.car != null : (nextActivity().mode() + ": " + getOid());

        Car car = this.car;

        this.car = null;
        this.currentCarUsage = CarUsage.NONE;
        car.release(this, time);

        return car;
    }

    @Override
    public Car parkCar(Zone zone, Location location, Time time) {

        assert this.car != null;

        this.currentCarUsage = CarUsage.PARKED;

        return this.car;
    }

    @Override
    public boolean hasParkedCar() {

        return this.currentCarUsage == CarUsage.PARKED;
    }

    @Override
    public void takeCarFromParking() {

        assert this.car != null;

        this.currentCarUsage = CarUsage.DRIVER;
    }

    @Override
    public boolean isCycling() {
        return null != bike && this.currentBikeUsage == BikeUsage.DRIVER;
    }

    @Override
    public boolean hasParkedBike() {
        return null != bike && currentBikeUsage == BikeUsage.PARKED;
    }

    @Override
    public void useBike(Bike bike, Time time) {
        Objects.requireNonNull(bike);
        this.currentBikeUsage = BikeUsage.DRIVER;
        this.bike = bike;
        bike.use(this, time);
    }

    @Override
    public Bike whichBike() {
        return bike;
    }

    @Override
    public Bike parkBike(Zone zone, Location location, Time time) {
        if (!isCycling()) {
            throw warn(new IllegalStateException(
                    "There is no bike to park available. The person first needs to use a bike: " + this), log);
        }
        this.currentBikeUsage = BikeUsage.PARKED;
        return this.bike;
    }

    @Override
    public void takeBikeFromParking() {
        if (!hasParkedBike()) {
            throw warn(new IllegalStateException(
                    "There is no parked bike available. The person first needs to park a bike: " + this), log);
        }
        this.currentBikeUsage = BikeUsage.DRIVER;
    }

    @Override
    public Bike releaseBike(Time time) {
        if (!isCycling()) {
            throw warn(new IllegalStateException(
                    "There is no bike in use. The person first needs to use a bike: " + this), log);
        }
        Bike bike = this.bike;
        this.bike = null;
        this.currentBikeUsage = BikeUsage.NONE;
        bike.release(this, time);
        return bike;
    }

    @Override
    public boolean isMobilityProviderCustomer(String company) {
        return mobilityProviderCustomership.getOrDefault(company, warn(company, "'is mobility provider customer'", false, log));
    }

    public Map<String, Boolean> mobilityProviderCustomership() {
        return mobilityProviderCustomership;
    }

    public Household household() {

        return this.household;
    }

    public boolean hasBike() {
        return this.hasBike;
    }

    public boolean hasAccessToCar() {
        return this.hasAccessToCar;
    }

    public boolean hasPersonalCar() {
        return this.hasPersonalCar;
    }

    public boolean hasCommuterTicket() {
        return this.hasCommuterTicket;
    }

    public boolean hasDrivingLicense() {
        return hasLicense;
    }


    public int age() {
        return this.age;
    }

    public PersonId getId() {
        assert this.id != null;

        return this.id;
    }

    public Employment employment() {
        return this.employment;
    }

    public Gender gender() {
        return this.gender;
    }

    public ActivityScheduleWithState activitySchedule() {
        return this.activitySchedule;
    }

    public int getIncome() {
        return this.income;
    }

    @Override
    public Graduation graduation() {
        return graduation;
    }

    public Zone homeZone() {

        return this.household.homeZone();
    }

    private NoSuchElementException missingDestination(ActivityType activityType) {
        return new NoSuchElementException("No destination available for activity type: " + activityType);
    }

    public Location fixedDestinationFor(ActivityType activityType) {
        return this.fixedDestinations
                .getDestination(activityType)
                .map(FixedDestination::location)
                .orElseThrow(() -> warn(missingDestination(activityType), log));
    }


    public Zone fixedZoneFor(ActivityType activityType) {
        return this.fixedDestinations
                .getDestination(activityType)
                .map(FixedDestination::zone)
                .orElseThrow(() -> warn(missingDestination(activityType), log));
    }

    public boolean hasFixedZoneFor(ActivityType activityType) {
        return this.fixedDestinations.hasDestination(activityType);
    }

    public Zone fixedActivityZone() {
        return fixedDestinations
                .getFixedDestination()
                .map(FixedDestination::zone)
                .orElseGet(() -> warn(this, "fixed activity zone", household().homeZone(), log));
    }

    public boolean hasFixedActivityZone() {
        return fixedDestinations.hasFixedDestination();
    }

    public Zone nextFixedActivityZone(
            ActivityIfc activity
    ) {
        ActivityIfc nextActivity = activitySchedule().nextActivity(activity);
        while (nextActivity != null && !nextActivity.activityType().isFixedActivity()) {
            nextActivity = activitySchedule().nextActivity(nextActivity);
        }
        if (nextActivity == null || nextActivity.activityType().isHomeActivity()) {
            return household().homeZone();
        } else {
            return fixedZoneFor(nextActivity);
        }
    }

    private Zone fixedZoneFor(ActivityIfc act) {
        assert this.fixedDestinations.hasDestination(act.activityType());
        return this.fixedDestinations
                .getDestination(act.activityType())
                .map(FixedDestination::zone)
                .orElseThrow(() -> missingDestination(act.activityType()));
    }

    public ActivityIfc currentActivity() {
        return this.activitySchedule.currentActivity();
    }


    public void startActivity(
            Time currentDate,
            ActivityIfc activity,
            Trip precedingTrip,
            ReschedulingStrategy rescheduling
    ) {

        Time plannedStartDate = activity.startDate();
        activity.setStartDate(currentDate);

        rescheduling.adjustSchedule(this.activitySchedule, activity, plannedStartDate, currentDate);

        assert activity != null;

        activity.setRunning(true);
        activity.setMode(precedingTrip.mode());
        this.activitySchedule.startActivity(activity);
    }

    public ActivityIfc nextActivity() {
        assert currentTrip() != null;

        return currentTrip().nextActivity();
    }

    public ActivityIfc nextHomeActivity() {
        assert currentActivity() != null;
        ActivityIfc homeActivity = this.activitySchedule.nextHomeActivity(currentActivity());

        assert homeActivity.activityType().isHomeActivity();

        return homeActivity;
    }

    public Trip currentTrip() {
        return this.currentTrip;
    }

    public void currentTrip(Trip trip) {
        this.currentTrip = trip;
    }

    public void initSchedule(
            TourFactory tourFactory, ActivityPeriodFixer fixer,
            ActivityStartAndDurationRandomizer activityDurationRandomizer,
            List<Time> days
    ) {
        this.activitySchedule = new DefaultActivitySchedule(tourFactory, fixer,
//																												this.activityPattern, 
                getPatternActivityWeek(),
                activityDurationRandomizer, days);
        clearTourPattern();
    }

    private void clearTourPattern() {
        tourPattern = null;
    }

    private PatternActivityWeek getPatternActivityWeek() {
        assert tourPattern != null;

        return new PatternActivityWeek(this.tourPattern.asPatternActivities());
    }

    public void assignPersonalCar(PrivateCar personalCar) {
        assert this.personalCar == null;

        this.personalCar = personalCar;
    }

    public PrivateCar personalCar() {
        assert this.personalCar != null;

        return this.personalCar;
    }

    public boolean hasPersonalCarAssigned() {

        return personalCar != null;
    }

    public boolean isFemale() {
        return this.gender == Gender.FEMALE;
    }

    public boolean isMale() {
        return this.gender == Gender.MALE;
    }

    public boolean isCarSharingMember() {
        return false;
    }


    public String forLogging(ImpedanceIfc impedance) {

        Household household = household();
        Zone homeZone = homeZone();

        DecimalFormat outFormat = new DecimalFormat("#####0");

        int targetZoneOidForWork = -1;
        int targetZoneOidForEducation = -1;

        if (hasFixedZoneFor(ActivityType.WORK)) {
            targetZoneOidForWork = fixedZoneFor(ActivityType.WORK).getId().getMatrixColumn();
        }
        if (hasFixedZoneFor(ActivityType.EDUCATION)) {
            targetZoneOidForEducation = fixedZoneFor(ActivityType.EDUCATION).getId().getMatrixColumn();
        }

        int commutationTicket = hasCommuterTicket() ? 1 : 0;
        int drivingLicense = age() >= 17 && (hasAccessToCar() || hasPersonalCar()) ? 1 : 0;

        int employmentType = employment().getTypeAsInt();

        StringBuffer buffer = new StringBuffer();

        buffer.append("P; ");
        buffer.append(household.getOid()).append("; ");
        buffer.append(household.getId().getHouseholdNumber()).append("; ");
        buffer.append(household.getId().getYear()).append("; ");
        buffer.append(getOid()).append("; ");
        buffer.append(getId().getPersonNumber()).append("; ");
        buffer.append(gender().getTypeAsInt()).append("; ");
        buffer.append(age()).append("; ");
        buffer.append(employmentType + "; ");
        buffer.append(homeZone.getId().getExternalId() + "; ");

        if (targetZoneOidForWork > -1) {

            Zone workZone = fixedZoneFor(ActivityType.WORK);
            Location workLocation = fixedDestinationFor(ActivityType.WORK);

            int distance = (int) impedance.getDistance(homeZone.getId(), workZone.getId());

            buffer.append(outFormat.format(distance) + "; ");
            buffer.append(workZone.getId().getExternalId() + "; ");
            buffer.append(workLocation + "; ");
        } else {
            buffer.append("-1; ");
            buffer.append("-1; ");
            buffer.append("-1; ");
        }


        if (targetZoneOidForEducation > -1) {

            Zone eduZone = fixedZoneFor(ActivityType.EDUCATION);
            Location eduLocation = fixedDestinationFor(ActivityType.EDUCATION);

            int distance = (int) impedance.getDistance(homeZone.getId(), eduZone.getId());

            buffer.append(outFormat.format(distance) + "; ");

            buffer.append(eduZone.getId().getExternalId() + "; ");
            buffer.append(eduLocation + "; ");
        } else {
            buffer.append("-1; ");
            buffer.append("-1; ");
            buffer.append("-1; ");
        }
        buffer.append(commutationTicket + "; ");
        buffer.append(drivingLicense + "; ");

        return buffer.toString();
    }

    @Override
    public PersonAttributes attributes() {
        return new PersonAttributes(id, household, age, employment, gender, income, hasBike,
                hasAccessToCar, hasPersonalCar, hasCommuterTicket, hasLicense);
    }

    public String forDebug() {

        String debug = "\nDEBUG PersonForDemand:"
                + "\noid=" + id.getOid()
                + "\ncurrentCarUsage=" + currentCarUsage
                + "\ncurrentTrip=" + currentTrip
                + "\ncar=" + car;

        ActivitySchedule schedule = activitySchedule();

        for (ActivityIfc act = schedule.firstActivity(); act != nextActivity(); act = schedule.nextActivity(act)) {

            debug += "\n" + act;
        }

        return debug;
    }

    @Override
    public Stream<FixedDestination> getFixedDestinations() {
        return fixedDestinations.stream();
    }

    @Override
    public ModeChoicePreferences modeChoicePrefsSurvey() {
        return this.modeChoicePrefsSurvey;
    }

    @Override
    public ModeChoicePreferences modeChoicePreferences() {
        return this.modeChoicePreferences;
    }

    @Override
    public ModeChoicePreferences travelTimeSensitivity() {
        return this.travelTimeSensitivity;
    }

    @Override
    public Optional<TourBasedActivityPattern> tourBasedActivityPattern() {
        return Optional.ofNullable(this.tourPattern);
    }

}
