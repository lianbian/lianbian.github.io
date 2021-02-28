package net.lianbian.springsecurity.model.dao;

import net.lianbian.springsecurity.model.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserMapper {
    User getUserFromDatabase(@Param("username") String username);
}