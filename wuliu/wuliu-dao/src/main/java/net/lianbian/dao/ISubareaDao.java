package net.lianbian.dao;

import net.lianbian.dao.base.IBaseDao;
import net.lianbian.domain.Subarea;

import java.util.List;

public interface ISubareaDao extends IBaseDao<Subarea> {

	public List<Object> findSubareaCountInRegion();

}
