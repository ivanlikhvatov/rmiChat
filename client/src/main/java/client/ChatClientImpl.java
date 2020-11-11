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

    private static final String CHARACTER_FOR_LOGIN = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhhijklmnopqrstuvwxyz";

    public ChatClientImpl(ClientGUI clientGUI, String username, String gender, char[] password) throws RemoteException {
        super();
        this.clientGUI = clientGUI;
        this.username = username;
        this.gender = gender;
        this.password = password;

        if (username != null){
            this.login = generateRandomLogin();
        }

        clientGUI.login = this.login;

        System.out.println(login);
    }

    @Override
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

        clientGUI.generalMessages = new ArrayList<>();
        clientGUI.privateMessages = new ArrayList<>();
        clientGUI.privateDialogs = new JList<>();

        registerWithServer(details, password);
        System.out.println("Client Listen RMI Server is running...\n");
    }


    @Override
    public void checkLoggingInUser(String login, char[] password) throws RemoteException{
        try {
            clientServiceName = "temp123";

            Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
            chatServer = (ChatServer) Naming.lookup(UNIC_BINDING_NAME);

            chatServer.checkLoggingInUser(login, password);

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
    public void setGeneralMessages(List<String> messages){
        clientGUI.generalMessages.addAll(messages);
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
        chatServer.setGeneralMessage(message, login);
    }

    @Override
    public void generalMessageFromServer(String message){

        clientGUI.generalMessages.add(message);

        if (clientGUI.generalTextArea != null) {
            clientGUI.generalTextArea.append(message);
            clientGUI.generalTextArea.setCaretPosition(clientGUI.generalTextArea.getDocument().getLength());
        }

        if (clientGUI.messageInput != null){
            clientGUI.messageInput.setText("");
        }

    }

    @Override
    public void privateMessageFromServer(Map<String, String> messageDetails, List<String[]> interlocutorsAndLastMessage) {
        PrivateMessage pm = new PrivateMessage();

        pm.setAddressee(new UserLoginAndName(messageDetails.get("addresseeLogin"), messageDetails.get("addresseeName")));
        pm.setSender(new UserLoginAndName(messageDetails.get("authorLogin"), messageDetails.get("authorName")));
        pm.setText(messageDetails.get("message"));






        if (clientGUI.dialogsPanel != null){
            clientGUI.dialogsPanel.remove(clientGUI.pmDialogsScrollPanel);
        }



        clientGUI.setPrivateDialogsPanel(interlocutorsAndLastMessage);

        clientGUI.pmDialogsScrollPanel.repaint();
        clientGUI.pmDialogsScrollPanel.revalidate();


        clientGUI.privateMessages.add(pm);

        if (messageDetails.get("authorLogin").equals(this.login)){
            if (clientGUI.messageInput != null){
                clientGUI.messageInput.setText("");
            }
        }

        if (clientGUI.privateTextArea != null){
            clientGUI.privateTextArea.append(messageDetails.get("message"));
            clientGUI.privateTextArea.setCaretPosition(clientGUI.privateTextArea.getDocument().getLength());
        }


    }

    @Override
    public String getClientServiceName() {
        return clientServiceName;
    }

    @Override
    public void sendPrivateMessage(String addressee, String message) throws RemoteException {
        chatServer.setPrivateMessage(addressee, login, message);
    }

    @Override
    public void sendPrivateMessage(List<String> addressees, String message) throws RemoteException {
        chatServer.setPrivateMessage(addressees, login, message);
    }

    public String generateRandomLogin() {
        Random random = new SecureRandom();

        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTER_FOR_LOGIN.charAt(random.nextInt(CHARACTER_FOR_LOGIN.length())));
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

    static class PrivateMessage{
        private UserLoginAndName sender;
        private UserLoginAndName addressee;
        private String text;

        public PrivateMessage() {
        }

        public PrivateMessage(UserLoginAndName sender, UserLoginAndName addressee, String text) {
            this.sender = sender;
            this.addressee = addressee;
            this.text = text;
        }

        public UserLoginAndName getSender() {
            return sender;
        }

        public void setSender(UserLoginAndName sender) {
            this.sender = sender;
        }

        public UserLoginAndName getAddressee() {
            return addressee;
        }

        public void setAddressee(UserLoginAndName addressee) {
            this.addressee = addressee;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    static class DialogLastMessage{
        private UserLoginAndName interlocutor;
        private String lastMessage;

        public DialogLastMessage() {
        }

        public DialogLastMessage(UserLoginAndName interlocutor, String lastMessage) {
            this.interlocutor = interlocutor;
            this.lastMessage = lastMessage;
        }

        public UserLoginAndName getInterlocutor() {
            return interlocutor;
        }

        public void setInterlocutor(UserLoginAndName interlocutor) {
            this.interlocutor = interlocutor;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        @Override
        public String toString() {
            return "<html>" + "<font size='5' style='bold'>" + interlocutor.getUsername() + "</font>" + "<br/>" + lastMessage + "</html>";
        }
    }
}
