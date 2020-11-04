package interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ChatServer extends Remote {
    void connect(Map<String, String> details, char[] password) throws RemoteException;
    void disconnect(ChatClient chatClient) throws RemoteException;
    void getPersonalMessage(String message) throws RemoteException;
    void getGeneralMessage(String message, String login) throws RemoteException;


}
