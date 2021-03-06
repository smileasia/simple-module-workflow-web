package net.simpleframework.workflow.web.component.abort;

import net.simpleframework.common.StringUtils;
import net.simpleframework.workflow.web.component.AbstractWfActionBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ActivityAbortBean extends AbstractWfActionBean {
	private static final long serialVersionUID = 3601555925624541852L;

	@Override
	public boolean isRunImmediately() {
		return false;
	}

	@Override
	public String getHandlerClass() {
		final String sClass = super.getHandlerClass();
		return StringUtils.hasText(sClass) ? sClass : DefaultActivityAbortHandler.class.getName();
	}
}
