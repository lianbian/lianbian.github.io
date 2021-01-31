package net.lianbian.service;

import net.lianbian.domain.Workordermanage;
import net.lianbian.utils.PageBean;

public interface IWorkordermanageService {
	/**
	 * 新增工单
	 * @param model
	 */
	public void add(Workordermanage model);

	public void pageQuery(PageBean pageBean);

}
