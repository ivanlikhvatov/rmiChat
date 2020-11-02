package entity;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private String text;
    private String address;

    public Message() {
    }

    public Message(String text, String address) {
        this.text = text;
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return text.equals(message.text) &&
                address.equals(message.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, address);
    }
}
