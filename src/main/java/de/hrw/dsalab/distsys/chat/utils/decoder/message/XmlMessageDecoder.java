package de.hrw.dsalab.distsys.chat.utils.decoder.message;

import de.hrw.dsalab.distsys.chat.data.Message;

/**
 * Represents a concrete implementation of the {@link IMessageDecoder}
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 1.0
 */
public class XmlMessageDecoder implements IMessageDecoder {
    /**
     * {@inheritDoc}
     */
    @Override
    public Message decode(byte[] rawInput) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encode(Message message) {
        return new byte[0];
    }
}
