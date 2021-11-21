package de.hrw.dsalab.distsys.chat.enumerations;


/**
 * Represents the type of the message
 * @author Nils Milewski
 * @version 1.0
 * @since 1.1
 */
public enum MessageType{
    /**
     * This is used if the type cannot be determined
     */
    NONE,

    /**
     * Represents a system message, it can be either one from the application or the server
     */
    SYSTEM,

    /**
     * Represent an inbound message
     */
    IN,

    /**
     * Represent an outbound message
     */
    OUT
}