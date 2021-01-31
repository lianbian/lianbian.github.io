package net.lianbian.dao.impl;

import net.lianbian.dao.IUserDao;
import net.lianbian.dao.base.BaseDaoImpl;
import net.lianbian.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDaoImpl extends BaseDaoImpl<User> implements IUserDao {

	public User findUserByUsernameAndPassword(String username, String userpassword) {
		String hql ="FROM User where username=? AND password =?";
		List<User> list = (List<User>) getHibernateTemplate().find(hql, username,userpassword);
		if(list !=null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}
	//通过用户名
	public User findUserByUsernameAndPassword(String username) {
		String hql ="FROM User where username=?";
		List<User> list = (List<User>) getHibernateTemplate().find(hql, username);
		if(list !=null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}



	

}
