package net.simpleframework.workflow.web.component.fallback;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.simpleframework.mvc.component.AbstractComponentHandler;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.workflow.engine.IWorkflowContextAware;
import net.simpleframework.workflow.engine.bean.ActivityBean;
import net.simpleframework.workflow.schema.AbstractTaskNode;
import net.simpleframework.workflow.schema.UserNode;
import net.simpleframework.workflow.web.WorkflowUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultActivityFallbackHandler extends AbstractComponentHandler implements
		IActivityFallbackHandler, IWorkflowContextAware {

	@Override
	public Collection<UserNode> getUserNodes(final ComponentParameter cp) {
		final ActivityBean activity = WorkflowUtils.getActivityBean(cp);
		final Map<String, UserNode> cache = new LinkedHashMap<String, UserNode>();
		ActivityBean pre = activity;
		while ((pre = wfaService.getPreActivity(pre)) != null) {
			final AbstractTaskNode tasknode = wfaService.getTaskNode(pre);
			UserNode usernode;
			if (tasknode instanceof UserNode && !(usernode = (UserNode) tasknode).isFallback()) {
				cache.put(tasknode.getName(), usernode);
			}
		}
		return cache.values();
	}
}