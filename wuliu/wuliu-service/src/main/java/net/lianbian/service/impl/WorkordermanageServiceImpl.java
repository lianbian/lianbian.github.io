package net.lianbian.service.impl;

import net.lianbian.dao.IWorkordermanageDao;
import net.lianbian.domain.Workordermanage;
import net.lianbian.service.IWorkordermanageService;
import net.lianbian.utils.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkordermanageServiceImpl implements IWorkordermanageService {
	
	@Autowired
	private IWorkordermanageDao workordermanageDao;
	
	
	public void add(Workordermanage model) {
		workordermanageDao.add(model);
	}


	public void pageQuery(PageBean pageBean) {
		workordermanageDao.pageQuery(pageBean);
	}

}
