package net.lianbian.dao.impl;

import net.lianbian.dao.ISubareaDao;
import net.lianbian.dao.base.BaseDaoImpl;
import net.lianbian.domain.Subarea;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubareaDaoImpl extends BaseDaoImpl<Subarea> implements ISubareaDao {
	//查询各个定区的分区数目
	public List<Object> findSubareaCountInRegion() {
		String hql = "select r.province ,Count(*) from Subarea s inner join s.region r "
						+ " group by r.province";
		List<Object> list = (List<Object>) this.getHibernateTemplate().find(hql);
		return list;
	}

	
}
