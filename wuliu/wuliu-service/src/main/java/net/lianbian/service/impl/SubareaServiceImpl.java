package net.lianbian.service.impl;

import net.lianbian.dao.IRegionDao;
import net.lianbian.dao.ISubareaDao;
import net.lianbian.domain.Region;
import net.lianbian.domain.Subarea;
import net.lianbian.service.ISubareaService;
import net.lianbian.utils.PageBean;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class SubareaServiceImpl implements ISubareaService {
	
	@Autowired
	private ISubareaDao subareaDao;
	@Autowired
	private IRegionDao regionDao;
	
	public void add(Subarea model) {
		subareaDao.add(model);
	}
	
	
	
	@Transactional(readOnly=true)
	public void pageQuery(PageBean pageBean) {
		subareaDao.pageQuery(pageBean);
	}
	
	
	
	@Transactional(readOnly=true)
	public List<Subarea> findAll() {
		return subareaDao.findAll();
	}


	
	public void saveBatch(List<Subarea> list) {
		for (Subarea subarea : list) {
			Region region = regionDao.findById(subarea.getRegion().getId());
			subarea.setRegion(region);
			subareaDao.saveOrUpdate(subarea);
		}
	}



	@Transactional(readOnly=true)
	public List<Subarea> finaNoDecidedzoneList() {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Subarea.class);
		//所属定区为空
		detachedCriteria.add(Restrictions.isNull("decidedzone"));
		return subareaDao.findByCriteria(detachedCriteria);
	}



	public List<Subarea> findAllByDecideczoneId(String decizedzoneId) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Subarea.class);
		detachedCriteria.add(Restrictions.eq("decidedzone.id", decizedzoneId));
		return subareaDao.findByCriteria(detachedCriteria );
	}

	public List<Object> findSubareaCountInRegion() {
		return subareaDao.findSubareaCountInRegion();
	}

}
