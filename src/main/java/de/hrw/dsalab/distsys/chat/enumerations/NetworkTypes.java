package de.hrw.dsalab.distsys.chat.enumerations;

/**
 * Represents the different supported network types
 *
 * @author Nils Milewski
 * @version 3.0
 * @since 0.4
 */
public enum NetworkTypes {
    /**
     * No network
     */
    NONE,

    /**
     * Dummy network
     */
    DUMMY,

    /**
     * TCP/IP Socket should be used
     */
    SOCKET,

    /**
     * UDP Socket should be used
     */
    UDP,

    /**
     * The broadcast protocol should be used
     */
    BROADCAST,

    /**
     * The multicast protocol should be used
     */
    MULTICAST,

    /**
     * A rmi network should be used
     */
    RMI,

    /**
     * Rest api should be used
     */
    REST,

    /**
     * Soap api should be used
     */
    SOAP
}
