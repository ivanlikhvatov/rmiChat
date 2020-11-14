package interfaces;


import javax.print.DocFlavor;
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

    void privateMessageFromServer(List<String[]> messages, List<String[]> interlocutorsAndLastMessage) throws RemoteException;

    boolean checkLoggingInUser(String login, char[] password) throws RemoteException;

    void identificationUser() throws RemoteException;

    void setDataAfterLogin(String name, String gender) throws RemoteException;
}
