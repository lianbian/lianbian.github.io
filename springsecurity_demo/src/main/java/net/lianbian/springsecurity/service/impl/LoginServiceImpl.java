package net.lianbian.springsecurity.service.impl;

import net.lianbian.springsecurity.model.LoginDetail;
import net.lianbian.springsecurity.model.TokenDetail;
import net.lianbian.springsecurity.model.dao.UserMapper;
import net.lianbian.springsecurity.service.LoginService;
import net.lianbian.springsecurity.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    private final UserMapper userMapper;
    private final TokenUtils tokenUtils;

    @Autowired
    public LoginServiceImpl(UserMapper userMapper, TokenUtils tokenUtils) {
        this.userMapper = userMapper;
        this.tokenUtils = tokenUtils;
    }

    @Override
    public LoginDetail getLoginDetail(String username) {
        return userMapper.getUserFromDatabase(username);
    }

    @Override
    public String generateToken(TokenDetail tokenDetail) {
        return tokenUtils.generateToken(tokenDetail);
    }
}
