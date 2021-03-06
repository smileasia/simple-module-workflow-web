package net.simpleframework.workflow.web.component.comments;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.AbstractComponentRegistry;
import net.simpleframework.mvc.component.ComponentBean;
import net.simpleframework.mvc.component.ComponentHtmlRenderEx;
import net.simpleframework.mvc.component.ComponentName;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ComponentRender;
import net.simpleframework.mvc.component.ComponentResourceProvider;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@ComponentBean(WfCommentBean.class)
@ComponentName(WfCommentRegistry.WF_COMMENT)
@ComponentRender(WfCommentRender.class)
@ComponentResourceProvider(WfCommentResourceProvider.class)
public class WfCommentRegistry extends AbstractComponentRegistry {

	public static final String WF_COMMENT = "wf_comment";

	@Override
	public AbstractComponentBean createComponentBean(final PageParameter pp,
			final Object attriData) {
		final WfCommentBean commentBean = (WfCommentBean) super.createComponentBean(pp, attriData);
		ComponentHtmlRenderEx.createAjaxRequest(ComponentParameter.get(pp, commentBean));
		return commentBean;
	}
}
