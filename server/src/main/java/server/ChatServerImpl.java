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
    private List<User> allUsers;
    private List<Message> allMessages;
    private Queue<Message> messagesToSend;
    private List<Thread> senders;

    public ChatServerImpl() throws RemoteException {
        super();
        activeUsers = new ArrayList<>();
        allUsers = new ArrayList<>();
        allMessages = new ArrayList<>();
        messagesToSend = new ConcurrentLinkedQueue<>();
        senders = new ArrayList<>();

    }

    @Override
    public void connect(Map<String, String> details, char[] password) {
        try{
            ChatClient nextClient = (ChatClient)Naming.lookup("rmi://" + details.get("hostName") + "/" + details.get("clientServiceName"));

            User user = (new User(
                    details.get("username"),
                    password,
                    details.get("gender"),
                    details.get("hostName"),
                    details.get("clientServiceName"),
                    details.get("login"),
                    nextClient));

            allUsers.add(user);
            activeUsers.add(user);

            updateUserList();
            setAllMessages(nextClient);
        }


        catch(RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }


    }

    @Override
    public boolean checkLoggingInUser(String login, char[] password) throws RemoteException{
        for (User user: allUsers) {
            if (user.getLogin().equals(login) && Arrays.equals(user.getPassword(), password)){

                System.out.println(user.getClient().getClientServiceName());//TODO вылетает ошибка при обращении к любому методу клиента

                user.getClient().identificationUser();

                return true;
            }
        }

        return false;
    }

    private void setAllMessages(ChatClient newClient) {

        try {
            newClient.setGeneralMessages(getGeneralMessageTextList());
            //TODO добавлять старые сообщения приватные если пользователь входит и не регистрируется
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private List<String> getGeneralMessageTextList(){
        List<String> messageTextList = new ArrayList<>();

        for (Message message : allMessages) {
            if (message.getClass().equals(GeneralMessage.class)){
                messageTextList.add(message.getMessage());
            }


        }

        return messageTextList;
    }

    private void updateUserList() throws RemoteException{
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


    }




    @Override
    public void setGeneralMessage(String message, String login) {
        User user = findByLogin(login);

        if (user == null){
            return;
        }



        //TODO проверить на цензуру сообщения, если не проходит бан или предупреждение
        String messageFromServer =  "[" +user.getName() + "]" + " : " + message + "\n";

        GeneralMessage gm = new GeneralMessage();
        gm.setText(messageFromServer);
        gm.setAuthor(user);

        allMessages.add(gm);
        messagesToSend.add(gm);

        createSender();



    }


    @Override
    public void setPrivateMessage(String addresseeLogin, String senderLogin, String message){
        User addressee = findByLogin(addresseeLogin);

        if (addressee == null){
            return;
        }

        User sender = findByLogin(senderLogin);

        if (sender == null){
            return;
        }

        //TODO проверить на цензуру сообщения, если не проходит бан или предупреждение
        String messageFromServer =  "[" +sender.getName() + "]" + " : " + message + "\n";

        PrivateMessage pm = new PrivateMessage();
        pm.setAddressee(addressee);
        pm.setAuthor(sender);
        pm.setMessage(messageFromServer);

        allMessages.add(pm);
        messagesToSend.add(pm);

        createSender();
    }

    @Override
    public void setPrivateMessage(List<String> addresseesLoginList, String senderLogin, String message){


        User sender = findByLogin(senderLogin);

        if (sender == null){
            return;
        }

        //TODO проверить на цензуру сообщения, если не проходит бан или предупреждение
        String messageFromServer =  "[" +sender.getName() + "]" + " : " + message + "\n";

        for (String addresseeLogin : addresseesLoginList) {
            User addressee = findByLogin(addresseeLogin);

            if (addressee == null){
                return;
            }

            PrivateMessage pm = new PrivateMessage();
            pm.setAuthor(sender);
            pm.setAddressee(addressee);
            pm.setMessage(messageFromServer);

            allMessages.add(pm);
            messagesToSend.add(pm);
            createSender();
        }


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

                        ChatClient addresseeClient = pm.getAddressee().getClient();
                        ChatClient authorClient = pm.getAuthor().getClient();

                        Map<String, String> messageDetails = new HashMap<>();
                        messageDetails.put("authorName", pm.getAuthor().getName());
                        messageDetails.put("authorLogin", pm.getAuthor().getLogin());
                        messageDetails.put("addresseeName", pm.getAddressee().getName());
                        messageDetails.put("addresseeLogin", pm.getAddressee().getLogin());
                        messageDetails.put("message", pm.getMessage());

                        try{
                            authorClient.privateMessageFromServer(messageDetails, findPrivateMessageByUserLogin(pm.getAuthor().getLogin()));
                            addresseeClient.privateMessageFromServer(messageDetails, findPrivateMessageByUserLogin(pm.getAddressee().getLogin()));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

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

        private List<String[]> findPrivateMessageByUserLogin(String userLogin){

            List<String[]> userPrivateMessages = new ArrayList<>();
            List<String> passedUsers = new ArrayList<>();

            for (int i = allMessages.size() - 1; i >= 0; i--) {
                if (allMessages.get(i).getClass().equals(PrivateMessage.class)){

                    PrivateMessage pm = (PrivateMessage) allMessages.get(i);

                    if (pm.getAddressee().getLogin().equals(userLogin)){

                        if (passedUsers.contains(pm.getAuthor().getLogin())){
                            continue;
                        }

                        String login = pm.getAuthor().getLogin();
                        String username = pm.getAuthor().getName();
                        String textMessage = pm.getMessage();

                        String[] details = new String[3];
                        details[0] = login;
                        details[1] = username;
                        details[2] = textMessage;


                        passedUsers.add(login);
                        userPrivateMessages.add(details);


                    }

                    if (pm.getAuthor().getLogin().equals(userLogin)){

                        if (passedUsers.contains(pm.getAddressee().getLogin())){
                            continue;
                        }

                        String login = pm.getAddressee().getLogin();
                        String username = pm.getAddressee().getName();
                        String textMessage = pm.getMessage();

                        String[] details = new String[3];
                        details[0] = login;
                        details[1] = username;
                        details[2] = textMessage;

                        passedUsers.add(login);
                        userPrivateMessages.add(details);
                    }



                }
            }

            return userPrivateMessages;
        }






//        private List<String[]> findAllPrivateMessageByUserLogin(String userLogin){
//
//            List<String[]> userPrivateMessages = new ArrayList<>();
//
//            for (Message message: allMessages) {
//                if (message.getClass().equals(PrivateMessage.class)){
//
//                    PrivateMessage pm = (PrivateMessage) message;
//
//                    if (pm.getAddressee().getLogin().equals(userLogin)){
//                        String login = pm.getAuthor().getLogin();
//                        String username = pm.getAuthor().getName();
//                        String textMessage = pm.getMessage();
//
//                        String[] details = new String[3];
//                        details[0] = login;
//                        details[1] = username;
//                        details[2] = textMessage;
//
//                        userPrivateMessages.add(details);
//                    }
//
//                    if (pm.getAuthor().getLogin().equals(userLogin)){
//                        String login = pm.getAddressee().getLogin();
//                        String username = pm.getAddressee().getName();
//                        String textMessage = pm.getMessage();
//
//                        String[] details = new String[3];
//                        details[0] = login;
//                        details[1] = username;
//                        details[2] = textMessage;
//
//                        userPrivateMessages.add(details);
//                    }
//
//
//
//                }
//            }
//
//            return userPrivateMessages;
//        }





        private void sendToAll(String message){
            for(User user : activeUsers){
                try {
                    user.getClient().generalMessageFromServer(message);
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
