package de.hrw.dsalab.distsys.chat.network;

import de.hrw.dsalab.distsys.chat.enumerations.CommandSequence;
import de.hrw.dsalab.distsys.chat.enumerations.NetworkTypes;
import de.hrw.dsalab.distsys.chat.interfaces.INetworkConsumer;
import de.hrw.dsalab.distsys.chat.interfaces.NetworkListener;
import de.hrw.dsalab.distsys.chat.network.rmi.RmiNetwork;
import de.hrw.dsalab.distsys.chat.network.udp.BroadcastNetwork;
import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;
import de.hrw.dsalab.distsys.chat.network.dummy.DummyNetwork;
import de.hrw.dsalab.distsys.chat.network.udp.MulticastNetwork;
import de.hrw.dsalab.distsys.chat.network.socket.SocketNetwork;
import de.hrw.dsalab.distsys.chat.network.udp.raw.UdpNetwork;
import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.decoder.message.IMessageDecoder;
import de.hrw.dsalab.distsys.chat.utils.decoder.message.JsonMessageDecoder;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkInitializeException;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This abstract class contains definitions for a software network layer.<br>
 * An {@link IMessageDecoder} and the UI-Interaction logic is also provided by this abstract class.<br>
 * Different network layer are created and accessible via the {@link AbstractNetwork#getNetwork(NetworkTypes)} method.
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 0.4
 */
public abstract class AbstractNetwork implements NetworkListener {
    public static final Logger logger = Logger.getLogger(AbstractNetwork.class);

    /**
     * List of all subscribed {@link INetworkConsumer consumer}
     */
    private final List<INetworkConsumer> networkConsumer;

    /**
     * States that the network is currently during a shutdown phase
     */
    protected final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    /**
     * This is the last sent message, it is used for spam protection
      */
    private Message lastMessage = null;

    /**
     * Represents the used {@link IMessageDecoder decoder}
     */
    private IMessageDecoder decoder = new JsonMessageDecoder();


    /**
     * Instantiates this abstract class
     */
    protected AbstractNetwork(){
        this.networkConsumer = new ArrayList<>();
    }

    /**
     * This method is used to send a {@link Byte} array over the network.<br>
     * An {@link User} object describes who had sent the data
     * @param user Sending {@link User} object
     * @param data {@link Byte} array of bytes
     */
    public abstract void sendNetworkData(User user, byte[] data);

    /**
     * This method is used to start the network.<br>
     * Initialization should be done here
     * @throws NetworkInitializeException Can be thrown to indicate the network cannot be started
     */
    public abstract void start() throws NetworkInitializeException;

    /**
     * Shuts the network down.<br>
     *
     */
    public abstract void stop();

    /**
     * This method initializes the network.<br>
     * The method does nothing if not overridden
     * @throws NetworkInitializeException Can be thrown to indicate that the network cannot be initialized
     */
    protected void init() throws NetworkInitializeException {}


    /**
     *
     * @return True iff te the server module is enabled
     */
    protected boolean isServerEnabled(){
        return Configuration.getConfiguration().getConnection().isServer();
    }

    /**
     * Changes the message {@link IMessageDecoder} for the network.<br>
     * The {@link JsonMessageDecoder} is used by default.
     * @param decoder New {@link IMessageDecoder}
     */
    public void withDecoder(IMessageDecoder decoder){
        this.decoder = decoder;
    }

    public IMessageDecoder getDecoder() {
        return decoder;
    }

    /**
     * This method subscribes an {@link INetworkConsumer} to the notification list
     * @param subscriber Subscriber
     */
    public void subscribe(INetworkConsumer subscriber){
        networkConsumer.add(subscriber);
    }

    /**
     * An {@link INetworkConsumer} can use this method to unsubscribe from the notification list
     * @param subscriber Subscriber
     */
    public void unsubscribe(INetworkConsumer subscriber){
        networkConsumer.remove(subscriber);
    }

    /**
     * This method notifies all subscribed {@link INetworkConsumer} with a provided {@link Message}
     * @param message Message which was received by the network
     */
    protected void notifyNetworkConsumer(Message message){
        if(null == message || (message.equals(lastMessage) && !User.isSystem(message.getUser()))){
            return;
        }
        lastMessage = message;
        networkConsumer.forEach(consumer -> consumer.networkDataReceived(message));
    }

    /**
     * This method constructs a new {@link Message#buildSystemMessage system message} and notifies all subscribes {@link INetworkConsumer consumer} that an error occurred
     * @param ex Exception which should be used
     */
    public void exceptionOccurred(Exception ex){
        logger.critical(ex.getMessage(), ex);
        Message message = Message.buildSystemMessage(ex.getMessage() + (ex.getMessage().endsWith(".") ? "" : ".") + "Please restart the application");
        notifyNetworkConsumer(message);
    }

    /**
     * This method constructs a new {@link Message#buildSystemMessage system message} and notifies all subscribes {@link INetworkConsumer consumer} that the connection was established
     * @param ip Ip of the connected server
     */
    public void connectionEstablished(String ip){
        Message message = Message.buildSystemMessage("Connection to " + ip + " successful established. Type disconnect to close the connection");
        notifyNetworkConsumer(message);
    }

    /**
     * This method constructs a new {@link Message#buildSystemMessage system message} and notifies all subscribes {@link INetworkConsumer consumer} that the connection was disconnected
     */
    public void connectionLost(){
        Message message = Message.buildSystemMessage("Connection lost. Type connect to reestablish a connection");
        notifyNetworkConsumer(message);
    }

    /**
     * This method initiates the shutdown sequence. <br>
     * It will wait until configured shutdown time is reached<br>
     * If a sequence is active it will immediately return with false.
     * @return True iff the sequence was successfully executed
     */
    public boolean shutdownSequence() {
        if(isShuttingDown.get()){
            logger.info("Already in a shutdown sequence");
            return false;
        }
        logger.info("Shutdown sequence started");
        isShuttingDown.set(true);
        long shutdownTime = Configuration.getConfiguration().getConnection().getShutdownTime();
        long end = System.currentTimeMillis() + shutdownTime;
        long diff = end;
        while (diff > 0) {
            diff = end - System.currentTimeMillis();
        }
        isShuttingDown.set(false);
        return true;
    }

    /**
     * Restarts the network
     * @throws NetworkInitializeException Is thrown by the {@link AbstractNetwork#init() initialization}
     */
    public void restart() throws NetworkInitializeException {
        stop();
        if(shutdownSequence()){
            start();
        }
    }

    /**
     * Checks if a byte array is a command.<br>
     * A command is specified by an array with 2 elements, first one must be {@link GeneralUtils#CONTROL_CHARACTER CONTROL_CHARACTER}
     * @param data array to validate
     * @return True iff the array is a command
     */
    public CommandSequence checkCommand(byte[] data) {
     return GeneralUtils.checkCommand(data);
    }




    /**
     * This map contains all implemented {@link AbstractNetwork network} associated by {@link NetworkTypes network types}
     */
    private static final Map<NetworkTypes, AbstractNetwork> networks = new EnumMap<>(NetworkTypes.class);

    /**
     * Access the {@link AbstractNetwork#networks network map}
     * @param type {@link NetworkTypes network typ} which should be used
     * @return {@link AbstractNetwork network} associated by {@link NetworkTypes type}
     */
    public static AbstractNetwork getNetwork(NetworkTypes type){
        synchronized (networks) {
            networks.putIfAbsent(NetworkTypes.DUMMY, new DummyNetwork());
            networks.putIfAbsent(NetworkTypes.SOCKET, new SocketNetwork());
            networks.putIfAbsent(NetworkTypes.UDP, new UdpNetwork());
            networks.putIfAbsent(NetworkTypes.BROADCAST, new BroadcastNetwork());
            networks.putIfAbsent(NetworkTypes.MULTICAST, new MulticastNetwork());
            networks.putIfAbsent(NetworkTypes.RMI, new RmiNetwork());
        }
        return networks.getOrDefault(type, new SocketNetwork());
    }
}
