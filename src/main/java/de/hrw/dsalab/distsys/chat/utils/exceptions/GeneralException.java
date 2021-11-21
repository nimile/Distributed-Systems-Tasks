package de.hrw.dsalab.distsys.chat.utils.exceptions;

public class GeneralException extends Exception{
    public GeneralException(){
        super();
    }

    public GeneralException(String message){
        super(message);
    }

    public GeneralException(Exception ex){
        super(ex);
    }

    public GeneralException(String message, Exception ex){
        super(message, ex);
    }
}
