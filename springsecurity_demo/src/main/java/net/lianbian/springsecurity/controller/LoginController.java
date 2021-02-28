package net.lianbian.springsecurity.controller;

import net.lianbian.springsecurity.model.LoginDetail;
import net.lianbian.springsecurity.model.ResultMap;
import net.lianbian.springsecurity.model.TokenDetail;
import net.lianbian.springsecurity.model.vo.Data;
import net.lianbian.springsecurity.model.vo.RequestLoginUser;
import net.lianbian.springsecurity.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    private final LoginService loginService;

    @Value("${token.header}")
    private String tokenHeader;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResultMap login(@Validated RequestLoginUser requestLoginUser, BindingResult bindingResult){
        if(bindingResult.hasErrors()) {
            return new ResultMap().fail("400").message("缺少参数或者格式不对").data("");
        }

        LoginDetail loginDetail = loginService.getLoginDetail(requestLoginUser.getUsername());
        ResultMap ifLoginFail = checkAccount(requestLoginUser, loginDetail);
        if(ifLoginFail!=null) {
            return ifLoginFail;
        }

        return new ResultMap().success().message("").data(new Data().addObj(tokenHeader, loginService.generateToken((TokenDetail) loginDetail)));
    }

    private ResultMap checkAccount(RequestLoginUser requestLoginUser, LoginDetail loginDetail) {

        if(loginDetail == null) {
            return new ResultMap().fail("434").message("账号不存在").data("");
        }

        if(loginDetail.enable() == false) {
            return new ResultMap().fail("452").message("账号在黑名单中").data("");
        }

        if(!loginDetail.getPassword().equals(requestLoginUser.getPassword())) {
            return new ResultMap().fail("438").message("密码错误").data("");
        }

        return null;
    }
}
