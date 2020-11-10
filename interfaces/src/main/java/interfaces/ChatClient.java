package interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ChatClient extends Remote {
    void updateUserList(Map<String, String> activeUsers) throws RemoteException;
    String getClientServiceName() throws RemoteException;

    void sendPrivateMessage(String addressee, String message) throws RemoteException;
    void sendPrivateMessage(List<String> addressees, String message) throws RemoteException;
    void sendGeneralMessage(String message, String login) throws RemoteException;

    void generalMessageFromServer(String message) throws RemoteException;

    void privateMessageFromServer(Map<String, String> messageDetails, List<String[]> interlocutorsAndLastMessage) throws RemoteException;

    void setGeneralMessages(List<String> messages) throws RemoteException;
}
