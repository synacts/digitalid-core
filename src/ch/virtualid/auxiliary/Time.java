package ch.virtualid.auxiliary;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.Int64Wrapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models time in milliseconds for both dates and intervals.
 * Dates are calculated as milliseconds since 1 January 1970, 00:00:00 GMT.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Time implements Immutable, Blockable, Comparable<Time>, SQLizable {
    
    /**
     * Stores the semantic type {@code time@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("time@virtualid.ch").load(Int64Wrapper.TYPE);
    
    
    /**
     * Stores the time of a decade (10 tropical years).
     */
    public static final @Nonnull Time DECADE = new Time(315569251900l);
    
    /**
     * Stores the time of a tropical year (default).
     */
    public static final @Nonnull Time TROPICAL_YEAR = new Time(31556925190l);
    
    /**
     * Stores the time of a calendar year (365 days).
     */
    public static final @Nonnull Time CALENDAR_YEAR = new Time(31536000000l);
    
    /**
     * Stores the time of a month (30 days).
     */
    public static final @Nonnull Time MONTH = new Time(2592000000l);
    
    /**
     * Stores the time of a week (7 days).
     */
    public static final @Nonnull Time WEEK = new Time(604800000l);
    
    /**
     * Stores the time of a day (24 hours).
     */
    public static final @Nonnull Time DAY = new Time(86400000l);
    
    /**
     * Stores the time of a half-day (12 hours).
     */
    public static final @Nonnull Time HALF_DAY = new Time(43200000l);
    
    /**
     * Stores the time of an hour (60 minutes).
     */
    public static final @Nonnull Time HOUR = new Time(3600000l);
    
    /**
     * Stores the time of a half-hour (30 minutes).
     */
    public static final @Nonnull Time HALF_HOUR = new Time(1800000l);
    
    /**
     * Stores the time of a minute (60 seconds).
     */
    public static final @Nonnull Time MINUTE = new Time(60000l);
    
    /**
     * Stores the time of a second (1000 milliseconds).
     */
    public static final @Nonnull Time SECOND = new Time(1000l);
    
    
    /**
     * Stores the earliest possible time.
     */
    public static final @Nonnull Time MIN = new Time(0l);
    
    /**
     * Stores the latest possible time.
     */
    public static final @Nonnull Time MAX = new Time(Long.MAX_VALUE);
    
    
    /**
     * Stores the time in milliseconds.
     */
    private final long value;
    
    /**
     * Creates a new time with the current system time.
     */
    public Time() {
        this.value = System.currentTimeMillis();
    }
    
    /**
     * Creates a new time with the given value.
     * 
     * @param value the time in milliseconds.
     */
    public Time(long value) {
        this.value = value;
    }
    
    /**
     * Creates a new time from the given block.
     * 
     * @param block the block containing the time.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public Time(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        this.value = new Int64Wrapper(block).getValue();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new Int64Wrapper(TYPE, value).toBlock();
    }
    
    
    /**
     * Returns the time in milliseconds.
     * 
     * @return the time in milliseconds.
     */
    @Pure
    public long getValue() {
        return value;
    }
    
    
    /**
     * Adds the given time to this time.
     * 
     * @param time the time to be added.
     * 
     * @return the sum of the two times.
     */
    @Pure
    public @Nonnull Time add(@Nonnull Time time) {
        return new Time(this.value + time.value);
    }
    
    /**
     * Subtracts the given time from this time.
     * 
     * @param time the time to be subtracted.
     * 
     * @return the difference of the two times.
     */
    @Pure
    public @Nonnull Time subtract(@Nonnull Time time) {
        return new Time(this.value - time.value);
    }
    
    /**
     * Multiplies this time by the given factor.
     * 
     * @param factor the factor to multiply with.
     * 
     * @return the product of this time and the factor.
     */
    @Pure
    public @Nonnull Time multiply(int factor) {
        return new Time(this.value * factor);
    }
    
    /**
     * Divides this time by the given divisor.
     * 
     * @param divisor the divisor to divide by.
     * 
     * @return the quotient of this time and the divisor.
     */
    @Pure
    public @Nonnull Time divide(int divisor) {
        return new Time(this.value / divisor);
    }
    
    /**
     * Rounds this time down to the given interval.
     * 
     * @param interval the interval to round down to.
     * 
     * @return this time rounded down to the given interval.
     */
    @Pure
    public @Nonnull Time roundDown(@Nonnull Time interval) {
        return new Time(this.value / interval.value * interval.value);
    }
    
    /**
     * Returns whether this time is a multiple of the given interval.
     * 
     * @param interval the interval to compare this time with.
     * 
     * @return whether this time is a multiple of the given interval.
     */
    @Pure
    public boolean isMultipleOf(@Nonnull Time interval) {
        return this.value % interval.value == 0;
    }
    
    
    /**
     * Returns whether this time is negative.
     * 
     * @return whether this time is negative.
     */
    @Pure
    public boolean isNegative() {
        return this.value < 0;
    }
    
    /**
     * Returns whether this time is non-negative.
     * 
     * @return whether this time is non-negative.
     */
    @Pure
    public boolean isNonNegative() {
        return this.value >= 0;
    }
    
    /**
     * Returns whether this time is positive.
     * 
     * @return whether this time is positive.
     */
    @Pure
    public boolean isPositive() {
        return this.value > 0;
    }
    
    
    /**
     * Returns whether this time is equal to the given time.
     * 
     * @param time the time to compare this time with.
     * 
     * @return whether this time is equal to the given time.
     */
    @Pure
    public boolean isEqualTo(@Nonnull Time time) {
        return this.value == time.value;
    }
    
    /**
     * Returns whether this time is greater than the given time.
     * 
     * @param time the time to compare this time with.
     * 
     * @return whether this time is greater than the given time.
     */
    @Pure
    public boolean isGreaterThan(@Nonnull Time time) {
        return this.value > time.value;
    }
    
    /**
     * Returns whether this time is greater than or equal to the given time.
     * 
     * @param time the time to compare this time with.
     * 
     * @return whether this time is greater than or equal to the given time.
     */
    @Pure
    public boolean isGreaterThanOrEqualTo(@Nonnull Time time) {
        return this.value >= time.value;
    }
    
    /**
     * Returns whether this time is less than the given time.
     * 
     * @param time the time to compare this time with.
     * 
     * @return whether this time is less than the given time.
     */
    @Pure
    public boolean isLessThan(@Nonnull Time time) {
        return this.value < time.value;
    }
    
    /**
     * Returns whether this time is less than or equal to the given time.
     * 
     * @param time the time to compare this time with.
     * 
     * @return whether this time is less than or equal to the given time.
     */
    @Pure
    public boolean isLessThanOrEqualTo(@Nonnull Time time) {
        return this.value <= time.value;
    }
    
    @Pure
    @Override
    public int compareTo(@Nonnull Time time) {
        return Long.compare(this.value, time.value);
    }
    
    
    /**
     * Returns the number of calendar years in this time.
     * 
     * @return the number of calendar years in this time.
     */
    @Pure
    public long getYears() {
        return this.value / CALENDAR_YEAR.value;
    }
    
    /**
     * Returns the number of months in this time (without the years).
     * 
     * @return the number of months in this time (without the years).
     */
    @Pure
    public long getMonths() {
        return this.value % CALENDAR_YEAR.value / MONTH.value;
    }
    
    /**
     * Returns the number of weeks in this time (without the years and months).
     * 
     * @return the number of weeks in this time (without the years and months).
     */
    @Pure
    public long getWeeks() {
        return this.value % CALENDAR_YEAR.value % MONTH.value / WEEK.value;
    }
    
    /**
     * Returns the number of days in this time (without the years, months and weeks).
     * 
     * @return the number of days in this time (without the years, months and weeks).
     */
    @Pure
    public long getDays() {
        return this.value % CALENDAR_YEAR.value % MONTH.value % WEEK.value / DAY.value;
    }
    
    /**
     * Returns the number of hours in this time (without the days).
     * 
     * @return the number of hours in this time (without the days).
     */
    @Pure
    public long getHours() {
        return this.value % DAY.value / HOUR.value;
    }
    
    /**
     * Returns the number of minutes in this time (without the hours).
     * 
     * @return the number of minutes in this time (without the hours).
     */
    @Pure
    public long getMinutes() {
        return this.value % HOUR.value / MINUTE.value;
    }
    
    /**
     * Returns the number of seconds in this time (without the minutes).
     * 
     * @return the number of seconds in this time (without the minutes).
     */
    @Pure
    public long getSeconds() {
        return this.value % MINUTE.value / SECOND.value;
    }
    
    /**
     * Returns the number of milliseconds in this time (without the seconds).
     * 
     * @return the number of milliseconds in this time (without the seconds).
     */
    @Pure
    public long getMilliseconds() {
        return this.value % SECOND.value;
    }
    
    
    /**
     * Stores the date formatter.
     */
    private static final @Nonnull ThreadLocal<DateFormat> formatter = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() {
            return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG);
        }
    };
    
    /**
     * Returns this time as a date.
     * 
     * @return this time as a date.
     */
    @Pure
    public @Nonnull String asDate() {
        return asDate(formatter.get());
    }
    
    /**
     * Returns this time as a date with the given formatter.
     * 
     * @param formatter the formatter determining the format.
     * 
     * @return this time as a date with the given formatter.
     */
    @Pure
    public @Nonnull String asDate(@Nonnull DateFormat formatter) {
        return formatter.format(new Date(value));
    }
    
    /**
     * Appends the given value with the given unit to the given string if the value is not zero.
     * 
     * @param string the string builder to be extended.
     * @param value the value to be appended.
     * @param unit the unit of the given value.
     */
    private void append(@Nonnull StringBuilder string, long value, @Nonnull String unit) {
        if (value != 0) {
            if (string.length() > 0) string.append(", ");
            string.append(value).append(" ").append(unit);
            if (value != 1 && value != -1) string.append("s");
        }
    }
    
    /**
     * Returns this time as an interval.
     * 
     * @return this time as an interval.
     */
    @Pure
    public @Nonnull String asInterval() {
        final @Nonnull StringBuilder result = new StringBuilder();
        append(result, getYears(), "year");
        append(result, getMonths(), "month");
        append(result, getWeeks(), "week");
        append(result, getDays(), "day");
        append(result, getHours(), "hour");
        append(result, getMinutes(), "minute");
        append(result, getSeconds(), "second");
        append(result, getMilliseconds(), "millisecond");
        return result.toString();
    }
    
    /**
     * Returns this time as a date or an interval.
     * 
     * @return this time as a date or an interval.
     */
    @Pure
    public @Nonnull String asString() {
        return isGreaterThan(DECADE) ? asDate() : asInterval();
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Time)) return false;
        final @Nonnull Time other = (Time) object;
        
        return this.value == other.value;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return (int) (this.value ^ (this.value >>> 32));
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Time get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return new Time(resultSet.getLong(columnIndex));
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, value);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
}
