package entity;

public class DialogLastMessage {
    private UserLoginAndName interlocutor;
    private String lastMessage;

    public DialogLastMessage() {
    }

    public DialogLastMessage(UserLoginAndName interlocutor, String lastMessage) {
        this.interlocutor = interlocutor;
        this.lastMessage = lastMessage;
    }

    public UserLoginAndName getInterlocutor() {
        return interlocutor;
    }

    public void setInterlocutor(UserLoginAndName interlocutor) {
        this.interlocutor = interlocutor;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public String toString() {
        return "<html>" + "<font size='5' style='bold'>" + interlocutor.getUsername() + "</font>" + "<br/>" + lastMessage + "</html>";
    }
}
