package net.lianbian.mall.tiny.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * Created by macro on 2019/4/8.
 */
@Configuration
@MapperScan({"net.lianbian.mall.tiny.mbg.mapper", "net.lianbian.mall.tiny.dao"})
public class MyBatisConfig {
}
