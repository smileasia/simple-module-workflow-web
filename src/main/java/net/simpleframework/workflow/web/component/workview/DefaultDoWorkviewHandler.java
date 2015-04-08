package net.simpleframework.workflow.web.component.workview;

import static net.simpleframework.common.I18n.$m;

import java.util.List;

import net.simpleframework.common.ID;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.component.AbstractComponentHandler;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.workflow.engine.IWorkflowServiceAware;
import net.simpleframework.workflow.engine.WorkviewBean;
import net.simpleframework.workflow.web.WorkflowUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultDoWorkviewHandler extends AbstractComponentHandler implements
		IDoWorkviewHandler, IWorkflowServiceAware {

	@Override
	public JavascriptForward doSent(final ComponentParameter cp, final List<ID> ids) {
		final List<WorkviewBean> list = vService.createWorkviews(WorkflowUtils.getWorkitemBean(cp),
				ids.toArray(new ID[ids.size()]));
		final JavascriptForward js = new JavascriptForward();
		js.append("alert('").append($m("DefaultDoWorkviewHandler.0", list.size())).append("');");
		js.append("$Actions['").append(cp.getComponentName()).append("_win'].close();");
		return js;
	}
}
