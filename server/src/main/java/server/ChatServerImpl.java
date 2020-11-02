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
        List<String> namesOfActiveUsers = getUserList();

        for(User user : this.activeUsers){
            try {
                user.getClient().updateUserList(namesOfActiveUsers);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private List<String> getUserList(){
        List<String> namesOfActiveUsers = new ArrayList<>();

        for (User user: activeUsers) {
            namesOfActiveUsers.add(user.getName());
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
    public void getPersonalMessage() {

    }

    @Override
    public void getGeneralMessage() {

    }



    public static void main (String[] args) throws RemoteException, AlreadyBoundException, InterruptedException, MalformedURLException {
//        final ChatServer service = new ChatServerImpl();
//
//        //создание реестра расшареных объетов
//        final Registry registry = LocateRegistry.createRegistry(2099);
//        //создание "заглушки" – приемника удаленных вызовов
//        Remote stub = UnicastRemoteObject.exportObject(service, 0);
//        //регистрация "заглушки" в реесте
//        registry.bind(UNIC_BINDING_NAME, stub);
//
////усыпляем главный поток, иначе программа завершится
//        Thread.sleep(Integer.MAX_VALUE);

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
