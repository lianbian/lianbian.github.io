package net.lianbian.springsecurity.service;

import net.lianbian.springsecurity.model.LoginDetail;
import net.lianbian.springsecurity.model.TokenDetail;

public interface LoginService {
    LoginDetail getLoginDetail(String username);

    String generateToken(TokenDetail tokenDetail);
}
