package de.hrw.dsalab.distsys.chat.enumerations;

/**
 * Represents the current state of the network
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 0.4
 */
public enum NetworkState {
    /**
     * Can be used if the state cannot be determined
     */
    UNKNOWN,

    /**
     * Network is offline
     */
    OFFLINE,

    /**
     * Network is online and functional
     */
    ONLINE,

    /**
     * Network has encountered an error during operation
     */
    ERROR,

    /**
     * Network is inside the startup routine
     */
    STARTUP,

    /**
     * Network is inside the shutdown routine
     */
    SHUTDOWN
}
