package de.hrw.dsalab.distsys.chat.network.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiClient extends Remote {
    void dataReceivedFromServer(byte[] data) throws RemoteException;
}
