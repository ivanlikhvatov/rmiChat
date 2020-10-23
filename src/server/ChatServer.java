package server;

import client.ChatClient;
import client.ChatClientImpl;
import entity.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.util.HashMap;

public interface ChatServer extends Remote {
    void connect(HashMap<String, String> details, char[] password) throws RemoteException;
    void disconnect(ChatClientImpl chatClient) throws RemoteException;
    void getPersonalMessage() throws RemoteException;
    void getGeneralMessage() throws RemoteException;


}
