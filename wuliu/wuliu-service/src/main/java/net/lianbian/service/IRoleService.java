package net.lianbian.service;

import net.lianbian.domain.Role;
import net.lianbian.utils.PageBean;

import java.util.List;

/**
 * @author ruanwenjun E-mail:861923274@qq.com
 * @date 2018年4月1日 上午10:59:59
*/
public interface IRoleService {

	public void add(String functionIds, Role model);

	public void pageQuery(PageBean pageBean);

	public List<Role> findAll();

}
