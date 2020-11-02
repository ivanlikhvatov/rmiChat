package interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatClient extends Remote {
    void updateUserList(List<String> activeUsers) throws RemoteException;;
//    void identificationUser() throws RemoteException;
//    void disconnectServer();
//    void sendPersonalMessage(String message);
//    void sendGeneralMessage(String message);
}
