package net.lianbian.service.impl;

import net.lianbian.dao.IDecidedzoneDao;
import net.lianbian.dao.ISubareaDao;
import net.lianbian.domain.Decidedzone;
import net.lianbian.domain.Subarea;
import net.lianbian.service.IDecidedzoneService;
import net.lianbian.utils.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class DecidedzoneService implements IDecidedzoneService {
	@Autowired
	private IDecidedzoneDao decidedzoneDao;
	@Autowired
	private ISubareaDao subareaDao;

	public void save(Decidedzone model, String[] subareaId) {
		//定区放弃了关系维护，所以需要分区来维护关系
		//保存定区
		decidedzoneDao.add(model);
		for (String id : subareaId) {
			Subarea subarea = subareaDao.findById(id);
			//保存定区即关联了，会自动insert
			subarea.setDecidedzone(model);
		}

	}

	public void pageQuery(PageBean pageBean) {
		decidedzoneDao.pageQuery(pageBean);
	}

}
