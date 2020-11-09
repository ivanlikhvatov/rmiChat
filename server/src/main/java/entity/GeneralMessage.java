package entity;

import java.io.Serializable;

public class GeneralMessage extends Message implements Serializable{
    private User author;
    private String text;

    public GeneralMessage() {
    }

    public GeneralMessage(User author, String text) {
        this.author = author;
        this.text = text;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
