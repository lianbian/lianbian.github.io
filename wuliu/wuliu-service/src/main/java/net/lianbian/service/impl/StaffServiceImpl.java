package net.lianbian.service.impl;

import net.lianbian.dao.IStaffDao;
import net.lianbian.domain.Staff;
import net.lianbian.service.IStaffService;
import net.lianbian.utils.PageBean;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StaffServiceImpl implements IStaffService {
	@Autowired
	private IStaffDao dao;
	
	public void save(Staff model) {
		dao.add(model);
	}
	
	@Transactional(readOnly=true)
	public void pageQuery(PageBean pageBean) {
		dao.pageQuery(pageBean);
	}
	
	public void deleteBatch(String ids) {
		String[] idList = ids.split(",");
		for (String id : idList) {
			dao.executeUptate("staff.deleteBatch", id);
		}
	}
	
	public void update(Staff model) {
		dao.update(model);
	}
	
	@Transactional(readOnly=true)
	public Staff findById(String id) {
		return dao.findById(id);
	}
	
	@Transactional(readOnly=true)
	public List<Staff> findNotDeleteStaffList() {
		DetachedCriteria dc = DetachedCriteria.forClass(Staff.class);
		dc.add(Restrictions.eq("deltag", "0"));
		return dao.findByCriteria(dc);
	}

}
