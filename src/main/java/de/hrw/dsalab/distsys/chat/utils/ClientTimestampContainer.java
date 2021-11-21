package de.hrw.dsalab.distsys.chat.utils;


import java.net.SocketAddress;
import java.util.Objects;

/**
 * Represents a container for a client<br>
 * The client is associated with a timestamp
 * @author Nils Milewski
 * @version 1.0
 * @since 2.3
 */
public class ClientTimestampContainer {
    private static final long INACTIVITY_TIME = 5 * GeneralUtils.MINUTES;

    /**
     * Address of the client
     */
    private final SocketAddress address;

    /**
     * Automatically created during constructing
     */
    private long lastActivity;

    /**
     * Constructor for an empty container
     */
    public ClientTimestampContainer(){
        address = null;
    }

    /**
     * Constructs a new {@link ClientTimestampContainer object} and initializes its timestamps
     * @param address Address of the client
     */
    public ClientTimestampContainer(SocketAddress address) {
        this.address = address;
        lastActivity = System.currentTimeMillis();
    }

    /**
     * Updates the client activity to the current {@link System#currentTimeMillis() Unix timestamp}
     */
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    /**
     * Checks if the client is dead.<br>
     * <b>NOTE</b> A client is considered dead when {@link ClientTimestampContainer#INACTIVITY_TIME inactivity time} is exceeded
     * @return true iff the {@link ClientTimestampContainer#INACTIVITY_TIME inactivity time} is exceeded
     */
    public boolean isDead(){
        return (lastActivity  + INACTIVITY_TIME) < System.currentTimeMillis();
    }

    /**
     * This method verifies that another container object is the same as the current one.<br>
     * It utilizes the {@link SocketAddress#equals} method
     * @param another Another object to check against
     * @return True iff the {@link SocketAddress} are equal
     */
    @Override
    public boolean equals(Object another) {
        if(! (another instanceof ClientTimestampContainer)){
            return false;
        }
        return address.equals(((ClientTimestampContainer) another).address);
    }

    /**
     * Calls {@link Objects#hash}
     * @return {@link Objects#hash}
     */
    @Override
    public int hashCode() {
        return Objects.hash(address, lastActivity);
    }
}
