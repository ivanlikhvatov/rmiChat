package server;

import entity.Message;
import entity.User;
import interfaces.ChatClient;
import interfaces.ChatServer;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.*;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    public static final String UNIC_BINDING_NAME = "server";

    private static final String charactersForLogin = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhhijklmnopqrstuvwxyz";

    private List<User> activeUsers;
    private List<Message> messages;

    public ChatServerImpl() throws RemoteException
    {
        super();
        activeUsers = new ArrayList<>();
        messages = new ArrayList<>();
    }

    @Override
    public void connect(Map<String, String> details, char[] password) {
        try{
            ChatClient nextClient = (ChatClient)Naming.lookup("rmi://" + details.get("hostName") + "/" + details.get("clientServiceName"));

            activeUsers.add(new User(
                    details.get("username"),
                    password,
                    details.get("gender"),
                    details.get("hostName"),
                    details.get("clientServiceName"),
                    details.get("login"),
                    nextClient));

            updateUserList();
        }


        catch(RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }

        System.out.println(activeUsers);


    }

    private void updateUserList() {
        Map<String, String> namesOfActiveUsers = getUserList();

        for(User user : this.activeUsers){
            try {
                user.getClient().updateUserList(namesOfActiveUsers);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private Map<String, String> getUserList(){
        Map<String, String> namesOfActiveUsers = new HashMap<>();

        for (User user: activeUsers) {
            namesOfActiveUsers.put(user.getLogin(), user.getName());
        }

        return namesOfActiveUsers;
    }

    @Override
    public void disconnect(ChatClient chatClient) throws RemoteException {

        Iterator<User> iter = activeUsers.iterator();

        while (iter.hasNext()) {
            User user = iter.next();

            if (user.getClientServiceName().equals(chatClient.getClientServiceName()))
                iter.remove();
        }




        if(!activeUsers.isEmpty()){
            updateUserList();
        }


        System.out.println(activeUsers);
    }

    @Override
    public void getPersonalMessage(String message) {

    }

    @Override
    public void getGeneralMessage(String message, String login) {
        User user = findByLogin(login);

        if (user == null){
            return;
        }

        //TODO проверить на цензуру сообщения, если не проходит бан или предупреждение

        String messageFromServer =  "[" +user.getName() + "]" + " : " + message + "\n";
        sendToAll(messageFromServer);
    }

    private void sendToAll(String message) {
        for(User user : activeUsers){
            try {
                user.getClient().messageFromServer(message);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private User findByLogin(String login){
        for (User user : activeUsers){
            if (user.getLogin().equals(login)){
                return user;
            }
        }

        return null;
    }



    public static void main (String[] args) throws RemoteException, AlreadyBoundException, InterruptedException, MalformedURLException {

        java.rmi.registry.LocateRegistry.createRegistry(1099);

        String hostName = "localhost";
        String serviceName = "server";

        if(args.length == 2){
            hostName = args[0];
            serviceName = args[1];
        }


        ChatServer hello = new ChatServerImpl();
        Naming.rebind("rmi://" + hostName + "/" + serviceName, hello);

    }


}
