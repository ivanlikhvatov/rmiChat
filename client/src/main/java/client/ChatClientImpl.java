package client;



import interfaces.ChatClient;
import interfaces.ChatServer;

import javax.swing.*;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.*;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    public static final String UNIC_BINDING_NAME = "server";

    private String hostName = "localhost";
    private ClientGUI clientGUI;
    private String username;
    private String gender;
    private char[] password;
    private String login;


    private String serviceName = "server";
    private String clientServiceName;
    protected ChatServer chatServer;

    private static final String charactersForLogin = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhhijklmnopqrstuvwxyz";

    public ChatClientImpl(ClientGUI clientGUI, String username, String gender, char[] password) throws RemoteException {
        super();
        this.clientGUI = clientGUI;
        this.username = username;
        this.gender = gender;
        this.password = password;
        this.login = generateRandomLogin();
        clientGUI.login = this.login;
    }


    public void identificationUser() throws RemoteException{
        clientServiceName = "Client_" + username + "_" + login;

        Map<String, String> details = new HashMap<>();
        details.put("username", username);
        details.put("gender", gender);
        details.put("clientServiceName", clientServiceName);
        details.put("hostName", hostName);
        details.put("login", login);



        try {

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

    public void registerWithServer(Map<String, String> details, char[] password) {
        try{
            chatServer.connect(details, password);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void updateUserList(Map<String, String> activeUsers) {

        if (clientGUI.generalMessagePanel != null){
            clientGUI.generalMessagePanel.remove(clientGUI.activeUsersScrollPanel);
        }

        clientGUI.setActiveUsersPanel(activeUsers);
        clientGUI.activeUsersScrollPanel.repaint();
        clientGUI.activeUsersScrollPanel.revalidate();
    }

    @Override
    public void sendGeneralMessage(String message, String login) throws RemoteException {
        chatServer.getGeneralMessage(message, login);
    }

    @Override
    public void messageFromServer(String message) throws RemoteException {
        System.out.println( message );
        clientGUI.textArea.append( message );
        clientGUI.textArea.setCaretPosition(clientGUI.textArea.getDocument().getLength());

        clientGUI.messageInput.setText("");
    }

    public String generateRandomLogin() {
        Random random = new SecureRandom();

        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(charactersForLogin.charAt(random.nextInt(charactersForLogin.length())));
        }

        return sb.toString();
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

    @Override
    public String getClientServiceName() {
        return clientServiceName;
    }

    public void setClientServiceName(String clientServiceName) {
        this.clientServiceName = clientServiceName;
    }

    static class UserLoginAndName {
        private String login;
        private String username;

        public UserLoginAndName(String login, String username) {
            this.login = login;
            this.username = username;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return username;
        }
    }
}
