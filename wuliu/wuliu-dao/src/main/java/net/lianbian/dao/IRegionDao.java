package net.lianbian.dao;

import net.lianbian.dao.base.IBaseDao;
import net.lianbian.domain.Region;

import java.util.List;

public interface IRegionDao extends IBaseDao<Region> {
	
	/**
	 * 根据Q进行模糊查询
	 * @param q
	 * @return
	 */
	List<Region> findAllByQ(String q);
	
}
