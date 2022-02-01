package edu.kit.ifv.mobitopp.time;

import static edu.kit.ifv.mobitopp.util.collections.StreamUtils.warn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleTime implements Time, Comparable<Time> {

    private final int seconds;
    private final DayOfWeek weekDay;
    private final RelativeTime fromStart;

    public SimpleTime() {
        this(0);
    }

    public SimpleTime(int seconds) {
        super();
        this.seconds = seconds;
        fromStart = RelativeTime.ofSeconds(seconds);
        weekDay = calculateWeekDay();
    }

    public static Time of(int days, int hours, int minutes, int seconds) {
        return from(RelativeTime.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds));
    }

    public static Time from(RelativeTime fromStart) {
        return ofSeconds(fromStart.seconds());
    }

    public static Time ofWeeks(int weeks) {
        return from(RelativeTime.ofWeeks(weeks));
    }

    public static Time ofDays(int days) {
        return from(RelativeTime.ofDays(days));
    }

    public static Time ofHours(int hours) {
        return from(RelativeTime.ofHours(hours));
    }

    public static Time ofMinutes(int minutes) {
        return from(RelativeTime.ofMinutes(minutes));
    }

    public static Time ofSeconds(int seconds) {
        return new SimpleTime(seconds);
    }

    @Override
    public int toSeconds() {
        return this.seconds;
    }

    @Override
    public int toMinutes() {
        return RelativeTime.ofSeconds(seconds).toMinutes();
    }

    @Override
    public int getWeek() {
        return fromStart().toWeeks();
    }

    @Override
    public int getDay() {
        RelativeTime fromStart = fromStart();
        if (fromStart.isNegative()) {
            return getNegativeDay();
        }
        return fromStart.toDays();
    }

    /**
     * Convert negative time to negative days.
     * Day -1 ranges from second -1 to second -86400 (1 day) including both borders.
     *
     * @return
     */
    private int getNegativeDay() {
        return fromStart().minusDays(1).plusSeconds(1).toDays();
    }

    @Override
    public DayOfWeek weekDay() {
        return weekDay;
    }

    private DayOfWeek calculateWeekDay() {
        return DayOfWeek.fromDay(getDay());
    }

    @Override
    public int getHour() {
        return fromStart().toHours() % 24;
    }

    @Override
    public int getMinute() {
        return fromStart().toMinutes() % 60;
    }

    @Override
    public int getSecond() {
        return fromStart().seconds() % 60;
    }

    @Override
    public Time previousDay() {
        return SimpleTime.ofDays(fromStart().toDays()).minusDays(1);
    }

    @Override
    public Time nextDay() {
        return SimpleTime.ofDays(fromStart().toDays()).plusDays(1);
    }

    @Override
    public boolean isMidnight() {
        return (getSecond() == 0) && (getMinute() == 0) && (getHour() == 0);
    }

    @Override
    public boolean isAfter(Time otherDate) {
        return toSeconds() > otherDate.toSeconds();
    }

    @Override
    public boolean isBefore(Time otherDate) {
        return toSeconds() < otherDate.toSeconds();
    }

    @Override
    public boolean isBeforeOrEqualTo(Time otherDate) {
        return toSeconds() <= otherDate.toSeconds();
    }

    @Override
    public boolean isAfterOrEqualTo(Time otherDate) {
        return toSeconds() >= otherDate.toSeconds();
    }

    @Override
    public Time minus(RelativeTime decrement) {
        RelativeTime changed = fromStart().minus(decrement);
        return from(changed);
    }

    @Override
    public Time minusWeeks(int decrement) {
        RelativeTime changed = fromStart().minusWeeks(decrement);
        return from(changed);
    }

    @Override
    public Time minusDays(int decrement) {
        RelativeTime changed = fromStart().minusDays(decrement);
        return from(changed);
    }

    @Override
    public Time minusHours(int decrement) {
        RelativeTime changed = fromStart().minusHours(decrement);
        return from(changed);
    }

    @Override
    public Time minusMinutes(int decrement) {
        RelativeTime changed = fromStart().minusMinutes(decrement);
        return from(changed);
    }

    @Override
    public Time minusSeconds(int decrement) {
        RelativeTime changed = fromStart().minusSeconds(decrement);
        return from(changed);
    }

    @Override
    public Time plus(RelativeTime increment) {
        RelativeTime changed = fromStart().plus(increment);
        return from(changed);
    }

    @Override
    public Time plusWeeks(int increment) {
        RelativeTime changed = fromStart().plusWeeks(increment);
        return from(changed);
    }

    @Override
    public Time plusDays(int increment) {
        RelativeTime changed = fromStart().plusDays(increment);
        return from(changed);
    }

    @Override
    public Time plusHours(int increment) {
        RelativeTime changed = fromStart().plusHours(increment);
        return from(changed);
    }

    @Override
    public Time plusMinutes(int increment) {
        RelativeTime changed = fromStart().plusMinutes(increment);
        return from(changed);
    }

    @Override
    public Time plusSeconds(int increment) {
        RelativeTime changed = fromStart().plusSeconds(increment);
        return from(changed);
    }

    @Override
    public Time startOfDay() {
        return SimpleTime.ofDays(fromStart().toDays());
    }

    @Override
    public Time newTime(int hours, int minutes, int seconds) {
        verify(hours, minutes, seconds);
        int days = fromStart().toDays();
        return SimpleTime.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

    private void verify(int hours, int minutes, int seconds) {
        if (0 > hours || 28 <= hours) {
            throw warn(new IllegalArgumentException("Hours too high or low: " + hours), log);
        }
        if (0 > minutes || 60 <= minutes) {
            throw warn(new IllegalArgumentException("Minutes too high or low: " + minutes), log);
        }
        if (0 > seconds || 60 <= seconds) {
            throw warn(new IllegalArgumentException("Seconds too high or low: " + seconds), log);
        }
    }

    public RelativeTime differenceTo(Time otherDate) {
        return this.fromStart().minus(otherDate.fromStart());
    }

    @Override
    public RelativeTime fromStart() {
        return fromStart;
    }

    public static List<Time> oneWeek() {
        Time start = new SimpleTime();
        List<Time> week = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            week.add(start.plusDays(day));
        }
        return week;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seconds);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleTime other = (SimpleTime) obj;
        return seconds == other.seconds;
    }

    @Override
    public String toString() {
        return new DateFormat().asWeekdayTime(this);
    }

    @Override
    public int compareTo(Time other) {
        if (isBefore(other)) {
            return -1;
        } else if (other.isBefore(this)) {
            return 1;
        }
        return 0;
    }

}
