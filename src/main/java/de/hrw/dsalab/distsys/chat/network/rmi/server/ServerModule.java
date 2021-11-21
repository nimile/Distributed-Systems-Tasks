package de.hrw.dsalab.distsys.chat.network.rmi.server;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.network.rmi.RmiNetwork;
import de.hrw.dsalab.distsys.chat.network.rmi.server.IRmiServer;
import de.hrw.dsalab.distsys.chat.network.rmi.server.RmiServer;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkException;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkInitializeException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerModule {
    private static final Logger logger = Logger.getLogger(ServerModule.class);
    private Registry registry;
    public ServerModule() {}

    /**
     * Starts the server module
     * @throws NetworkInitializeException Is thrown when the module cannot be initialized
     */
    public void start() throws NetworkInitializeException {
        if(!Configuration.getConfiguration().getConnection().isServer()){
            return;
        }
        logger.info("Module starting");
        try {
            RmiServer rmiServer = new RmiServer();

            logger.info("Search registry");
            registry = LocateRegistry.getRegistry();

            logger.info("Export server instance as " + IRmiServer.EXPORT_NAME);
            registry.rebind(IRmiServer.EXPORT_NAME, rmiServer);

            logger.info("Module started");
        } catch (Exception ex) {
            logger.critical("Cannot start module", ex);
            throw new NetworkInitializeException(ex);
        }
    }
    /**
     * Stops the server module
     * @throws NetworkException Is thrown when an exception occurred during shutdown
     */
    public void stop() throws NetworkException{
        if(!Configuration.getConfiguration().getConnection().isServer()){
            return;
        }
        logger.info("Stopping module");
        try {
            registry.unbind(IRmiServer.EXPORT_NAME);
            logger.info("Stopped module");
        } catch (RemoteException | NotBoundException ex) {
            logger.critical("Cannot stop module", ex);
            throw new NetworkException(ex);
        }
    }
}
