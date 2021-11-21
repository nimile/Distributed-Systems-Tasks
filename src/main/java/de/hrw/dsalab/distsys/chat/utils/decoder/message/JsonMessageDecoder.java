package de.hrw.dsalab.distsys.chat.utils.decoder.message;

import com.google.gson.Gson;
import de.hrw.dsalab.distsys.chat.data.Message;

import java.nio.charset.StandardCharsets;

/**
 * Represents a concrete implementation of the {@link IMessageDecoder}
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 1.0
 */
public class JsonMessageDecoder implements IMessageDecoder {
    /**
     * {@inheritDoc}
     */
    @Override
    public Message decode(byte[] rawInput) {
        Gson gson = new Gson();
        return gson.fromJson(new String(rawInput), Message.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encode(Message message) {
        Gson gson = new Gson();
        return gson.toJson(message).getBytes(StandardCharsets.UTF_8);
    }
}
