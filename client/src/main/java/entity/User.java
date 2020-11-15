package entity;

public class User {
    private String login;
    private String username;
    private String gender;

    public User(String login, String username, String gender) {
        this.login = login;
        this.username = username;
        this.gender = gender;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        String g;

        if (gender.equals("male")){
            g = "муж";
        } else {
            g = "жен";
        }

        return "<html>" + "<font size='3' style='bold'>" + username + "</font>" + "  " + "<font size = '2'>" + g + "</font>" + "</html>";
    }
}
