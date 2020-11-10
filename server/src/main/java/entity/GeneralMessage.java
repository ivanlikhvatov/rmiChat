package entity;

import java.io.Serializable;

public class GeneralMessage extends Message implements Serializable{
    private User author;

    public GeneralMessage() {
    }

    public GeneralMessage(User author, String text) {
        super(text);
        this.author = author;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getText() {
        return super.getMessage();
    }

    public void setText(String text) {
        super.setMessage(text);
    }

}
