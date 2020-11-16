package entity;


import interfaces.ChatClient;

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
    private String login;

    public User() {
    }

    public User(String name, char[] password, String gender, String hostName, String clientServiceName, String login,ChatClient client) {
        this.name = name;
        this.password = password;
        this.gender = gender;
        this.hostName = hostName;
        this.clientServiceName = clientServiceName;
        this.login = login;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        return Objects.equals(name, user.name) &&
                Arrays.equals(password, user.password) &&
                Objects.equals(gender, user.gender) &&
                Objects.equals(hostName, user.hostName) &&
                Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, gender, hostName, clientServiceName, client, login);
        result = 31 * result + Arrays.hashCode(password);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password=" + Arrays.toString(password) +
                ", gender='" + gender + '\'' +
                ", hostName='" + hostName + '\'' +
                ", clientServiceName='" + clientServiceName + '\'' +
                ", login='" + login + '\'' +
                '}';
    }
}
