package net.lianbian.dao;

import net.lianbian.dao.base.IBaseDao;
import net.lianbian.domain.User;

public interface IUserDao extends IBaseDao<User> {
	/**
	 * 根据用户名和密码查找用户
	 * @param username
	 * @param userpassword
	 * @return
	 */
	public User findUserByUsernameAndPassword(String username, String userpassword);

	public User findUserByUsernameAndPassword(String username);

	

}
