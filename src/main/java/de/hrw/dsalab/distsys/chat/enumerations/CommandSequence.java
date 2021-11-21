package de.hrw.dsalab.distsys.chat.enumerations;

/**
 * This enumeration is used as a result by the {@link de.hrw.dsalab.distsys.chat.network.AbstractNetwork#checkCommand}
 * @author Nils Milewski
 * @version 1.0
 * @since 1.1
 */
public enum CommandSequence {
    /**
     * Represents that the investigated message is just a regular one
     */
    REGULAR,

    /**
     * Represents that the investigated message is a connect sequence
     */
    CONNECT,

    /**
     * Represents that the investigated message is just a disconnect sequence
     */
    DISCONNECT,

    /**
     * Represents that the sequence is neither a message nor a valid command
     */
    UNKNOWN

}
