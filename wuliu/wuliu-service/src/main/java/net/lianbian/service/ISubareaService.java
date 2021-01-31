package net.lianbian.service;

import net.lianbian.domain.Subarea;
import net.lianbian.utils.PageBean;

import java.util.List;

/**
 * 分区管理
 * @author RUANWENJUN
 *
 */
public interface ISubareaService {
	
	/**
	 * 添加分区
	 * @param model
	 */
	public void add(Subarea model);
	
	/**
	 * 分区分页查询
	 * @param pageBean
	 */
	public void pageQuery(PageBean pageBean);
	
	/**
	 * 查询所有分区
	 * @return
	 */
	public List<Subarea> findAll();
	/**
	 * 一键导入
	 * @param list
	 */
	public void saveBatch(List<Subarea> list);
	/**
	 * 找到所有没有定区的分区
	 * @return
	 */
	public List<Subarea> finaNoDecidedzoneList();
	/**
	 * 查找关联指定定区的分区
	 * @param decizedzoneId
	 * @return
	 */
	public List<Subarea> findAllByDecideczoneId(String decizedzoneId);

	public List<Object> findSubareaCountInRegion();

}
