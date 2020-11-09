package server;

import entity.GeneralMessage;
import entity.Message;
import entity.PrivateMessage;
import entity.User;
import interfaces.ChatClient;
import interfaces.ChatServer;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    public static final String UNIC_BINDING_NAME = "server";
    public static final String HOST_NAME = "localhost";
    private static final int MAX_COUNT_THREADS = 1000;

    private static final String CHARACTER_FOR_LOGIN = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhhijklmnopqrstuvwxyz";

    private List<User> activeUsers;
    private List<Message> allMessages;
    private Queue<Message> messagesToSend;
    private List<Thread> senders;

    public ChatServerImpl() throws RemoteException
    {
        super();
        activeUsers = new ArrayList<>();
        allMessages = new ArrayList<>();
        messagesToSend = new ConcurrentLinkedQueue<>();
        senders = new ArrayList<>();

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
//        sendToAll(messageFromServer);

        GeneralMessage gm = new GeneralMessage();
        gm.setText(messageFromServer);
        gm.setAuthor(user);

        allMessages.add(gm);
        messagesToSend.add(gm);

        createSender();



    }

    private void createSender(){

        int currentCountThreads = Thread.getAllStackTraces().keySet().size();

        if (currentCountThreads >= MAX_COUNT_THREADS){
            return;
        }

        Thread sender = new Thread(new Sender());
        sender.start();
        senders.add(sender);
    }

    private User findByLogin(String login){
        for (User user : activeUsers){
            if (user.getLogin().equals(login)){
                return user;
            }
        }

        return null;
    }

    class Sender implements Runnable{

        @Override
        public void run() {
            Message message;

            while (messagesToSend.size() > 0) {


                if ((message = messagesToSend.poll()) != null){

                    if (message.getClass().equals(PrivateMessage.class)){
                        PrivateMessage pm = (PrivateMessage) message;
                        System.out.println(pm.getClass());
                    }


                    if (message.getClass().equals(GeneralMessage.class)){
                        GeneralMessage gm = (GeneralMessage) message;
                        sendToAll(gm.getText());

                    }

                }

            }


            if (!senders.isEmpty()){
                senders.remove(this);
            }



        }

        public void sendToAll(String message){
            for(User user : activeUsers){
                try {
                    user.getClient().messageFromServer(message);
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static void main (String[] args) throws RemoteException, MalformedURLException {

        java.rmi.registry.LocateRegistry.createRegistry(1099);

        String hostName = HOST_NAME;
        String serviceName = UNIC_BINDING_NAME;

        if(args.length == 2){
            hostName = args[0];
            serviceName = args[1];
        }


        ChatServer hello = new ChatServerImpl();
        Naming.rebind("rmi://" + hostName + "/" + serviceName, hello);

    }



}
