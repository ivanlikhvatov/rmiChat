package entity;

import client.ChatClient;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class User implements Serializable {
    private String name;
    private char[] password;
    private String gender;
    private String hostName;
    private String clientServiceName;
    private ChatClient client;

    public User() {
    }

    public User(String name, char[] password, String gender, String hostName, String clientServiceName, ChatClient client) {
        this.name = name;
        this.password = password;
        this.gender = gender;
        this.hostName = hostName;
        this.clientServiceName = clientServiceName;
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getClientServiceName() {
        return clientServiceName;
    }

    public void setClientServiceName(String clientServiceName) {
        this.clientServiceName = clientServiceName;
    }

    public ChatClient getClient() {
        return client;
    }

    public void setClient(ChatClient client) {
        this.client = client;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return name.equals(user.name) &&
                Arrays.equals(password, user.password) &&
                gender.equals(user.gender) &&
                hostName.equals(user.hostName) &&
                clientServiceName.equals(user.clientServiceName) &&
                client.equals(user.client);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, gender, hostName, clientServiceName, client);
        result = 31 * result + Arrays.hashCode(password);
        return result;
    }
}
