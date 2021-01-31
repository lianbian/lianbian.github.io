package net.lianbian.dao.impl;

import net.lianbian.dao.IRegionDao;
import net.lianbian.dao.base.BaseDaoImpl;
import net.lianbian.domain.Region;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RegionDaoImpl extends BaseDaoImpl<Region> implements IRegionDao {

	public List<Region> findAllByQ(String q) {
		String hql = "FROM Region r WHERE province LIKE ? OR city LIKE ? OR district LIKE ? OR shortcode LIKE ? OR citycode LIKE ?";
		List<Region> list = (List<Region>) this.getHibernateTemplate().find(hql, "%"+q+"%", "%"+q+"%", "%"+q+"%", "%"+q+"%", "%"+q+"%");
		return list;
	}

	

}
