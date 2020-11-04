package interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ChatClient extends Remote {
    void updateUserList(Map<String, String> activeUsers) throws RemoteException;
    String getClientServiceName() throws RemoteException;
//    void identificationUser() throws RemoteException;
//    void disconnectServer();
//    void sendPersonalMessage(String message);
    void sendGeneralMessage(String message, String login) throws RemoteException;

    void messageFromServer(String message) throws RemoteException;
}
