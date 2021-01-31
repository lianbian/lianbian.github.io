package net.lianbian.service;

import net.lianbian.domain.Function;
import net.lianbian.utils.PageBean;

import java.util.List;

/**
 * @author ruanwenjun E-mail:861923274@qq.com
 * @date 2018年3月30日 下午3:33:23
*/
public interface IFunctionService {

	public void save(Function model);

	public List<Function> findAllFirstFunction();

	public void pageQuery(PageBean pageBean);

	public List<Function> findAll();

}
