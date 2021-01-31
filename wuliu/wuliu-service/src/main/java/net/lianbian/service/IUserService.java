package net.lianbian.service;

import net.lianbian.domain.Function;
import net.lianbian.domain.User;
import net.lianbian.utils.PageBean;

import java.util.List;

public interface IUserService {
	
	public User login(User user);
	
	public void editPassword(String id, String password);

	public void add(User model, String[] roleId);

	public void pageQuery(PageBean pageBean);

	public List<Function> findMenu();

}
