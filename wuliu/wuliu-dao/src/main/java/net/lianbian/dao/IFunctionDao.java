package net.lianbian.dao;

import net.lianbian.dao.base.IBaseDao;
import net.lianbian.domain.Function;

import java.util.List;

/**
 * @author ruanwenjun E-mail:861923274@qq.com
 * @date 2018年3月30日 下午3:37:15
*/
public interface IFunctionDao extends IBaseDao<Function> {

	public List<Function> findAllMenu();

	public List<Function> findFunctionByUserID(String id);

	public List<Function> findAuthorizationByUserID(String id);

	public List<Function> findAllFirstFunction();

}
