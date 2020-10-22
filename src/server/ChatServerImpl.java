package server;

import client.ChatClient;
import entity.Message;
import entity.User;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ChatServerImpl implements ChatServer {

    public static final String UNIC_BINDING_NAME = "server";

    private List<User> users;
    private List<Message> messages;

    public ChatServerImpl() throws RemoteException
    {
        super();
        users = new ArrayList<>();
        messages = new ArrayList<>();
    }

    @Override
    public void connect(User user) {
        users.add(user);
        System.out.println(user.getName() + " добавлен");
    }

    @Override
    public void disconnect(User user) {
        users.remove(user);
        System.out.println(user.getName() + " удален");
    }

    @Override
    public void getPersonalMessage() {

    }

    @Override
    public void getGeneralMessage() {

    }



    public static void main (String[] args) throws RemoteException, AlreadyBoundException, InterruptedException {


        final ChatServer service = new ChatServerImpl();

        //создание реестра расшареных объетов
        final Registry registry = LocateRegistry.createRegistry(2099);
        //создание "заглушки" – приемника удаленных вызовов
        Remote stub = UnicastRemoteObject.exportObject(service, 0);
        //регистрация "заглушки" в реесте
        registry.bind(UNIC_BINDING_NAME, stub);

//усыпляем главный поток, иначе программа завершится
        Thread.sleep(Integer.MAX_VALUE);

    }
}
