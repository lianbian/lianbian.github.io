package net.lianbian.springsecurity.model;

public interface LoginDetail {
    String getUsername();
    String getPassword();
    boolean enable();
}
