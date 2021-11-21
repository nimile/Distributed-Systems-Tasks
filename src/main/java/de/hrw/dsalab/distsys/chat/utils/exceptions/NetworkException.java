package de.hrw.dsalab.distsys.chat.utils.exceptions;

import java.io.IOException;


public class NetworkException extends IOException {
    public NetworkException(){
        this("Theres a generic network problem, type connect to reestablish the connection.");
    }

    public NetworkException(String message){
        super(message);
    }

    public NetworkException(Exception ex){
        super(ex);
    }

    public NetworkException(String message, Exception ex){
        super(message, ex);
    }
}
