package entity;

public class PrivateMessage {
    private UserLoginAndName sender;
    private UserLoginAndName addressee;
    private String text;

    public PrivateMessage() {
    }

    public PrivateMessage(UserLoginAndName sender, UserLoginAndName addressee, String text) {
        this.sender = sender;
        this.addressee = addressee;
        this.text = text;
    }

    public UserLoginAndName getSender() {
        return sender;
    }

    public void setSender(UserLoginAndName sender) {
        this.sender = sender;
    }

    public UserLoginAndName getAddressee() {
        return addressee;
    }

    public void setAddressee(UserLoginAndName addressee) {
        this.addressee = addressee;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
