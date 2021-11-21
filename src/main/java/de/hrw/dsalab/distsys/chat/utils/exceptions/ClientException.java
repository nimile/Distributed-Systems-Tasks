package de.hrw.dsalab.distsys.chat.utils.exceptions;

public class ClientException extends Exception{
    public ClientException(){
        super();
    }

    public ClientException(String message){
        super(message);
    }

    public ClientException(Exception ex){
        super(ex);
    }

    public ClientException(String message, Exception ex){
        super(message, ex);
    }
}
