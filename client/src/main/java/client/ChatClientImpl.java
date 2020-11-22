package client;

import interfaces.ChatClient;
import interfaces.ChatServer;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.*;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {
    private ClientGUI clientGUI;
    private String username;
    private String gender;
    private char[] password;
    private String login;
    private String clientServiceName;
    private ChatServer chatServer;

    private static final String HOST_NAME = "localhost";
    private static final String UNIC_BINDING_NAME = "server";
    private static final String CHARACTER_FOR_LOGIN = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhhijklmnopqrstuvwxyz";

    public ChatClientImpl(ClientGUI clientGUI, String username, String gender, char[] password) throws RemoteException {
        super();
        this.clientGUI = clientGUI;
        this.username = username;
        this.gender = gender;
        this.password = password;
        this.login = generateRandomLogin();
        clientGUI.setLogin(this.login);
    }

    @Override
    public void identificationUser(){
        clientServiceName = "Client_" + login;
        Map<String, String> details = new HashMap<>();
        details.put("username", username);
        details.put("gender", gender);
        details.put("clientServiceName", clientServiceName);
        details.put("hostName", HOST_NAME);
        details.put("login", login);

        try {
            Naming.rebind("rmi://" + HOST_NAME + "/" + clientServiceName, this);
            chatServer = (ChatServer) Naming.lookup(UNIC_BINDING_NAME);
        }
        catch (ConnectException  e) {
            clientGUI.generateErrorMessage("Сервер не найден\nПопробуйте позже", "Проблема подключения");
            e.printStackTrace();
        }
        catch(NotBoundException | MalformedURLException me){
            clientGUI.generateErrorMessage("Не удается подключиться к серверу", "Проблема подключения");
            me.printStackTrace();
        }
        catch (RemoteException re){
            clientGUI.generateErrorMessage("Неккоректный пользователь", "Проблема подключения");
            re.printStackTrace();
        }

        clientGUI.assignGeneralMessages();
        clientGUI.assignPrivateMessages();
        clientGUI.assignPrivateDialogs();
        registerWithServer(details, password);
    }

    public void registerWithServer(Map<String, String> details, char[] password) {
        try{
            chatServer.connectNewUser(details, password);
        }
        catch(Exception e){
            clientGUI.generateErrorMessage("Не удалось зарегистрироваться,\nпопробуйте позже", "ошибка регистрации");
            e.printStackTrace();
        }
    }

    @Override
    public void updateUserList(List<String[]> activeUsers) {
        clientGUI.updateUserListPanel(activeUsers);
    }

    @Override
    public void sendGeneralMessage(String message, String login){
        try{
            chatServer.setGeneralMessage(message, login);
        }
        catch (RemoteException re){
            clientGUI.generateErrorMessage("Ваше сообщение не было отправлено", "Ошибка отправки");
            re.printStackTrace();
        }
    }

    @Override
    public void generalMessageFromServer(String message){
        clientGUI.updateGeneralMessages(message);
    }

    @Override
    public void privateMessageFromServer(Map<String, String> messageDetails, List<String[]> interlocutorsAndLastMessage){
        clientGUI.updatePrivateMessages(messageDetails, interlocutorsAndLastMessage);
    }

    @Override
    public String getClientServiceName() {
        return clientServiceName;
    }

    @Override
    public void sendPrivateMessage(String addressee, String message){
        try{
            chatServer.setPrivateMessage(addressee, login, message);
        }
        catch (RemoteException re){
            clientGUI.generateErrorMessage("Ваше сообщение не было отправлено", "Ошибка отправки");
            re.printStackTrace();
        }

    }

    @Override
    public void sendPrivateMessage(List<String> addressees, String message){
        try {
            chatServer.setPrivateMessage(addressees, login, message);
        }
        catch (RemoteException re){
            clientGUI.generateErrorMessage("Ваши сообщения не были отправлены", "Ошибка отправки");
            re.printStackTrace();
        }

    }

    public String generateRandomLogin() {
        Random random = new SecureRandom();

        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTER_FOR_LOGIN.charAt(random.nextInt(CHARACTER_FOR_LOGIN.length())));
        }

        return sb.toString();
    }

    public void changePersonalData(String name, String gender, char[] pass) {
        this.username = name;
        this.gender = gender;
        this.password = pass;

        String[] details = new String[3];
        details[0] = login;
        details[1] = name;
        details[2] = gender;

        try {
            chatServer.changePersonalData(details, pass);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void disconnect(ChatClientImpl chatClient) {
        try{
            chatServer.disconnect(chatClient);
        } catch (RemoteException e) {
            clientGUI.generateErrorMessage("Проблемы с отключением от сервера", "Ошибка закрытия приложения");
            e.printStackTrace();
        }
    }
}
