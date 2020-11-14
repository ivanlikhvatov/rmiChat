package interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ChatServer extends Remote {
    void connectNewUser(Map<String, String> details, char[] password) throws RemoteException;
    void disconnect(ChatClient chatClient) throws RemoteException;
    void setPrivateMessage(String addressee, String sender, String message) throws RemoteException;
    void setPrivateMessage(List<String> addressees, String sender, String message) throws RemoteException;

    void setGeneralMessage(String message, String login) throws RemoteException;


    boolean checkLoggingInUser(String login, char[] password) throws RemoteException;

    void changePersonalData(String[] details, char[] pass) throws RemoteException;
}
