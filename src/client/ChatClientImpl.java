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
    protected ChatServer chatServer;

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
            chatServer.connect(details, password);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void updateUserList(List<String> activeUsers) {

    }


    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public ClientGUI getClientGUI() {
        return clientGUI;
    }

    public void setClientGUI(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClientServiceName() {
        return clientServiceName;
    }

    public void setClientServiceName(String clientServiceName) {
        this.clientServiceName = clientServiceName;
    }
}
