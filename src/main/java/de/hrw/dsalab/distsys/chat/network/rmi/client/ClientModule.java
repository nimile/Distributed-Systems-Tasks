package de.hrw.dsalab.distsys.chat.network.rmi.client;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.network.rmi.RmiNetwork;
import de.hrw.dsalab.distsys.chat.network.rmi.server.IRmiServer;
import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkException;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkInitializeException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class ClientModule extends UnicastRemoteObject implements IRmiClient {
    private static final Logger logger = Logger.getLogger(ClientModule.class);

     public ClientModule(RmiNetwork network) throws RemoteException {
        super();
        this.network = network;
    }

    private transient IRmiServer remoteServerModule;
    private final transient RmiNetwork network;


    /**
     * Starts the server module
     * @throws NetworkInitializeException Is thrown when the module cannot be initialized
     */
    public void start() throws NetworkInitializeException{
        try {
            logger.info("Module starting");

            String ip = Configuration.getConfiguration().getConnection().getIp();
            logger.info("Locate registry (" + ip + ")");
            Registry registry = LocateRegistry.getRegistry(ip);

            logger.info("Search for: " + IRmiServer.EXPORT_NAME);
            remoteServerModule = (IRmiServer) registry.lookup(IRmiServer.EXPORT_NAME);

            logger.info("Found: " + Arrays.toString(registry.list()));
            logger.info("Subscribed to server module");
            remoteServerModule.subscribe(this);
            network.connectionEstablished(ip);

            logger.info("Module started");
        }
        catch (Exception ex) {
            logger.critical("Cannot start module", ex);
            throw new NetworkInitializeException(ex);
        }
    }

    /**
     * Stops the client module
     * @throws NetworkException Is thrown when an exception occurred during shutdown
     */
    public void stop() throws NetworkException{
        try {
            logger.info("Stopping module");
            remoteServerModule.sendMessage(GeneralUtils.DISCONNECT_SEQUENCE);
            logger.info("Stopped module");
            network.connectionLost();
        } catch (RemoteException ex) {
            logger.critical("Cannot stop module", ex);
            network.exceptionOccurred(ex);
        }

    }

    public void sendMessage(Message message){
        try {
            byte[] data = network.getDecoder().encode(message);
            remoteServerModule.sendMessage(data);
        } catch (RemoteException ex) {
            network.exceptionOccurred(ex);
        }

    }

    @Override
    public void dataReceivedFromServer(byte[] data) throws RemoteException {
        Message msg = network.getDecoder().decode(data);
        network.messageReceived(msg);
    }
}
