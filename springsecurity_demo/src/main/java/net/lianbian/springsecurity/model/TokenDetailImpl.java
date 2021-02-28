package net.lianbian.springsecurity.model;

public class TokenDetailImpl implements TokenDetail{
    private final String username;

    public TokenDetailImpl(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
