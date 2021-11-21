package de.hrw.dsalab.distsys.chat.data;

import com.google.gson.Gson;
import de.hrw.dsalab.distsys.chat.enumerations.NetworkTypes;
import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Represents the configuration of a connection.
 * <p>It uses {@link lombok.Lombok} to construct</p>
 *      <ul>
 *          <li>Constructor</li>
 *          <li>Getter</li>
 *          <li>Setter</li>
 *          <li>equals/hashCode</li>
 * </ul>
 * @author Nils Milewski
 * @version 1.5.3
 * @since 1.0
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "connection")
public class Connection implements Serializable {
    /**
     * This is the {@link NetworkTypes network typ}, default is {@link NetworkTypes#SOCKET}
      */
    private NetworkTypes networkTypes = NetworkTypes.SOCKET;

    /**
     * Enables the server module
     */
    private boolean server = false;

    /**
     * Specifies the server ip address
     */
    private String ip = "127.0.0.1";

    /**
     * Specifies where the application listens on the {@link Connection#ip server}
     */
    private int serverPort = 9292;

    /**
     * Specifies the server shutdown time, default is 10 Minutes
     */
    private long shutdownTime = 10 * GeneralUtils.MINUTES;

    /**
     * Specifies the interval when the client cleaner should run
     */
    private long cleanerInterval = 10 * GeneralUtils.MINUTES;

    /**
     * Utilizes the {@link Gson#toJson} method to create a json representation of the object
     * @return Json String representation
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
