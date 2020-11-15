package entity;

public class PrivateMessage {
    private User sender;
    private User addressee;
    private String text;

    public PrivateMessage() {
    }

    public PrivateMessage(User sender, User addressee, String text) {
        this.sender = sender;
        this.addressee = addressee;
        this.text = text;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getAddressee() {
        return addressee;
    }

    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
