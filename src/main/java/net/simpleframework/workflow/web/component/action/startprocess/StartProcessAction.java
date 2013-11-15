package net.simpleframework.workflow.web.component.action.startprocess;

import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.DefaultAjaxRequestHandler;
import net.simpleframework.workflow.engine.InitiateItem;
import net.simpleframework.workflow.engine.ProcessModelBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class StartProcessAction extends DefaultAjaxRequestHandler {

	public IForward doStartProcess(final ComponentParameter cp) {
		final ComponentParameter nCP = StartProcessUtils.get(cp);
		final InitiateItem initiateItem = StartProcessUtils.getInitiateItem(nCP);
		final String initiator = nCP.getParameter("initiator");
		if (StringUtils.hasText(initiator)) {
			final ID selected = ID.of(initiator);
			initiateItem.setSelectedRoleId(selected);
		}
		return new JavascriptForward(StartProcessUtils.jsStartProcessCallback(nCP, initiateItem));
	}

	public IForward doTransitionSave(final ComponentParameter cp) {
		final InitiateItem initiateItem = StartProcessUtils.getInitiateItem(cp);
		final String[] transitions = StringUtils.split(cp.getParameter("transitions"));
		initiateItem.resetTransitions(transitions);

		final ComponentParameter nCP = StartProcessUtils.get(cp);
		final JavascriptForward js = new JavascriptForward();
		if (initiateItem.getInitiateRoles().size() > 1) {
			js.append("$Actions['process_transition_manual_window'].close();");
			js.append("$Actions['initiator_select_window']('").append(StartProcessUtils.BEAN_ID)
					.append("=").append(nCP.hashId()).append("&").append(ProcessModelBean.modelId)
					.append("=").append(nCP.getParameter(ProcessModelBean.modelId)).append("');");
		} else {
			js.append(StartProcessUtils.jsStartProcessCallback(nCP, initiateItem));
		}
		return js;
	}
}
