package entity;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private String name;
    private String password;
    private String gender;

    public User() {
    }

    public User(String name, String password, String gender) {
        this.name = name;
        this.password = password;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return name.equals(user.name) &&
                password.equals(user.password) &&
                gender.equals(user.gender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, password, gender);
    }
}
