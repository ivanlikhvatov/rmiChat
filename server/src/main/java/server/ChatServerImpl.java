package server;
import entity.GeneralMessage;
import entity.Message;
import entity.PrivateMessage;
import entity.User;
import interfaces.ChatClient;
import interfaces.ChatServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private List<Thread> messageUploaders;
    private List<Thread> userUploaders;
    private Queue<User> usersToUpload;
    private Queue<Message> messagesToUpload;
    public static final String UNIC_BINDING_NAME = "server";
    public static final String HOST_NAME = "localhost";
    private static final int MAX_COUNT_THREADS_FOR_NEW_MESSAGE = 400;
    private static final int MAX_COUNT_THREADS_FOR_OLD_MESSAGE = 100;
    private static final int MAX_COUNT_THREADS_FOR_UPLOAD_MESSAGE = 400;
    private static final int MAX_COUNT_THREADS_FOR_UPLOAD_USER = 100;

    public ChatServerImpl() throws RemoteException {
        super();
        activeUsers = new ArrayList<>();
        allUsers = new ArrayList<>();
        allMessages = new ArrayList<>();
        messagesToSend = new ConcurrentLinkedQueue<>();
        connectingUsers = new ConcurrentLinkedQueue<>();
        usersToUpload = new ConcurrentLinkedQueue<>();
        messagesToUpload = new ConcurrentLinkedQueue<>();
        newMessageSenders = Collections.synchronizedList(new ArrayList<>());
        oldMessageSenders = Collections.synchronizedList(new ArrayList<>());
        messageUploaders = Collections.synchronizedList(new ArrayList<>());
        userUploaders = Collections.synchronizedList(new ArrayList<>());
        loadDataFromFile();
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
            usersToUpload.add(user);
            createUploaderUser();
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

        String messageFromServer =  "[" +user.getName() + "]" + " : " + message + "\n";
        GeneralMessage gm = new GeneralMessage();
        gm.setText(messageFromServer);
        gm.setAuthor(user);
        allMessages.add(gm);
        messagesToSend.add(gm);
        messagesToUpload.add(gm);
        createUploaderMessage();
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

        String messageFromServer =  "[" +sender.getName() + "]" + " : " + message + "\n";
        PrivateMessage pm = new PrivateMessage();
        pm.setAddressee(addressee);
        pm.setAuthor(sender);
        pm.setMessage(messageFromServer);
        allMessages.add(pm);
        messagesToSend.add(pm);
        messagesToUpload.add(pm);
        createUploaderMessage();
        createNewMessageSender();
    }

    @Override
    public void setPrivateMessage(List<String> addresseesLoginList, String senderLogin, String message){
        User sender = findActiveByLogin(senderLogin);

        if (sender == null){
            return;
        }

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
            messagesToUpload.add(pm);
            createUploaderMessage();
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

    private void createUploaderUser(){
        int currentCountThreads = userUploaders.size();

        if (currentCountThreads >= MAX_COUNT_THREADS_FOR_UPLOAD_USER){
            return;
        }

        Thread loader = new Thread(new UploaderUserInfoToFile());
        loader.start();
        userUploaders.add(loader);

    }

    private void createUploaderMessage(){
        int currentCountThreads = messageUploaders.size();

        if (currentCountThreads >= MAX_COUNT_THREADS_FOR_UPLOAD_MESSAGE){
            return;
        }

        Thread loader = new Thread(new UploaderMessageInfoToFile());
        loader.start();
        messageUploaders.add(loader);
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

    public void loadDataFromFile(){
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse("server/data/AllUsers.xml");

            Node root = document.getDocumentElement();
            NodeList users = root.getChildNodes();
            for (int i = 0; i < users.getLength(); i++) {
                Node userNode = users.item(i);
                if (userNode.getNodeType() != Node.TEXT_NODE) {
                    NodeList userProps = userNode.getChildNodes();
                    User user = new User();
                    for(int j = 0; j < userProps.getLength(); j++) {
                        Node userProp = userProps.item(j);

                        if (userProp.getNodeType() != Node.TEXT_NODE) {
                            if (userProp.getNodeName().equals("Username")){
                                user.setName(userProp.getChildNodes().item(0).getTextContent());
                            }

                            if (userProp.getNodeName().equals("Password")){
                                user.setPassword(userProp.getChildNodes().item(0).getTextContent().toCharArray());
                            }

                            if (userProp.getNodeName().equals("Gender")){
                                user.setGender(userProp.getChildNodes().item(0).getTextContent());
                            }

                            if (userProp.getNodeName().equals("HostName")){
                                user.setHostName(userProp.getChildNodes().item(0).getTextContent());
                            }

                            if (userProp.getNodeName().equals("ClientServiceName")){
                                user.setClientServiceName(userProp.getChildNodes().item(0).getTextContent());
                            }

                            if (userProp.getNodeName().equals("Login")){
                                user.setLogin(userProp.getChildNodes().item(0).getTextContent());
                            }
                        }
                    }
                    allUsers.add(user);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace();
        }


        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse("server/data/AllMessages.xml");

            Node root = document.getDocumentElement();
            NodeList messages = root.getChildNodes();
            for (int i = 0; i < messages.getLength(); i++) {
                Node messageNode = messages.item(i);

                if (messageNode.getNodeType() != Node.TEXT_NODE) {
                    Message message;

                    if (messageNode.getAttributes().item(0).getTextContent().equals("PrivateMessage")){
                        PrivateMessage pm = new PrivateMessage();
                        NodeList messageProps = messageNode.getChildNodes();

                        for(int j = 0; j < messageProps.getLength(); j++) {
                            Node messageProp = messageProps.item(j);

                            if (messageProp.getNodeType() != Node.TEXT_NODE) {
                                if (messageProp.getNodeName().equals("Text")){
                                    pm.setMessage(messageProp.getChildNodes().item(0).getTextContent());
                                }

                                if (messageProp.getNodeName().equals("Author")){
                                    NodeList authorProps = messageProp.getChildNodes();
                                    User user = new User();

                                    for (int k = 0; k < authorProps.getLength(); k++){
                                        Node authorProp = authorProps.item(k);

                                        if (authorProp.getNodeType() != Node.TEXT_NODE) {
                                            if (authorProp.getNodeName().equals("Username")){
                                                user.setName(authorProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (authorProp.getNodeName().equals("Password")){
                                                user.setPassword(authorProp.getChildNodes().item(0).getTextContent().toCharArray());
                                            }

                                            if (authorProp.getNodeName().equals("Gender")){
                                                user.setGender(authorProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (authorProp.getNodeName().equals("HostName")){
                                                user.setHostName(authorProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (authorProp.getNodeName().equals("ClientServiceName")){
                                                user.setClientServiceName(authorProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (authorProp.getNodeName().equals("Login")){
                                                user.setLogin(authorProp.getChildNodes().item(0).getTextContent());
                                            }
                                        }
                                    }

                                    pm.setAuthor(user);
                                }
                                if (messageProp.getNodeName().equals("Addressee")){
                                    NodeList addresseeProps = messageProp.getChildNodes();

                                    User user = new User();

                                    for (int k = 0; k < addresseeProps.getLength(); k++){
                                        Node addresseeProp = addresseeProps.item(k);

                                        if (addresseeProp.getNodeType() != Node.TEXT_NODE) {
                                            if (addresseeProp.getNodeName().equals("Username")){
                                                user.setName(addresseeProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (addresseeProp.getNodeName().equals("Password")){
                                                user.setPassword(addresseeProp.getChildNodes().item(0).getTextContent().toCharArray());
                                            }

                                            if (addresseeProp.getNodeName().equals("Gender")){
                                                user.setGender(addresseeProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (addresseeProp.getNodeName().equals("HostName")){
                                                user.setHostName(addresseeProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (addresseeProp.getNodeName().equals("ClientServiceName")){
                                                user.setClientServiceName(addresseeProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (addresseeProp.getNodeName().equals("Login")){
                                                user.setLogin(addresseeProp.getChildNodes().item(0).getTextContent());
                                            }
                                        }
                                    }

                                    pm.setAddressee(user);
                                }
                            }
                        }
                        message = pm;
                    }else if (messageNode.getAttributes().item(0).getTextContent().equals("GeneralMessage")){
                        GeneralMessage gm = new GeneralMessage();
                        NodeList messageProps = messageNode.getChildNodes();

                        for(int j = 0; j < messageProps.getLength(); j++) {
                            Node messageProp = messageProps.item(j);

                            if (messageProp.getNodeType() != Node.TEXT_NODE) {
                                if (messageProp.getNodeName().equals("Text")){
                                    gm.setMessage(messageProp.getChildNodes().item(0).getTextContent());
                                }

                                if (messageProp.getNodeName().equals("Author")){
                                    NodeList authorProps = messageProp.getChildNodes();
                                    User user = new User();

                                    for (int k = 0; k < authorProps.getLength(); k++){
                                        Node authorProp = authorProps.item(k);

                                        if (authorProp.getNodeType() != Node.TEXT_NODE) {
                                            if (authorProp.getNodeName().equals("Username")){
                                                user.setName(authorProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (authorProp.getNodeName().equals("Password")){
                                                user.setPassword(authorProp.getChildNodes().item(0).getTextContent().toCharArray());
                                            }

                                            if (authorProp.getNodeName().equals("Gender")){
                                                user.setGender(authorProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (authorProp.getNodeName().equals("HostName")){
                                                user.setHostName(authorProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (authorProp.getNodeName().equals("ClientServiceName")){
                                                user.setClientServiceName(authorProp.getChildNodes().item(0).getTextContent());
                                            }

                                            if (authorProp.getNodeName().equals("Login")){
                                                user.setLogin(authorProp.getChildNodes().item(0).getTextContent());
                                            }
                                        }
                                    }
                                    gm.setAuthor(user);
                                }
                            }
                        }
                        message = gm;
                    }else{
                        continue;
                    }
                    allMessages.add(message);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace();
        }
    }

    class UploaderUserInfoToFile implements Runnable{


        @Override
        public void run() {
            User user;

            while (usersToUpload.size() > 0){
                if ((user = usersToUpload.poll()) != null){
                    try {
                        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        Document document = documentBuilder.parse("server/data/AllUsers.xml");
                        Node root = document.getDocumentElement();
                        Element userTag = document.createElement("User");
                        Element username = document.createElement("Username");
                        username.setTextContent(user.getName());
                        Element password = document.createElement("Password");
                        String passString = new String(user.getPassword());
                        password.setTextContent(passString);
                        Element gender = document.createElement("Gender");
                        gender.setTextContent(user.getGender());
                        Element hostName = document.createElement("HostName");
                        hostName.setTextContent(user.getHostName());
                        Element clientServiceName = document.createElement("ClientServiceName");
                        clientServiceName.setTextContent(user.getClientServiceName());
                        Element login = document.createElement("Login");
                        login.setTextContent(user.getLogin());

                        userTag.appendChild(username);
                        userTag.appendChild(password);
                        userTag.appendChild(gender);
                        userTag.appendChild(hostName);
                        userTag.appendChild(clientServiceName);
                        userTag.appendChild(login);

                        root.appendChild(userTag);

                        try {
                            Transformer tr = TransformerFactory.newInstance().newTransformer();
                            DOMSource source = new DOMSource(document);
                            FileOutputStream fos = new FileOutputStream("server/data/AllUsers.xml");
                            StreamResult result = new StreamResult(fos);
                            tr.transform(source, result);
                        } catch (TransformerException | IOException e) {
                            e.printStackTrace(System.out);
                        }

                    } catch (ParserConfigurationException | SAXException | IOException ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            }

            userUploaders.remove(this);
        }


    }

    class UploaderMessageInfoToFile implements Runnable{

        @Override
        public void run() {
            Message message;

            while (messagesToUpload.size() > 0){
                if ((message = messagesToUpload.poll()) != null){

                    if (message instanceof PrivateMessage){
                        try {
                            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                            Document document = documentBuilder.parse("server/data/AllMessages.xml");
                            Node root = document.getDocumentElement();
                            Element messageTag = document.createElement("Message");
                            messageTag.setAttribute("type", "PrivateMessage");

                            Element text = document.createElement("Text");
                            text.setTextContent(message.getMessage());

                            Element author = document.createElement("Author");
                            Element authorName = document.createElement("Username");
                            authorName.setTextContent(((PrivateMessage) message).getAuthor().getName());
                            Element authorPassword = document.createElement("Password");
                            String authorPassString = new String(((PrivateMessage) message).getAuthor().getPassword());
                            authorPassword.setTextContent(authorPassString);
                            Element authorGender = document.createElement("Gender");
                            authorGender.setTextContent(((PrivateMessage) message).getAuthor().getGender());
                            Element authorHostName = document.createElement("HostName");
                            authorHostName.setTextContent(((PrivateMessage) message).getAuthor().getHostName());
                            Element authorClientServiceName = document.createElement("ClientServiceName");
                            authorClientServiceName.setTextContent(((PrivateMessage) message).getAuthor().getClientServiceName());
                            Element authorLogin = document.createElement("Login");
                            authorLogin.setTextContent(((PrivateMessage) message).getAuthor().getLogin());
                            author.appendChild(authorName);
                            author.appendChild(authorPassword);
                            author.appendChild(authorGender);
                            author.appendChild(authorHostName);
                            author.appendChild(authorClientServiceName);
                            author.appendChild(authorLogin);

                            Element addressee = document.createElement("Addressee");
                            Element addresseeUsername = document.createElement("Username");
                            addresseeUsername.setTextContent(((PrivateMessage) message).getAddressee().getName());
                            Element addresseePassword = document.createElement("Password");
                            String addresseePassString = new String(((PrivateMessage) message).getAddressee().getPassword());
                            addresseePassword.setTextContent(addresseePassString);
                            Element addresseeGender = document.createElement("Gender");
                            addresseeGender.setTextContent(((PrivateMessage) message).getAddressee().getGender());
                            Element addresseeHostName = document.createElement("HostName");
                            addresseeHostName.setTextContent(((PrivateMessage) message).getAddressee().getHostName());
                            Element addresseeClientServiceName = document.createElement("ClientServiceName");
                            addresseeClientServiceName.setTextContent(((PrivateMessage) message).getAddressee().getClientServiceName());
                            Element addresseeLogin = document.createElement("Login");
                            addresseeLogin.setTextContent(((PrivateMessage) message).getAddressee().getLogin());
                            addressee.appendChild(addresseeUsername);
                            addressee.appendChild(addresseePassword);
                            addressee.appendChild(addresseeGender);
                            addressee.appendChild(addresseeHostName);
                            addressee.appendChild(addresseeClientServiceName);
                            addressee.appendChild(addresseeLogin);

                            messageTag.appendChild(text);
                            messageTag.appendChild(author);
                            messageTag.appendChild(addressee);
                            root.appendChild(messageTag);

                            try {
                                Transformer tr = TransformerFactory.newInstance().newTransformer();
                                DOMSource source = new DOMSource(document);
                                FileOutputStream fos = new FileOutputStream("server/data/AllMessages.xml");
                                StreamResult result = new StreamResult(fos);
                                tr.transform(source, result);
                            } catch (TransformerException | IOException e) {
                                e.printStackTrace(System.out);
                            }
                        } catch (ParserConfigurationException | SAXException | IOException ex) {
                            ex.printStackTrace(System.out);
                        }
                    }

                    if (message instanceof GeneralMessage){
                        try {
                            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                            Document document = documentBuilder.parse("server/data/AllMessages.xml");
                            Node root = document.getDocumentElement();
                            Element messageTag = document.createElement("Message");
                            messageTag.setAttribute("type", "GeneralMessage");

                            Element text = document.createElement("Text");
                            text.setTextContent(message.getMessage());

                            Element author = document.createElement("Author");
                            Element authorName = document.createElement("Username");
                            authorName.setTextContent(((GeneralMessage) message).getAuthor().getName());
                            Element authorPassword = document.createElement("Password");
                            String authorPassString = new String(((GeneralMessage) message).getAuthor().getPassword());
                            authorPassword.setTextContent(authorPassString);
                            Element authorGender = document.createElement("Gender");
                            authorGender.setTextContent(((GeneralMessage) message).getAuthor().getGender());
                            Element authorHostName = document.createElement("HostName");
                            authorHostName.setTextContent(((GeneralMessage) message).getAuthor().getHostName());
                            Element authorClientServiceName = document.createElement("ClientServiceName");
                            authorClientServiceName.setTextContent(((GeneralMessage) message).getAuthor().getClientServiceName());
                            Element authorLogin = document.createElement("Login");
                            authorLogin.setTextContent(((GeneralMessage) message).getAuthor().getLogin());
                            author.appendChild(authorName);
                            author.appendChild(authorPassword);
                            author.appendChild(authorGender);
                            author.appendChild(authorHostName);
                            author.appendChild(authorClientServiceName);
                            author.appendChild(authorLogin);

                            messageTag.appendChild(text);
                            messageTag.appendChild(author);
                            root.appendChild(messageTag);

                            try {
                                Transformer tr = TransformerFactory.newInstance().newTransformer();
                                DOMSource source = new DOMSource(document);
                                FileOutputStream fos = new FileOutputStream("server/data/AllMessages.xml");
                                StreamResult result = new StreamResult(fos);
                                tr.transform(source, result);
                            } catch (TransformerException | IOException e) {
                                e.printStackTrace(System.out);
                            }
                        } catch (ParserConfigurationException | SAXException | IOException ex) {
                            ex.printStackTrace(System.out);
                        }
                    }


                }
            }

            messageUploaders.remove(this);
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

        ChatServer server = new ChatServerImpl();
        Naming.rebind("rmi://" + hostName + "/" + serviceName, server);
    }
}


