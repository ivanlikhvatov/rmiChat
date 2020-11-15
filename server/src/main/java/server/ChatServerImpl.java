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
    private List<User> activeUsers;
    private List<User> allUsers;
    private Queue<User> connectingUsers;
    private List<Message> allMessages;
    private Queue<Message> messagesToSend;
    private List<Thread> newMessageSenders;
    private List<Thread> oldMessageSenders;
    public static final String UNIC_BINDING_NAME = "server";
    public static final String HOST_NAME = "localhost";
    private static final int MAX_COUNT_THREADS_FOR_NEW_MESSAGE = 900;
    private static final int MAX_COUNT_THREADS_FOR_OLD_MESSAGE = 100;
    private static final String CHARACTER_FOR_LOGIN = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhhijklmnopqrstuvwxyz";

    public ChatServerImpl() throws RemoteException {
        super();
        activeUsers = new ArrayList<>();
        allUsers = new ArrayList<>();
        allMessages = new ArrayList<>();
        messagesToSend = new ConcurrentLinkedQueue<>();
        connectingUsers = new ConcurrentLinkedQueue<>();
        newMessageSenders = Collections.synchronizedList(new ArrayList<>());
        oldMessageSenders = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void connectNewUser(Map<String, String> details, char[] password) {
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
            connectingUsers.add(user);
            createOldMessageSender();
        } catch(RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }
    }

    public void connectOldUser(User user){
        try{
            ChatClient nextClient = (ChatClient)Naming.lookup("rmi://" + user.getHostName() + "/" + user.getClientServiceName());
            nextClient.setDataAfterLogin(user.getName(), user.getGender());
            user.setClient(nextClient);
            activeUsers.add(user);
            updateUserList();
            connectingUsers.add(user);
            createOldMessageSender();
        } catch(RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }

    }

    @Override
    public void disconnect(ChatClient chatClient){
        Iterator<User> iter = activeUsers.iterator();

        while (iter.hasNext()) {
            User user = iter.next();

            try{
                if (user.getClientServiceName().equals(chatClient.getClientServiceName())){
                    iter.remove();
                }
            } catch (RemoteException e){
                e.printStackTrace();
            }
        }

        try{
            Naming.unbind("rmi://" + HOST_NAME + "/" + chatClient.getClientServiceName());
        } catch (NotBoundException | MalformedURLException | RemoteException e){
            e.printStackTrace();
        }

        if(!activeUsers.isEmpty()){
            updateUserList();
        }
    }

    @Override
    public boolean checkLoggingInUser(String login, char[] password){
        for (User oldUser: allUsers) {
            if (oldUser.getLogin().equals(login) && Arrays.equals(oldUser.getPassword(), password)){
                connectOldUser(oldUser);
                return true;
            }
        }

        try{
            Naming.unbind("rmi://" + HOST_NAME + "/" + "Client_" + login);
        } catch (NotBoundException | MalformedURLException | RemoteException e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void changePersonalData(String[] details, char[] pass) {
        String login = details[0];
        String name = details[1];
        String gender = details[2];

        for (User user : activeUsers) {
            if (user.getLogin().equals(login)){
                user.setName(name);
                user.setGender(gender);
                user.setPassword(pass);
            }
        }

        updateUserList();
    }

    private void updateUserList(){
        List<String[]> users = getUserList();

        for(User user : activeUsers){
            try {
                user.getClient().updateUserList(users);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String[]> getUserList(){
        List<String[]> users = new ArrayList<>();

        for (User user: activeUsers) {
            String[] userDetails = new String[3];
            userDetails[0] = user.getLogin();
            userDetails[1] = user.getName();
            userDetails[2] = user.getGender();
            users.add(userDetails);
        }

        return users;
    }

    @Override
    public void setGeneralMessage(String message, String login) {
        User user = findActiveByLogin(login);

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
        createNewMessageSender();
    }

    @Override
    public void setPrivateMessage(String addresseeLogin, String senderLogin, String message){
        User addressee = findAllByLogin(addresseeLogin);

        if (addressee == null){
            return;
        }

        User sender = findActiveByLogin(senderLogin);

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
        createNewMessageSender();
    }

    @Override
    public void setPrivateMessage(List<String> addresseesLoginList, String senderLogin, String message){
        User sender = findActiveByLogin(senderLogin);

        if (sender == null){
            return;
        }

        //TODO проверить на цензуру сообщения, если не проходит бан или предупреждение
        String messageFromServer =  "[" +sender.getName() + "]" + " : " + message + "\n";

        for (String addresseeLogin : addresseesLoginList) {
            User addressee = findActiveByLogin(addresseeLogin);

            if (addressee == null){
                return;
            }

            PrivateMessage pm = new PrivateMessage();
            pm.setAuthor(sender);
            pm.setAddressee(addressee);
            pm.setMessage(messageFromServer);
            allMessages.add(pm);
            messagesToSend.add(pm);
            createNewMessageSender();
        }
    }

    private void createNewMessageSender(){
        int currentCountThreads = newMessageSenders.size();

        if (currentCountThreads >= MAX_COUNT_THREADS_FOR_NEW_MESSAGE){
            return;
        }

        Thread sender = new Thread(new NewMessageSender());
        sender.start();
        newMessageSenders.add(sender);
    }

    private void createOldMessageSender(){
        int currentCountThreads = oldMessageSenders.size();

        if (currentCountThreads >= MAX_COUNT_THREADS_FOR_OLD_MESSAGE){
            return;
        }

        Thread sender = new Thread(new OldMessageSender());
        sender.start();
        oldMessageSenders.add(sender);
    }

    private User findActiveByLogin(String login){
        for (User user : activeUsers){
            if (user.getLogin().equals(login)){
                return user;
            }
        }

        return null;
    }

    private User findAllByLogin(String login){
        for (User user : allUsers){
            if (user.getLogin().equals(login)){
                return user;
            }
        }

        return null;
    }

    class NewMessageSender implements Runnable{
        @Override
        public void run() {
            Message message;

            while (messagesToSend.size() > 0) {
                if ((message = messagesToSend.poll()) != null){
                    if (message instanceof PrivateMessage){
                        PrivateMessage pm = (PrivateMessage) message;

                        ChatClient addresseeClient = null;
                        ChatClient authorClient = null;

                        if (activeUsers.contains(pm.getAddressee())){
                            addresseeClient = pm.getAddressee().getClient();
                        }

                        if (activeUsers.contains(pm.getAuthor())){
                            authorClient = pm.getAuthor().getClient();
                        }

                        Map<String, String> messageDetails = new HashMap<>();
                        messageDetails.put("authorName", pm.getAuthor().getName());
                        messageDetails.put("authorLogin", pm.getAuthor().getLogin());
                        messageDetails.put("addresseeName", pm.getAddressee().getName());
                        messageDetails.put("addresseeLogin", pm.getAddressee().getLogin());
                        messageDetails.put("authorGender", pm.getAuthor().getGender());
                        messageDetails.put("addresseeGender", pm.getAddressee().getGender());
                        messageDetails.put("message", pm.getMessage());

                        try{
                            if (authorClient != null){
                                authorClient.privateMessageFromServer(messageDetails, findPrivateMessageByUserLogin(pm.getAuthor().getLogin()));
                            }

                            if (addresseeClient != null){
                                addresseeClient.privateMessageFromServer(messageDetails, findPrivateMessageByUserLogin(pm.getAddressee().getLogin()));
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    if (message instanceof GeneralMessage){
                        GeneralMessage gm = (GeneralMessage) message;
                        sendToAll(gm.getText());
                    }
                }
            }

            if (!newMessageSenders.isEmpty()){
                newMessageSenders.remove(this);
            }
        }

        private List<String[]> findPrivateMessageByUserLogin(String userLogin){
            List<String[]> userPrivateMessages = new ArrayList<>();
            List<String> passedUsers = new ArrayList<>();

            for (int i = allMessages.size() - 1; i >= 0; i--) {
                if (allMessages.get(i) instanceof PrivateMessage){
                    PrivateMessage pm = (PrivateMessage) allMessages.get(i);

                    if (pm.getAddressee().getLogin().equals(userLogin)){
                        if (passedUsers.contains(pm.getAuthor().getLogin())){
                            continue;
                        }

                        String login = pm.getAuthor().getLogin();
                        String username = pm.getAuthor().getName();
                        String gender = pm.getAuthor().getGender();
                        String textMessage = pm.getMessage();

                        String[] details = new String[4];
                        details[0] = login;
                        details[1] = username;
                        details[2] = gender;
                        details[3] = textMessage;

                        passedUsers.add(login);
                        userPrivateMessages.add(details);
                    }

                    if (pm.getAuthor().getLogin().equals(userLogin)){
                        if (passedUsers.contains(pm.getAddressee().getLogin())){
                            continue;
                        }

                        String login = pm.getAddressee().getLogin();
                        String username = pm.getAddressee().getName();
                        String gender = pm.getAddressee().getGender();
                        String textMessage = pm.getMessage();


                        String[] details = new String[4];
                        details[0] = login;
                        details[1] = username;
                        details[2] = gender;
                        details[3] = textMessage;

                        passedUsers.add(login);
                        userPrivateMessages.add(details);
                    }
                }
            }
            return userPrivateMessages;
        }


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

    class OldMessageSender implements Runnable{
        @Override
        public void run() {
            User user;

            while (connectingUsers.size() > 0) {
                if (allMessages.size() == 0){
                    connectingUsers.poll();
                    return;
                }

                if ((user = connectingUsers.poll()) != null){
                    setAllMessages(user);
                }
            }

            if (!oldMessageSenders.isEmpty()){
                oldMessageSenders.remove(this);
            }
        }

        private void setAllMessages(User user){
            List<String[]> privateMessages = new ArrayList<>();

            for (Message message : allMessages) {
                if (message instanceof GeneralMessage){
                    try {
                        user.getClient().generalMessageFromServer(message.getMessage());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                if (message instanceof PrivateMessage){
                    if (((PrivateMessage) message).getAddressee().getLogin().equals(user.getLogin()) || ((PrivateMessage) message).getAuthor().getLogin().equals(user.getLogin())){
                        String[] messageDetails = new String[7];
                        messageDetails[0] = ((PrivateMessage) message).getAuthor().getName();
                        messageDetails[1] = ((PrivateMessage) message).getAuthor().getLogin();
                        messageDetails[2] = ((PrivateMessage) message).getAuthor().getGender();
                        messageDetails[3] = ((PrivateMessage) message).getAddressee().getName();
                        messageDetails[4] = ((PrivateMessage) message).getAddressee().getLogin();
                        messageDetails[5] = ((PrivateMessage) message).getAddressee().getGender();
                        messageDetails[6] = message.getMessage();

                        privateMessages.add(messageDetails);
                    }
                }
            }

            try{
                user.getClient().privateMessageFromServer(privateMessages, findPrivateMessageByUserLogin(user.getLogin()));
            } catch (RemoteException e) {
                e.printStackTrace();
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
                        String gender = pm.getAuthor().getGender();
                        String textMessage = pm.getMessage();

                        String[] details = new String[4];
                        details[0] = login;
                        details[1] = username;
                        details[2] = gender;
                        details[3] = textMessage;

                        passedUsers.add(login);
                        userPrivateMessages.add(details);
                    }

                    if (pm.getAuthor().getLogin().equals(userLogin)){
                        if (passedUsers.contains(pm.getAddressee().getLogin())){
                            continue;
                        }

                        String login = pm.getAddressee().getLogin();
                        String username = pm.getAddressee().getName();
                        String gender = pm.getAddressee().getGender();
                        String textMessage = pm.getMessage();

                        String[] details = new String[4];
                        details[0] = login;
                        details[1] = username;
                        details[2] = gender;
                        details[3] = textMessage;

                        passedUsers.add(login);
                        userPrivateMessages.add(details);
                    }
                }
            }
            return userPrivateMessages;
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


