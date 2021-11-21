package de.hrw.dsalab.distsys.chat.utils.exceptions;

public class NetworkInitializeException extends NetworkException{
    public NetworkInitializeException(){
        super("Network initialization failed, please restart the application");
    }

    public NetworkInitializeException(Exception ex){
        super(ex);
    }

}
