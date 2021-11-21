package de.hrw.dsalab.distsys.chat.data;

import com.google.gson.Gson;
import de.hrw.dsalab.distsys.chat.enumerations.MessageType;
import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

import static de.hrw.dsalab.distsys.chat.utils.GeneralUtils.convertMillisTo;

/**
 * Represents a chat message
 *     <p><b>It uses {@link lombok.Lombok} to construct</b></p>
 *      <ul>
 *          <li>Constructor</li>
 *          <li>Getter</li>
 *          <li>Setter</li>
 *          <li>equals/hashCode</li>
 *          <li>Builder pattern</li>
 * </ul>
 * @author Nils Milewski
 * @version 1.4.1
 * @since 1.1
 */
@Data
@Builder
public class Message implements Serializable {

    /**
     * Specifies the user object
     */
    private User user;

    /**
     * Specifies the actual message
     */
    private String chatMessage;

    /**
     * Specifies the amount of seconds which has to pass until the message is considered a new one
     */
    private static final long SLOW_DOWN_RECEIVE = 10 * GeneralUtils.SECONDS;

    /**
     * Specifies timestamp when the message is created, by default it is the {@link System#currentTimeMillis() Unix timestamp}
     */
    @Builder.Default private long timestamp = System.currentTimeMillis();

    /**
     * Specifies the message type, by default {@link MessageType#IN IN} is used
     */
    @Builder.Default private MessageType type = MessageType.IN;


    public long getTravelTime(){
        return timestamp - System.currentTimeMillis();
    }


    /**
     * This implementation does a couple checks to ensure that the {@link Message} are equal.
     * <ul>
     *     <li>instanceof
     *         <ul><li>Checks if the provided object is an instance of the {@link Message} class</li></ul>
     *     </li>
     *     <li>Content
     *         <ul><li>Checks if the provided Message has the same content</li></ul>
     *     </li>
     *     <li>User
     *         <ul><li>Checks if the provided message has the same user as this one</li></ul>
     *     </li>
     *     <li>Slowdown
     *         <ul><li>The message was received in a specified timeframe ({@link Message#SLOW_DOWN_RECEIVE}</li></ul>
     *     </li>
     * </ul>
     * @param another Another {@link Object}
     * @return true iff the provided {@link Object} equals this one
     */
    @Override
    public boolean equals(Object another) {
        if(!(another instanceof Message)){
            return false;
        }
        Message msg = (Message) another;

        long newest = Math.max(msg.getTimestamp(), this.getTimestamp());
        long oldest = Math.min(msg.getTimestamp(), this.getTimestamp());
        if((newest - oldest) > SLOW_DOWN_RECEIVE){
            return false;
        }

        return msg.getUser().getUid().equals(this.getUser().getUid()) && msg.getChatMessage().equalsIgnoreCase(this.getChatMessage());
    }


    /**
     * Calls {@link Objects#hash}
     * @return {@link Objects#hash}
     */
    @Override
    public int hashCode() {
        return Objects.hash(chatMessage, timestamp);
    }

    /**
     * Returns the String representation specified by {@link Configuration configured format} of this Message.<br>
     * Every wildcard is replaced with their respected values.<br>
     * <p><b>Supported wildcards</b></p>
     * <ul>
     *     <li>$dir$<ul><li>Direction of the message specified by {@link MessageType}</li></ul></li>
     *     <li>$timestamp$<ul><li>{@link System#currentTimeMillis() Unix timestamp} of the message</li></ul></li>
     *     <li>$date$<ul><li>Date representation of the {@link System#currentTimeMillis() Unix timestamp}</li></ul></li>
     *     <li>time<ul><li>Time representation of the {@link System#currentTimeMillis() Unix timestamp}</li></ul></li>
     *     <li>$nick$<ul><li>Nick name specified by the {@link User User object}</li></ul></li>
     *     <li>$uid$<ul><li>UID of the specified {@link User User object}</li></ul></li>
     *     <li>$message$<ul><li>Actual chat message</li></ul></li>
     * </ul>
     * @return Formatted string
     */
    public String getMessage(){
        String format = Configuration.getConfiguration().getChatMessageFormat();
        String out = format;
        type = User.isSystem(user) ? MessageType.SYSTEM : type;
        type = user.equals(Configuration.getConfiguration().getUser()) ? MessageType.OUT : type;
        type = (type == null) ? MessageType.NONE : type;
        if(format.contains("$dir$")){
            String dir;
            switch(type){
                case SYSTEM:
                    dir = "><";
                    break;
                case IN:
                    dir = ">>";
                    break;
                case OUT:
                    dir = "<<";
                    break;
                case NONE:
                default:
                    dir = "<>";
            }
            out = out.replace("$dir$", dir);
        }
        if(format.contains("$timestamp$")){
            out = out.replace("$timestamp", "" + timestamp);
        }
        if(format.contains("$date$")){
            out = out.replace("$date$", "" + convertMillisTo("yyyy-MM-dd", timestamp));
        }
        if(format.contains("$time$")){
            out = out.replace("$time$", "" + convertMillisTo("HH:mm:ss", timestamp));
        }
        if(format.contains("$nick$")){
            out = out.replace("$nick$", user.getNick());
        }
        if(format.contains("$uid$")){
            out = out.replace("$uid$", String.valueOf((user.getUid())));
        }
        if(format.contains("$message$")){
            out = out.replace("$message$", chatMessage);
        }
        return out;
    }


    /**
     * Utilizes the {@link Gson#toJson} method to create a json representation of the object
     * @return Json String representation
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /**
     * Construct a new message object using the {@link User#getSystemUser() System} user
     * @param message Message which should be used
     * @return New {@link Message} object with {@link User#getSystemUser() System} as user
     */
    public static Message buildSystemMessage(String message){
        return Message.builder().user(User.getSystemUser()).chatMessage(message).build();
    }
}