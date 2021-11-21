package de.hrw.dsalab.distsys.chat.network.rmi.server;

import de.hrw.dsalab.distsys.chat.network.rmi.client.IRmiClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiServer extends Remote {
    String EXPORT_NAME = "rmi://distsys/network/rmi/server_module";
    
    void subscribe(IRmiClient subscriber) throws RemoteException;

    void unsubscribe(IRmiClient subscriber) throws RemoteException;

    void sendMessage(byte[] data) throws RemoteException;

}
