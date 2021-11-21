package de.hrw.dsalab.distsys.chat.network.udp.raw;

import de.hrw.dsalab.distsys.chat.data.Connection;
import de.hrw.dsalab.distsys.chat.enumerations.CommandSequence;
import de.hrw.dsalab.distsys.chat.utils.ClientTimestampContainer;
import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import de.hrw.dsalab.distsys.chat.utils.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static de.hrw.dsalab.distsys.chat.network.udp.raw.UdpNetwork.BUFFER_SIZE;

/**
 * This class is used by {@link UdpNetwork network} as a server module to handle incoming connection.<br>
 * This module is disabled if the configuration states that the server should be disabled
 * @author Nils Milewski
 * @version 1.3
 * @since 2.5
 */
class UdpServerModule {
    private static final Logger logger = Logger.getLogger(UdpServerModule.class);

    /**
     * This is the underlying {@link UdpNetwork network}, which is communicated with
     */
    private final UdpNetwork network;

    /**
     * This is the {@link DatagramSocket socket} which is used to communicate over
     */
    private final DatagramSocket socket;

    /**
     * This is the timer which cleans dead clients
     */
    private final Timer disconnectedClientCleaner = new Timer();

    /**
     * This is the interval when the next clean should occur
     */
    private final AtomicLong cleanerInterval = new AtomicLong(5);

    /**
     * This {@link CopyOnWriteArrayList array list} is used to store all connected connections.
     */
    private final CopyOnWriteArrayList<ClientTimestampContainer> clients = new CopyOnWriteArrayList<>();

    /**
     * This {@link DatagramPacket packet} is used to store incoming packets
     */
    private final DatagramPacket receivedPacket;


    /**
     * Initiates a new server module
     * @param network {@link UdpServerModule} which uses this module
     * @param connection {@link Connection connection configuration} provided by {@link UdpServerModule}
     * @throws SocketException Is thrown iff the socket cannot be created
     */
    UdpServerModule(UdpNetwork network, Connection connection) throws SocketException {
        this.network = network;
        byte[] buffer = new byte[BUFFER_SIZE];
        this.socket = new DatagramSocket(connection.getServerPort());
        this.receivedPacket = new DatagramPacket(buffer, buffer.length);
        cleanerInterval.set(connection.getCleanerInterval());
    }

    /**
     * Starts the module, if an exception occurs or the server is active this method will abort<br>
     * The listener is initialized to run {@link UdpServerModule#listen listen} and started<br>
     * The dead client cleaner is initialized with a 5 minute and started<br>
     */
    public void start(){
        Thread t = new Thread(this::listen, getClass().getSimpleName());
        t.setDaemon(true);
        t.start();
        disconnectedClientCleaner.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setName("Client Cleaner");
                cleanDeadClients();
            }
        }, 5 * GeneralUtils.MINUTES, cleanerInterval.get());
        logger.info("Started module");
    }

    /**
     * Stops the module and cleans and forced to run the dead client cleaner
     */
    public void stop(){
        socket.close();
        disconnectedClientCleaner.purge();
        logger.info("Stopped module");
    }

    /**
     * Iterates over the {@link UdpServerModule#clients clients} to possible disconnect them.
     */
    void cleanDeadClients() {
        int cleaned = 0;
        logger.info("Disconnect dead clients");
        for (ClientTimestampContainer client : clients) {
            if (client.isDead()) {
                synchronized (clients) {
                    clients.remove(client);
                }
                cleaned++;
            }
        }
        logger.info("Disconnected " + cleaned + " clients");
    }

    /**
     * Broadcast the received data to all {@link UdpServerModule#clients connected clients}
     * @param data Data to be handled
     */
    private void handleDataIn(byte[] data){
        if(!network.isAlive().get()){
            return;
        }
        clients.forEach(client -> {
            try{
                DatagramPacket packet = new DatagramPacket(data, data.length, client.getAddress());
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Listen for incoming packets.<br>
     * <ul>
     *     <li>1. Listen for incoming packets</li>
     *     <li>2. Construct a new {@link ClientTimestampContainer} container</li>
     *     <li>2.1 If {@link UdpServerModule#clients} contains the container update the activity</li>
     *     <li>3. Copy incoming data into a new byte array</li>
     *     <li>4. Validate the data</li>
     *     <li>5. Branch into handleData or handleCommand</li>
     *     <li>5.1 If data is two bytes and a contains the {@link GeneralUtils#CONTROL_CHARACTER} branch into {@link UdpServerModule#handleCommand}</li>
     *     <li>5.2 Otherwise branch into {@link UdpServerModule#handleDataIn}</li>
     * </ul>
     */
    private void listen() {
        logger.info("Start listening");
        while(network.isAlive().get()){
            try {
                socket.receive(receivedPacket);
                SocketAddress address = new InetSocketAddress(receivedPacket.getAddress(), receivedPacket.getPort());
                var  connection  = new ClientTimestampContainer(address);
                if(clients.contains(connection)){
                    clients.stream().filter(connection::equals).findFirst().orElse(new ClientTimestampContainer()).updateActivity();
                }
                byte[] data = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());
                var commandValidation = network.checkCommand(data);
                if(commandValidation == CommandSequence.REGULAR) {
                    handleDataIn(data);
                }else{
                    handleCommand(commandValidation, data, connection);
                }
            }catch (Exception ex){
                logger.critical(ex.getMessage(), ex);
                network.exceptionOccurred(ex);
            }
        }
    }

    /**
     * Handles a command sequence.<br>
     * For future features the data is also required to pass in
     * @param commandValidation Command which occurred
     * @param data Received data
     * @param client The {@link ClientTimestampContainer} contains information about the client
     */
    private void handleCommand(CommandSequence commandValidation, byte[] data, ClientTimestampContainer client) {
        switch (commandValidation){
            case CONNECT:
                logger.info("Client " + client.getAddress() + " connected");
                synchronized (clients) {
                    clients.add(client);
                }
                break;
            case DISCONNECT:
                logger.info("Client " + client.getAddress() + " disconnected");
                synchronized (clients) {
                    clients.remove(client);
                }
                break;
        }
    }
}