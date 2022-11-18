package helper;

import java.io.Serializable;

//MESSAGE CLASS WAS CREATED WITH HELP FROM CPS IN OFFICE HOURS
public class Message<T> implements Serializable {
    private MessageType type;
    private T message;

    public Message(T message, MessageType type) {
        this.type = type;
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public T getMessage() {
        return message;
    }

    public enum MessageType {
        STRING, STATUS, PRICES, TRADES, TIME
    }
}
