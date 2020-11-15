package entity;

public class DialogLastMessage {
    private User interlocutor;
    private String lastMessage;

    public DialogLastMessage() {
    }

    public DialogLastMessage(User interlocutor, String lastMessage) {
        this.interlocutor = interlocutor;
        this.lastMessage = lastMessage;
    }

    public User getInterlocutor() {
        return interlocutor;
    }

    public void setInterlocutor(User interlocutor) {
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
        String g;

        if (interlocutor.getGender().equals("male")){
            g = "муж";
        } else {
            g = "жен";
        }

        return "<html>" + "<font size='5' style='bold'>" + interlocutor.getUsername() + "</font>" + "  " + "<font size = '3'>" + g + "</font>" + "<br/>" + lastMessage + "</html>";
    }
}
