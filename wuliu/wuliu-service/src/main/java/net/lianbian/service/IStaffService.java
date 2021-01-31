package net.lianbian.service;

import net.lianbian.domain.Staff;
import net.lianbian.utils.PageBean;

import java.util.List;

public interface IStaffService {
	/**
	 * 添加派送员
	 * @param model
	 */
	public void save(Staff model);
	
	/**
	 * 分页查询业务
	 * @param pageBean
	 */
	public void pageQuery(PageBean pageBean);
	/**
	 * 批量删除取派员
	 * @param ids
	 */
	public void deleteBatch(String ids);
	/**
	 * 更新取派员
	 * @param model
	 */
	public void update(Staff model);
	/**
	 * 根据ID查找
	 * @param id
	 * @return
	 */
	public Staff findById(String id);
	/**
	 * 查找所有未被删除的派送员
	 * @return
	 */
	public List<Staff> findNotDeleteStaffList();

}
