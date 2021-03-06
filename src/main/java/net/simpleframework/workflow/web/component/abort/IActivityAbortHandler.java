package net.simpleframework.workflow.web.component.abort;

import java.util.List;

import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.IComponentHandler;
import net.simpleframework.workflow.engine.IWorkflowContextAware;
import net.simpleframework.workflow.engine.bean.ActivityBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IActivityAbortHandler extends IComponentHandler, IWorkflowContextAware {

	/**
	 * 获取要放弃的环节实例
	 * 
	 * @param cp
	 * @return
	 */
	List<ActivityBean> getActivities(ComponentParameter cp);

	JavascriptForward doAbort(ComponentParameter cp, List<ActivityBean> list);
}
