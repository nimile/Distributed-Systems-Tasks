package de.hrw.dsalab.distsys.chat.utils.exceptions;

public class ConfigurationException extends Exception{

    public ConfigurationException(String message){
        super(message);
    }

    public ConfigurationException(Exception ex){
        super(ex);
    }

    public ConfigurationException(String message, Exception ex){
        super(message, ex);
    }
}
