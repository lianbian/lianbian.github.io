package net.lianbian.web.action;

import net.lianbian.domain.Function;
import net.lianbian.service.IFunctionService;
import net.lianbian.web.action.base.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Scope("prototype")
public class FunctionAction extends BaseAction<Function> {
	@Autowired
	private IFunctionService functionService;
	
	//查找所有一级权限权限,以ajax形式返回（针对下拉box）
	public String ajaxList() {
		List<Function> list = functionService.findAllFirstFunction();
		this.Object2JsonString(list, new String[] {"roles","parentFunction","description","generatemenu","code","page"});
		return NONE;
	}
	//添加权限
	public String add() {
		functionService.save(model);
		return SUCCESS;
	}
	//分页查找权限
	public String pageQuery() {
		int currentPage = Integer.parseInt(model.getPage());
		pageBean.setCurrentPage(currentPage);
		functionService.pageQuery(pageBean);
		this.Object2JsonString(pageBean, new String[] {"currentPage","pageSize","detachedCriteria","parentFunction","roles",
										"children","code"});
		return NONE;
	}
	//查找所有的权限，以AJAX返回(针对树)
	public String findAll() {
		List<Function> list = functionService.findAll();
		this.Object2JsonString(list, new String[] {"roles","parentFunction","description","generatemenu","code","page","children"});
		return NONE;
	}
}
