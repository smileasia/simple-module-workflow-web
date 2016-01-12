package net.simpleframework.workflow.web.component.startprocess;

import net.simpleframework.mvc.component.AbstractComponentRender.ComponentBaseActionJavascriptRender;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ComponentUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class StartProcessRender extends ComponentBaseActionJavascriptRender {

	@Override
	protected String getParams(final ComponentParameter cp) {
		return StartProcessUtils.toParams(cp, null);
	}

	@Override
	protected String getActionPath(final ComponentParameter cp) {
		return ComponentUtils.getResourceHomePath(StartProcessBean.class) + "/jsp/start_process.jsp";
	}
}
