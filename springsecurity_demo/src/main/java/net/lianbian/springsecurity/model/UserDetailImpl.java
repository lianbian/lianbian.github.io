package net.lianbian.springsecurity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;

public class UserDetailImpl implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    // 开始定义必要的属性
    private String id;
    private String username;
    private String password;
    private Date lastPasswordReset;
    private Collection<? extends GrantedAuthority> authorities;
    private Boolean enabled;
    // 必要的属性定义完毕
    //开始定义 UserDetails 必要的属性，因为不打算启用这些限制条件，所以不对这些条件做限制，全部设置为 true （通过）
    private Boolean accountNonExpired = true;
    private Boolean accountNonLocked = true;
    private Boolean credentialsNonExpired = true;

    public UserDetailImpl() {
        super();
    }

    public UserDetailImpl(String id, String username, String password, Date lastPasswordReset,
                          Collection<? extends GrantedAuthority> authorities, Boolean enabled) {
        this.setId(id);
        this.setUsername(username);
        this.setPassword(password);
        this.setLastPasswordReset(lastPasswordReset);
        this.setAuthorities(authorities);
        this.enabled = enabled;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public Date getLastPasswordReset() {
        return this.lastPasswordReset;
    }

    public void setLastPasswordReset(Date lastPasswordReset) {
        this.lastPasswordReset = lastPasswordReset;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.getAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.getAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return  this.getCredentialsNonExpired();
    }

    @JsonIgnore
    public Boolean getCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @JsonIgnore
    public Boolean getEnabled() {
        return this.enabled;
    }

    @JsonIgnore
    public Boolean getAccountNonLocked() {
        return this.accountNonLocked;
    }

    @JsonIgnore
    public Boolean getAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.getEnabled();
    }
}
