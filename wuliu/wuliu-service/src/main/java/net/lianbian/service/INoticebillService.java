package net.lianbian.service;

import net.lianbian.domain.Noticebill;

public interface INoticebillService {
	/**
	 * 为新的通知单处理业务
	 * @param model
	 */
	public void add(Noticebill model);

}
