package net.lianbian.service.impl;

import net.lianbian.dao.IRoleDao;
import net.lianbian.domain.Function;
import net.lianbian.domain.Role;
import net.lianbian.service.IRoleService;
import net.lianbian.utils.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleServiceImpl implements IRoleService {
	@Autowired
	private IRoleDao roleDao;
	
	//添加角色，并赋予权限
	public void add(String functionIds, Role model) {
		roleDao.add(model);
		//解析权限id
		String[] split = functionIds.split(",");
		for (String string : split) {
			Function f = new Function(string);
			model.getFunctions().add(f);
		}
	}

	//分页查找角色
	public void pageQuery(PageBean pageBean) {
		roleDao.pageQuery(pageBean);
	}
	
	//查找所有角色
	public List<Role> findAll() {
		List<Role> list = roleDao.findAll();
		return list;
	}
}
