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
    private HashMap<String, User> activeUsers;
    private HashMap<String, User> allUsers;
    private Queue<User> connectingUsers;
    private List<Message> allMessages;
    private Queue<Message> messagesToSend;
    private List<Runnable> newMessageSenders;
    private List<Runnable> oldMessageSenders;
    public static final String UNIC_BINDING_NAME = "server";
    public static final String HOST_NAME = "localhost";
    private static int MAX_COUNT_THREADS_FOR_NEW_MESSAGE;
    private static int MAX_COUNT_THREADS_FOR_OLD_MESSAGE;
    private static int COUNT_MESSAGE_FOR_ONE_THREAD;
    private static int COUNT_CONNECTING_USERS_FOR_ONE_THREAD;

    public ChatServerImpl() throws RemoteException {
        super();
        activeUsers = new HashMap<>();
        allUsers = new HashMap<>();
        allMessages = new ArrayList<>();
        messagesToSend = new ConcurrentLinkedQueue<>();
        connectingUsers = new ConcurrentLinkedQueue<>();
        newMessageSenders = Collections.synchronizedList(new ArrayList<>());
        oldMessageSenders = Collections.synchronizedList(new ArrayList<>());
        setCountsForThreads();
    }

    private static void setCountsForThreads(){
        int availableProcessorCounts = Runtime.getRuntime().availableProcessors();
        int threadCounts = availableProcessorCounts * 5;
        MAX_COUNT_THREADS_FOR_OLD_MESSAGE = threadCounts / 3;
        MAX_COUNT_THREADS_FOR_NEW_MESSAGE = threadCounts - MAX_COUNT_THREADS_FOR_OLD_MESSAGE;
        COUNT_MESSAGE_FOR_ONE_THREAD = 300;
        COUNT_CONNECTING_USERS_FOR_ONE_THREAD = 100;
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

            allUsers.put(user.getLogin(), user);
            activeUsers.put(user.getLogin(), user);
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
            activeUsers.put(user.getLogin(), user);
            updateUserList();
            connectingUsers.add(user);
            createOldMessageSender();
        } catch(RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }

    }

    @Override
    public void disconnect(ChatClient chatClient){
        Iterator<User> iter = activeUsers.values().iterator();

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
        User user;

        if ((user = allUsers.get(login)) != null && Arrays.equals(user.getPassword(), password)){
            user = allUsers.get(login);

            connectOldUser(user);
            return true;
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
        User user;

        if ((user = activeUsers.get(login)) != null){
            user.setName(name);
            user.setGender(gender);
            user.setPassword(pass);
        }

        updateUserList();
    }

    private void updateUserList(){
        List<String[]> users = getUserList();

        for(User user : activeUsers.values()){
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

        for (User user: activeUsers.values()) {
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
        User user = activeUsers.get(login);

        if (user == null){
            return;
        }

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
        User addressee = allUsers.get(addresseeLogin);

        if (addressee == null){
            return;
        }

        User sender = activeUsers.get(senderLogin);

        if (sender == null){
            return;
        }

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
        User sender = activeUsers.get(senderLogin);

        if (sender == null){
            return;
        }

        String messageFromServer =  "[" +sender.getName() + "]" + " : " + message + "\n";

        for (String addresseeLogin : addresseesLoginList) {
            User addressee = activeUsers.get(addresseeLogin);

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

        if (messagesToSend.size() >= COUNT_MESSAGE_FOR_ONE_THREAD || currentCountThreads == 0){
            Runnable sender = new NewMessageSender();
            newMessageSenders.add(sender);
            sender.run();
        }
    }

    private void createOldMessageSender(){
        int currentCountThreads = oldMessageSenders.size();

        if (currentCountThreads >= MAX_COUNT_THREADS_FOR_OLD_MESSAGE){
            return;
        }

        if (connectingUsers.size() >= COUNT_CONNECTING_USERS_FOR_ONE_THREAD || currentCountThreads == 0){
            Runnable sender = new OldMessageSender();
            oldMessageSenders.add(sender);
            sender.run();
        }
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

                        if (activeUsers.containsKey(pm.getAddressee().getLogin())){
                            addresseeClient = pm.getAddressee().getClient();
                        }

                        if (activeUsers.containsKey(pm.getAuthor().getLogin())){
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
            for(User user : activeUsers.values()){
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

                    if (!oldMessageSenders.isEmpty()){
                        oldMessageSenders.remove(this);
                    }
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

        ChatServer server = new ChatServerImpl();
        Naming.rebind("rmi://" + hostName + "/" + serviceName, server);
    }
}


