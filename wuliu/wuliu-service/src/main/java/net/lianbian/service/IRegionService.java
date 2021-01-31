package net.lianbian.service;

import net.lianbian.domain.Region;
import net.lianbian.utils.PageBean;

import java.util.List;

public interface IRegionService {
	/**
	 * 一键保存区域
	 * @param regionList
	 */
	public void saveBatch(List<Region> regionList);
	
	/**
	 * 分页查找区域
	 * @param pageBean
	 */
	public void pageQuery(PageBean pageBean);
	
	/**
	 * 查找所有区域
	 * @param q 模糊查询的参数
	 * @return 
	 */
	public List<Region> finaAllRegion(String q);

}
