package de.hrw.dsalab.distsys.chat.utils.decoder.message;

import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;

/**
 * Represents an abstract definition for a new message decoder
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 1.0
 */
public interface IMessageDecoder {

    /**
     * This method is used to decode a {@link Byte} array into a {@link Message} object.
     * @param rawInput Received network data
     * @return {@link Message}
     */
    Message decode(byte[] rawInput);

    /**
     * Converts a {@link Message} object into a {@link Byte} array<br>
     * This array can be used to send using {@link AbstractNetwork#sendNetworkData(User, byte[])}
     * @param message {@link Message} object which shall be encoded
     * @return {@link Byte} array
     */
    byte[] encode(Message message);
}
