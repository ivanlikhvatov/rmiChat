package entity;

public class UserLoginAndName {
    private String login;
    private String username;

    public UserLoginAndName(String login, String username) {
        this.login = login;
        this.username = username;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return username;
    }
}
