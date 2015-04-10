package net.simpleframework.workflow.web;

import static net.simpleframework.common.I18n.$m;

import java.util.LinkedHashSet;
import java.util.Set;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectEx.IVal;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.workflow.engine.ActivityBean;
import net.simpleframework.workflow.engine.EWorkitemStatus;
import net.simpleframework.workflow.engine.IWorkflowServiceAware;
import net.simpleframework.workflow.engine.ProcessBean;
import net.simpleframework.workflow.engine.WorkitemBean;
import net.simpleframework.workflow.engine.WorkviewBean;
import net.simpleframework.workflow.engine.participant.Participant;
import net.simpleframework.workflow.web.page.AbstractItemsTPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class WorkflowUtils implements IWorkflowServiceAware {

	public static String getProcessTitle(final ProcessBean process) {
		if (process == null) {
			return "";
		}
		final String title = process.getTitle();
		return StringUtils.hasText(title) ? title : $m("WorkflowUtils.0");
	}

	public static ButtonElement createLogButton() {
		return ButtonElement.logBtn().setDisabled(
				((IWorkflowWebContext) workflowContext).getLogRef() == null);
	}

	public static String toStatusHTML(final PageParameter pp, final Enum<?> status, final Object txt) {
		final StringBuilder sb = new StringBuilder();
		sb.append(new ImageElement(pp.getCssResourceHomePath(AbstractItemsTPage.class)
				+ "/images/status_" + status.name() + ".png").setClassName("icon16").addStyle(
				"margin: 0 4px;"));
		sb.append(new SpanElement(txt != null ? txt : status.toString()).setClassName("icon_txt"));
		return sb.toString();
	}

	public static String toStatusHTML(final PageParameter pp, final Enum<?> status) {
		return toStatusHTML(pp, status, null);
	}

	public static String getParticipants(final ActivityBean activity, final boolean r) {
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (final Participant p : (r ? aService.getParticipants2(activity) : aService
				.getParticipants(activity, true))) {
			if (i++ > 0) {
				sb.append(", ");
			}
			sb.append(permission.getUser(p.userId).getText());
		}
		return sb.toString();
	}

	public static String getUserTo(final ActivityBean activity) {
		return activity.getAttrCache("to_" + activity.getId(), new IVal<String>() {
			@Override
			public String get() {
				final Set<String> list = new LinkedHashSet<String>();
				for (final ActivityBean nextActivity : aService.getNextActivities(activity)) {
					for (final WorkitemBean workitem : wService.getWorkitems(nextActivity)) {
						list.add(workitem.getUserText());
					}
				}
				return list.size() > 0 ? StringUtils.join(list, ", ") : null;
			}
		});
	}

	public static String getUserFrom(final ActivityBean activity) {
		final ActivityBean preActivity = aService.getPreActivity(activity);
		if (preActivity == null) {
			return null;
		}
		return activity.getAttrCache("from_" + preActivity.getId(), new IVal<String>() {
			@Override
			public String get() {
				final Set<String> list = new LinkedHashSet<String>();
				for (final WorkitemBean workitem : wService.getWorkitems(preActivity,
						EWorkitemStatus.complete)) {
					list.add(workitem.getUserText());
				}
				return list.size() > 0 ? StringUtils.join(list, ", ") : null;
			}
		});
	}

	public static WorkitemBean getWorkitemBean(final PageParameter pp) {
		return pp.getRequestCache("@WorkitemBean", new IVal<WorkitemBean>() {
			@Override
			public WorkitemBean get() {
				return wService.getBean(pp.getParameter("workitemId"));
			}
		});
	}

	public static WorkviewBean getWorkviewBean(final PageParameter pp) {
		return pp.getRequestCache("@WorkviewBean", new IVal<WorkviewBean>() {
			@Override
			public WorkviewBean get() {
				return vService.getBean(pp.getParameter("WorkviewId"));
			}
		});
	}

	public static ProcessBean getProcessBean(final PageParameter pp) {
		return pp.getRequestCache("@ProcessBean", new IVal<ProcessBean>() {
			@Override
			public ProcessBean get() {
				return pService.getBean(pp.getParameter("processId"));
			}
		});
	}
}
