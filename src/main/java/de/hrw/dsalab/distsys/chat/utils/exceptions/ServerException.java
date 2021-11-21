package de.hrw.dsalab.distsys.chat.utils.exceptions;

public class ServerException extends Exception{
    public ServerException(){
        super();
    }

    public ServerException(String message){
        super(message);
    }

    public ServerException(Exception ex){
        super(ex);
    }

    public ServerException(String message, Exception ex){
        super(message, ex);
    }
}
