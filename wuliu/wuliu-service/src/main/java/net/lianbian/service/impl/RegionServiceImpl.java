package net.lianbian.service.impl;

import net.lianbian.dao.IRegionDao;
import net.lianbian.domain.Region;
import net.lianbian.service.IRegionService;
import net.lianbian.utils.PageBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class RegionServiceImpl implements IRegionService {
	@Autowired
	private IRegionDao regionDao;
	
	public void saveBatch(List<Region> regionList) {
		//遍历regionList，对每一个region都执行一次saveOrUpdata操作
		for (Region region : regionList) {
			regionDao.saveOrUpdate(region);
		}
	}
	
	@Transactional(readOnly=true)
	public void pageQuery(PageBean pageBean) {
		regionDao.pageQuery(pageBean);
	}
	
	@Transactional(readOnly=true)
	public List<Region> finaAllRegion(String q) {
		List<Region> regionList = null;
		if(StringUtils.isNoneBlank(q)) {
			//当Q部位空的时候调用模糊查询方法
			regionList = regionDao.findAllByQ(q);
		}else {
			//查询全部
			regionList = regionDao.findAll();
		}
		return regionList;
	}

}
