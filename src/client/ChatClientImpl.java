package client;

import entity.User;
import server.ChatServer;

import javax.swing.*;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    public static final String UNIC_BINDING_NAME = "server";

    private String hostName = "localhost";
    private ClientGUI clientGUI;
    private String username;
    private String gender;
    private String password;


    private String serviceName = "GroupChatService";
    private String clientServiceName;
    private ChatServer chatServer;

    public ChatClientImpl(ClientGUI clientGUI, String username) throws RemoteException {
        super();
        this.clientGUI = clientGUI;
        this.username = username;
    }


//    @Override
    public void identificationUser() throws RemoteException{
        String[] details = {username, hostName, clientServiceName};



        try {
            final Registry registry = LocateRegistry.getRegistry("localhost", 2099);
//            Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
            chatServer = (ChatServer) registry.lookup(UNIC_BINDING_NAME);
        }
        catch (ConnectException  e) {
            JOptionPane.showMessageDialog(
                    clientGUI.frame, "The server seems to be unavailable\nPlease try later",
                    "Connection problem", JOptionPane.ERROR_MESSAGE);
//            connectionProblem = true;
            e.printStackTrace();
        }
        catch(NotBoundException me){
//            connectionProblem = true;
            me.printStackTrace();
        }
//        if(!connectionProblem){
            registerWithServer(details);
//        }
        System.out.println("Client Listen RMI Server is running...\n");
    }

    public void registerWithServer(String[] details) {
        try{
            chatServer.connect(new User(this.username, "123", "male"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

//    @Override
//    public void disconnectServer() {
//
//    }
//
//    @Override
//    public void sendPersonalMessage(String message) {
//
//    }
//
//    @Override
//    public void sendGeneralMessage(String message) {
//
//    }





//    public static void main(String[] args) throws Exception
//    {
//        //создание реестра расшареных объетов
//        final Registry registry = LocateRegistry.getRegistry("localhost", 2099);
//
//        //получаем объект (на самом деле это proxy-объект)
//        ChatServer service = (ChatServer) registry.lookup(UNIC_BINDING_NAME);
//
//        //Вызываем удаленный метод
//        service.connect(new User("gena", "123", "male"));
//
//        service.getGeneralMessage();
//    }
}
