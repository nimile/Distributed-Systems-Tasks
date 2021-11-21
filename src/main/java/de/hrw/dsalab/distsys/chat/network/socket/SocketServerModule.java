package de.hrw.dsalab.distsys.chat.network.socket;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.data.Connection;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.utils.ClientTimestampContainer;
import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.decoder.message.IMessageDecoder;


import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is used by {@link SocketNetwork network} as a server module to handle incoming connection.<br>
 * This module is disabled if the configuration states that the server should be disabled
 * @author Nils Milewski
 * @version 1.2
 * @since 1.2
 */
class SocketServerModule {
    public static final Logger logger = Logger.getLogger(SocketServerModule.class);

    /**
     * This is the underlying {@link SocketNetwork network}, which is communicated with
     */
    private final SocketNetwork network;

    /**
     * This is the {@link ServerSocket socket} which is used to communicate over
     */
    private ServerSocket socket;

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
    private final CopyOnWriteArrayList<Client> connectedClients = new CopyOnWriteArrayList<>();

    /**
     * Used to state that the server is active
     */
    private final AtomicBoolean alive = new AtomicBoolean(false);

    /**
     * States that the server is enabled by the provided {@link SocketServerModule#connection connection} configuration
     */
    private final AtomicBoolean serverEnabled = new AtomicBoolean(false);

    /**
     * {@link SocketNetwork#getDecoder()}
     */
    private final IMessageDecoder decoder;

    /**
     * {@link Connection connection} configuration which should be used
     */
    private Connection connection;

    /**
     * Initiates a new server module
     * @param network {@link SocketNetwork} which uses this module
     */
    public SocketServerModule(SocketNetwork network) {
        this.network = network;
        decoder = this.network.getDecoder();
        init();
    }

    /**
     * Initializes the module
     */
    public void init() {
        logger.info("Initialize Server");
        connection = Configuration.getConfiguration().getConnection();
        serverEnabled.set(connection.isServer());
        cleanerInterval.set(connection.getCleanerInterval());
    }

    /**
     * Starts the module, if an exception occurs or the server is active this method will abort<br>
     * The {@link SocketServerModule#socket socket} will be created<br>
     * The listener is initialized to run {@link SocketServerModule#listen listen} and started<br>
     * The dead client cleaner is initialized with a 5 minute and started<br>
     */
    void start() {
        try {
            if (!serverEnabled.get() || alive.get()) {
                return;
            }
            logger.info("Startup Server");
            init();
            socket = new ServerSocket(connection.getServerPort());

            socket.setReuseAddress(true);
            alive.set(true);
            Thread listener = new Thread(this::listen, "Server");
            listener.setDaemon(true);
            listener.start();

            // This task disconnects all dead client.
            // A client is dead when no new message was received in a specific interval
            disconnectedClientCleaner.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Thread.currentThread().setName("Client Cleaner");
                    cleanDeadClients();
                }
            }, 5 * GeneralUtils.MINUTES, cleanerInterval.get());
            logger.info("Server Online");
        } catch (IOException ex) {
            logger.critical("Cannot start the server", ex);
        }
    }

    /**
     * Iterates over the {@link SocketServerModule#connectedClients clients} to possible disconnect them.
     */
    void cleanDeadClients() {
        int cleaned = 0;
        logger.info("Disconnect dead clients");
        for (Client client : connectedClients) {
            if (client.isInactive()) {
                // The client is no longer active therefore its considered dead.
                // To save resource the dead client is disconnected and removed from active client list
                client.disconnect();
                synchronized (connectedClients) {
                    connectedClients.remove(client);
                }
                cleaned++;
            }
        }
        logger.info("Disconnected " + cleaned + " clients");
    }

    /**
     * Listens for incoming connections and start a new {@link Client client} object
     */
    private void listen() {
        if(!serverEnabled.get()){
            return;
        }
        logger.info("Server loop started");
        while(alive.get()){
            try{
                Socket client = socket.accept();
                logger.info("Client connected");
                client.setKeepAlive(true);
                Client cl = new Client(client, this, decoder);
                cl.connect();
                connectedClients.add(cl);
            } catch (IOException e) {
                logger.critical(e.getMessage());
            }
        }
        logger.info("Server loop stopped");
    }

    /**
     * Stops the module and initiates the shutdown sequence to give the clients a small timeframe for a disconnect
     */
    void stop() {
        try {
            if (!serverEnabled.get() || !alive.get()) {
                return;
            }
            logger.info("Shutdown server");
            Thread t = new Thread(this::shutdown);
            t.setDaemon(true);
            t.start();
        } catch (Exception ex) {
            logger.critical("Cannot stop the server", ex);
        }
    }

    /**
     * Shuts the network down.<br>
     * When the {@link AbstractNetwork#shutdownSequence()} is successfully executed all clients are disconnected and removed
     * from the client list.<br>
     */
    private void shutdown() {
        try{
        if (network.shutdownSequence()) {
            for (Client client : connectedClients) {
                logger.info("Disconnect all clients");
                client.disconnect();
                synchronized (connectedClients) {
                    connectedClients.remove(client);
                }
            }
            alive.set(false);
            socket.close();
        }
        }catch (Exception ex){
            logger.info("An error occurred during shutdown");
        }
        finally {
            disconnectedClientCleaner.purge();
            logger.info("Server Offline");
        }
    }

    /**
     * Broadcasts a received message to all open connections
     * @param msg {@link Message message} to broadcast
     */
    public void broadcast(Message msg) {
        logger.info("Broadcast message");

        byte[] b = decoder.encode(msg);
        connectedClients.forEach(client -> {
            try {
                client.send(b);
            } catch (IOException e) {
                // Client cannot be reached => Disconnect and remove from connected clients
                e.printStackTrace();
                connectedClients.remove(client);
                client.disconnect();
            }
        });
    }

    /**
     * This internal class represents a connected client.
     */
    private static class Client {
        /**
         * {@link ClientTimestampContainer}
         */
        private final ClientTimestampContainer timestampContainer;

        /**
         * Socket where the client is connected to
         */
        private final Socket socket;

        /**
         * Underlying {@link SocketServerModule server module}
         */
        private final SocketServerModule server;

        /**
         * Connection state
         */
        private final AtomicBoolean connected = new AtomicBoolean(false);

        /**
         * {@link IMessageDecoder}
         */
        private final IMessageDecoder decoder;

        /**
         * Construct a new Client
         * @param socket {@link Socket Client socket} of the server
         * @param server Underlying {@link SocketServerModule server module}
         * @param decoder Used {@link IMessageDecoder decoder} described by the {@link SocketNetwork#getDecoder()}
         */
        public Client(Socket socket, SocketServerModule server, IMessageDecoder decoder){
            this.socket = socket;
            this.server = server;
            this.decoder = decoder;
            timestampContainer = new ClientTimestampContainer(new InetSocketAddress(socket.getInetAddress(), socket.getPort()));
        }

        /**
         * Calls {@link ClientTimestampContainer#isDead()}
         * @return {@link ClientTimestampContainer#isDead()}
         */
        public boolean isInactive(){
            return timestampContainer.isDead();
        }

        /**
         * Sends data to the client<br>
         * <p><b>NOTE</b> The operation will immediately abort if the client is not connected</p>
         * @param data Data to be sent
         * @throws IOException Can be thrown by {@link Socket#getOutputStream()}
         */
        public void send(byte[] data) throws IOException {
            if(!connected.get()){
                return;
            }
            socket.getOutputStream().write(data);
        }

        /**
         * Handles a command sequence
         * @param command possible command sequence
         */
        private void handleCommand(byte[] command){
            if(command.length > 1){
                switch(command[1]){
                    case GeneralUtils.DISCONNECT_CHARACTER:
                        connected.set(false);
                        break;

                    case GeneralUtils.CONNECT_CHARACTER:
                    default:
                }
            }
        }

        /**
         * Method which is executed by {@link Thread thread}<br>
         * This method handles the received data
         */
        public void run() {
            InetAddress ip = socket.getInetAddress();
            while(connected.get()){
                try {
                    InputStream is = socket.getInputStream();
                    // This while loop waits till data is available or the client shall disconnect
                    //noinspection StatementWithEmptyBody
                    while(is.available() < 1 && connected.get());
                    int length = is.available();

                    // When data is received this if statement should be executed otherwise the client shall disconnect
                    if(length > 0) {
                        timestampContainer.updateActivity();
                        String in = new String(is.readNBytes(length));
                        if(in.startsWith("" + (char)GeneralUtils.CONTROL_CHARACTER)){
                            handleCommand(in.getBytes(StandardCharsets.UTF_8));
                        }else{
                            Message msg = decoder.decode(in.getBytes(StandardCharsets.UTF_8));
                            server.broadcast(msg);
                        }
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            server.network.messageReceived(Message.buildSystemMessage("Client " + ip.getCanonicalHostName() + "(" + ip.getHostAddress() + ") disconnected"));
            try {
                // Close the connection
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         * This client sets {@link Client#connected} to true and starts a {@link Client#run listen} thread
         */
        public void connect(){
            if(connected.get()){
                return;
            }
            connected.set(true);
            Thread thread = new Thread(this::run, socket.getInetAddress().getHostAddress());
            thread.setDaemon(true);
            thread.start();
        }

        /**
         * Disconnect the client by setting {@link Client#connected} to false
         */
        public void disconnect(){
            if(!connected.get()){
                return;
            }
            connected.set(false);
        }
    }
}
