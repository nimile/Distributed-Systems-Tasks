package de.hrw.dsalab.distsys.chat.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.PrintStream;
import java.util.logging.Level;

/**
 * This is a custom logger class
 * @author Nils Milewski
 * @version 1.3.1
 * @since 1.0
 */
public class Logger {
    /**
     * This attribute is used as the output of the logger
     */
    private static PrintStream out = System.out;

    /**
     * Represents the name of the logger
     */
    private final String name;

    /**
     * Represents the calling class
     */
    private final Class<?> caller;

    /**
     * Represents the minimum {@link Level level} to be logged
     */
    private Level minimumLevel = Level.ALL;

    /**
     * Request a new {@link Logger logger} object
     * @param caller Calling class
     * @return New {@link Logger logger} object
     */
    public static Logger getLogger(Class<?> caller){
        return  new Logger(caller);
    }

    /**
     * Changes the output for all {@link Logger logger}
     * @param out Output where the logging should be printed to
     */
    public static void setOutput(PrintStream out){
        Logger.out = out;
    }

    /**
     * Constructs a new {@link Logger logger} object
     * @param caller Calling class
     */
    private Logger(Class<?> caller){
        this.caller = caller;
        this.name = caller.getSimpleName();
    }

    /**
     * Logs a message and an exception.<br>
     * <b>NOTE</b> If the exception is provided the stacktrace will be appended to the message
     * @param level {@link Level}
     * @param message Message to log
     * @param ex Optional exception to log
     */
    private void log(Level level, String message, Exception ex){
        if(level.intValue() < minimumLevel.intValue()){
             return;
        }
        if(ex != null){
            message += System.lineSeparator() + ExceptionUtils.getStackTrace(ex);
        }

        String text = "[" + name + "][" + level.getName() + "][" + GeneralUtils.convertMillisTo("HH:mm:ss", System.currentTimeMillis()) + "]: "+ message;
        out.println(text);
    }

    /**
     * Logs a message with given {@link Level}
     * @param level {@link Level}
     * @param message Message which should be logged
     */
    private void log(Level level, String message){
        log(level, message, null);
    }

    /**
     * Sets the minimum {@link Level loglevel} everything below this {@link Level} will be ignored
     * @param level {@link Level}
     */
    public void setMinimumLogLevel(Level level){
        this.minimumLevel = level;
    }


    /**
     * Logs a debug message
     * @param message Message which should be logged
     */
    public void debug(String message){
        log(Level.FINEST, message);
    }

    /**
     * Logs a debug message
     * @param message Message which should be logged
     * @param ex {@link Exception} which should be logged
     */
    public void debug(String message, Exception ex){
        log(Level.FINEST, message, ex);
    }


    /**
     * Logs a message as {@link Level#CONFIG config}
     * @param message Message which should be logged
     */
    public void config(String message){
        log(Level.CONFIG, message);
    }


    /**
     * Logs a message as {@link Level#INFO info}
     * @param message Message which should be logged
     */
    public void info(String message){
        log(Level.INFO, message);
    }


    /**
     * Logs a warning as {@link Level#WARNING warning}
     * @param message Message which should be logged
     */

    public void warn(String message){
        log(Level.WARNING, message);
    }

    /**
     * Logs a message and an exception as {@link Level#WARNING warning}
     * @param message Message which should be logged
     * @param ex {@link Exception} which should be logged
     */
    public void warn(String message, Exception ex){
        log(Level.WARNING, message, ex);
    }


    /**
     * Logs a critical message as {@link Level#SEVERE severe}
     * @param message Message which should be logged
     */
    public void critical(String message){
        log(Level.SEVERE, message);
    }

    /**
     * Logs a message and an exception as {@link Level#SEVERE severe}
     * @param message Message which should be logged
     * @param ex {@link Exception} which should be logged
     */
    public void critical(String message, Exception ex){
        log(Level.SEVERE, message, ex);
    }
}
