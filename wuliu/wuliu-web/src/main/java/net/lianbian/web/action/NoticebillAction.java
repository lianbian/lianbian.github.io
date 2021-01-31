package net.lianbian.web.action;

import net.lianbian.crm.Customer;
import net.lianbian.crm.ICustomerService;
import net.lianbian.domain.Noticebill;
import net.lianbian.domain.User;
import net.lianbian.service.INoticebillService;
import net.lianbian.utils.BosUtils;
import net.lianbian.web.action.base.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
public class NoticebillAction extends BaseAction<Noticebill> {
	
	@Autowired
	private ICustomerService customerService;             //客户服务
	@Autowired
	private INoticebillService noticebillService;
	
	public String findCustomerByTelephone() {
		Customer customer = customerService.findCustomerByTelephone(model.getTelephone());
		this.Object2JsonString(customer, new String[] {"workbills"});
		return NONE;
	}
	//添加新单
	
	public String add() {
		//获得当前session中的user
		User user = BosUtils.getCurrentLoginUser();
		model.setUser(user);
		//调用service层方法进行添加新单的业务
		noticebillService.add(model);
		return SUCCESS;
	}
}
