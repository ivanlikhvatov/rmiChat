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
import java.util.HashMap;
import java.util.List;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    public static final String UNIC_BINDING_NAME = "server";

    private String hostName = "localhost";
    private ClientGUI clientGUI;
    private String username;
    private String gender;
    private char[] password;


    private String serviceName = "server";
    private String clientServiceName;
    private ChatServer chatServer;

    public ChatClientImpl(ClientGUI clientGUI, String username, String gender, char[] password) throws RemoteException {
        super();
        this.clientGUI = clientGUI;
        this.username = username;
        this.gender = gender;
        this.password = password;
    }


    public void identificationUser() throws RemoteException{
//        String[] details = {username, hostName, clientServiceName};
        clientServiceName = "Client_" + username;

        HashMap<String, String> details = new HashMap<>();
        details.put("username", username);
        details.put("gender", gender);
        details.put("clientServiceName", clientServiceName);
        details.put("hostName", hostName);



        try {
//            final Registry registry = LocateRegistry.getRegistry("localhost", 2099);
//            chatServer = (ChatServer) registry.lookup(UNIC_BINDING_NAME);

            Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
            chatServer = (ChatServer) Naming.lookup(UNIC_BINDING_NAME);

        }
        catch (ConnectException  e) {
            JOptionPane.showMessageDialog(
                    clientGUI.frame, "The server seems to be unavailable\nPlease try later",
                    "Connection problem", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        catch(NotBoundException | MalformedURLException me){
            me.printStackTrace();
        }

        registerWithServer(details, password);
        System.out.println("Client Listen RMI Server is running...\n");
    }

    public void registerWithServer(HashMap<String, String> details, char[] password) {
        try{
//            this.getRef();
//            System.out.println(this.getRef());
            chatServer.connect(details, password);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void updateUserList(List<String> activeUsers) {

    }
}
