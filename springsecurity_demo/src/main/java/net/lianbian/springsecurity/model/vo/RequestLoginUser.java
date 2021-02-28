package net.lianbian.springsecurity.model.vo;

import com.sun.istack.internal.NotNull;

public class RequestLoginUser {
    @NotNull
    private String username;

    @NotNull
    private String password;

    public RequestLoginUser() {

    }

    public String getUsername() {
        return username;
    }

    public RequestLoginUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword(){
        return password;
    }

    public RequestLoginUser setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString(){
        return "RequestLoginUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
