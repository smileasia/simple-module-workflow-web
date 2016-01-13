package net.simpleframework.workflow.web.page.list.process;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.workflow.engine.IWorkflowContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IProcessWorksHandler extends IWorkflowContextAware {

	/**
	 * 获取要操作的流程模型
	 * 
	 * @return
	 */
	String getModelName();

	/**
	 * 表格初始化
	 * 
	 * @param pp
	 * @param tablePager
	 * @param qw
	 */
	void doTablePagerInit(PageParameter pp, TablePagerBean tablePager, EProcessWorks qw);

	/**
	 * 获取左侧元素列表
	 * 
	 * @param pp
	 * @param qw
	 * @return
	 */
	ElementList getLeftElements(PageParameter pp, EProcessWorks qw);

	public static enum EProcessWorks {
		my,

		dept,

		org,

		role
	}
}