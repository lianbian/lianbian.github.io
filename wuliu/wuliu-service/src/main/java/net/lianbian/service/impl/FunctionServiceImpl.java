package net.lianbian.service.impl;

import net.lianbian.dao.IFunctionDao;
import net.lianbian.domain.Function;
import net.lianbian.service.IFunctionService;
import net.lianbian.utils.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FunctionServiceImpl implements IFunctionService {
	@Autowired
	private IFunctionDao functionDao;
	public void save(Function model) {
		Function parentFunction = model.getParentFunction();
		if(parentFunction != null && parentFunction.getId().equals("")) {
			model.setParentFunction(null);
		}
		functionDao.add(model);
	}
	//查找所有一级权限
	public List<Function> findAllFirstFunction() {
		return functionDao.findAllFirstFunction();
	}

	public void pageQuery(PageBean pageBean) {
		functionDao.pageQuery(pageBean);
	}
	//查找所有权限
	public List<Function> findAll() {
		List<Function> list = functionDao.findAll();
		return list;
	}

}
