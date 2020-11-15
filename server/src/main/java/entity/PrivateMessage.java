package entity;

import java.io.Serializable;

public class PrivateMessage extends Message implements Serializable{
    private User author;
    private User addressee;

    public PrivateMessage() {
    }

    public PrivateMessage(User author, User addressee, String message) {
        this.author = author;
        this.addressee = addressee;
        super.setMessage(message);
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getAddressee() {
        return addressee;
    }

    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public String getMessage() {
        return super.getMessage();
    }

    public void setMessage(String message) {
        super.setMessage(message);
    }
}
