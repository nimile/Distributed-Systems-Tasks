package de.hrw.dsalab.distsys.chat.utils;

import de.hrw.dsalab.distsys.chat.enumerations.CommandSequence;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a static class which provides utility methods
 * @author Nils Milewski
 * @version 1.1
 * @since 0.1
 */
public class GeneralUtils {
    private GeneralUtils() {}

    /**
     * Represent how many milliseconds are a second
     */
    public static final long SECONDS = 1000;

    /**
     * Represent how many seconds are a minute
     */
    public static final long MINUTES = 60 * SECONDS;

    /**
     * Represent how many minutes are an hour
     */
    public static final long HOURS = 60 * MINUTES;

    /**
     * Represent the byte which should start a control sequence
     */
    public static final byte CONTROL_CHARACTER = 27;

    /**
     * Represent the disconnect byte.<br>
     * The following sequence represent a disconnect sequence <code>{{@link GeneralUtils#CONTROL_CHARACTER CONTROL_CHARACTER}, {@link GeneralUtils#DISCONNECT_CHARACTER DISCONNECT_CHARACTER}}</code>
     */
    public static final byte DISCONNECT_CHARACTER = 2;

    /**
     * Represent to connect byte.<br>
     * The following sequence represent a connect sequence <code>{{@link GeneralUtils#CONTROL_CHARACTER CONTROL_CHARACTER}, {@link GeneralUtils#CONNECT_CHARACTER CONNECT_CHARACTER}}</code>
     */
    public static final byte CONNECT_CHARACTER = 3;

    /**
     * Represent a connect sequence
     */
    public static final byte[] CONNECT_SEQUENCE = {CONTROL_CHARACTER, CONNECT_CHARACTER};

    /**
     * Represent a disconnect sequence
     */
    public static final byte[] DISCONNECT_SEQUENCE = {CONTROL_CHARACTER, DISCONNECT_CHARACTER};


    /**
     * Converts a {@link System#currentTimeMillis() Unix timestamp} into the <i>mm:ss</i> format
     * @param timestamp {@link System#currentTimeMillis() Unix timestamp}
     * @return {@link System#currentTimeMillis() Unix timestamp} as <i>mm:ss</i>
     */
    public static String convertMillisToMinutesAndSeconds(long timestamp){
        return convertMillisTo("mm:ss", timestamp);
    }

    /**
     * Converts a {@link System#currentTimeMillis() Unix timestamp} into a specific format
     * @param format Format which should be converted to
     * @param timestamp {@link System#currentTimeMillis() Unix timestamp}
     * @return {@link System#currentTimeMillis() Unix timestamp} as specified by <i>format</i>
     */
    public static String convertMillisTo(String format, long timestamp){
        SimpleDateFormat formatter= new SimpleDateFormat(format);
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    /**
     * Masks the hour part of a {@link System#currentTimeMillis() Unix timestamp}
     * @param timestamp {@link System#currentTimeMillis() Unix timestamp}
     * @return hour part of the {@link System#currentTimeMillis() Unix timestamp}
     */
    public static long getHoursFromUnixTimestamp(long timestamp){
        return ((timestamp / (1000 * 60 * 60)) % 24);
    }
    /**
     * Masks the minute part of a {@link System#currentTimeMillis() Unix timestamp}
     * @param timestamp {@link System#currentTimeMillis() Unix timestamp}
     * @return Minutes part of the {@link System#currentTimeMillis() Unix timestamp}
     */
    public static long getMinutesFromUnixTimestamp(long timestamp){
        return ((timestamp / (1000 * 60)) % 60);
    }
    /**
     * Masks the seconds part of a {@link System#currentTimeMillis() Unix timestamp}
     * @param timestamp {@link System#currentTimeMillis() Unix timestamp}
     * @return Seconds part of the {@link System#currentTimeMillis() Unix timestamp}
     */
    public static long getSecondsFromUnixTimestamp(long timestamp){
        return (timestamp / 1000) % 60 ;
    }

    /**
     * Masks the milliseconds part of a {@link System#currentTimeMillis() Unix timestamp}
     * @param timestamp {@link System#currentTimeMillis() Unix timestamp}
     * @return Milliseconds part of the {@link System#currentTimeMillis() Unix timestamp}
     */
    public static long getMillisecondsFromUnixTimestamp(long timestamp){
        return (timestamp % 1000);
    }

    /**
     * Checks if a number is between a given range.
     * @param number Number to validate
     * @param min Maximum number to be allowed
     * @param max Minimum number to be allowed
     * @return True iff the number is in the specified range
     */
    public static boolean isNumberInRange(long number, long min, long max){
        return (number >= min && number <= max);
    }


    /**
     * Checks if a byte array is a command.<br>
     * A command is specified by an array with 2 elements, first one must be {@link GeneralUtils#CONTROL_CHARACTER CONTROL_CHARACTER}
     * @param data array to validate
     * @return True iff the array is a command
     */
    public static CommandSequence checkCommand(byte[] data) {
        CommandSequence result;
        if (data.length < 2) {
            result = CommandSequence.UNKNOWN;
        } else {
            if (data[0] == GeneralUtils.CONTROL_CHARACTER) {
                switch (data[1]) {
                    case GeneralUtils.CONNECT_CHARACTER:
                        result = CommandSequence.CONNECT;
                        break;
                    case GeneralUtils.DISCONNECT_CHARACTER:
                        result = CommandSequence.DISCONNECT;
                        break;
                    default:
                        result = CommandSequence.UNKNOWN;
                }
            }else{
                result = CommandSequence.REGULAR;
            }
        }
        return result;
    }
}
