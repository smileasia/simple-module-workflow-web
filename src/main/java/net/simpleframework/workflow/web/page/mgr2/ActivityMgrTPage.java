package net.simpleframework.workflow.web.page.mgr2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.workflow.engine.EActivityStatus;
import net.simpleframework.workflow.engine.bean.ActivityBean;
import net.simpleframework.workflow.engine.bean.ProcessBean;
import net.simpleframework.workflow.web.WorkflowLogRef.ActivityUpdateLogPage;
import net.simpleframework.workflow.web.WorkflowUtils;
import net.simpleframework.workflow.web.page.t1.AbstractWorkflowMgrPage;
import net.simpleframework.workflow.web.page.t1.ActivityMgrPage;
import net.simpleframework.workflow.web.page.t1.ActivityMgrPage.ActivityStatusDescPage;
import net.simpleframework.workflow.web.page.t1.ActivityTbl;
import net.simpleframework.workflow.web.page.t1.WorkitemsMgrPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ActivityMgrTPage extends AbstractWorkflowMgrTPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addTablePagerBean(pp);

		// workitems
		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "ActivityMgrTPage_workitems_page",
				WorkitemsMgrPage.class);
		addWindowBean(pp, "ActivityMgrTPage_workitems", ajaxRequest).setWidth(800).setHeight(480);
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"ActivityMgrTPage_tbl").setPagerBarLayout(EPagerBarLayout.none)
				.setContainerId("idActivityMgrTPage_tbl").setHandlerClass(_ActivityTbl.class);
		tablePager.addColumn(ActivityMgrPage.TC_TASKNODE())
				.addColumn(AbstractWorkflowMgrPage.TC_STATUS(EActivityStatus.class))
				.addColumn(ActivityMgrPage.TC_PARTICIPANTS())
				.addColumn(ActivityMgrPage.TC_PARTICIPANTS2())
				.addColumn(AbstractWorkflowMgrPage.TC_CREATEDATE())
				.addColumn(ActivityMgrPage.TC_TIMEOUT())
				.addColumn(AbstractWorkflowMgrPage.TC_COMPLETEDATE())
				// .addColumn(ActivityMgrPage.TC_PREVIOUS())
				.addColumn(TablePagerColumn.OPE(70));
		return tablePager;
	}

	@Override
	protected Class<? extends AbstractMVCPage> getUpdateLogPage() {
		return ActivityUpdateLogPage.class;
	}

	@Override
	protected Class<? extends AbstractMVCPage> getStatusDescPage() {
		return _ActivityStatusDescPage.class;
	}

	@Override
	protected SpanElement createOrgElement(final PageParameter pp) {
		final SpanElement oele = super.createOrgElement(pp);
		final ProcessBean process = WorkflowUtils.getProcessBean(pp);
		if (process != null) {
			oele.setText(oele.getText() + " - " + WorkflowUtils.getProcessTitle(process));
		}
		return oele;
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='tbar'>");
		final ProcessBean process = WorkflowUtils.getProcessBean(pp);
		sb.append(ElementList.of(LinkButton.backBtn().setOnclick(
				JS.loc(uFactory.getUrl(pp, ProcessMgrTPage.class, "modelId="
						+ (process != null ? process.getModelId() : ""))))));
		sb.append("</div>");
		sb.append(toMonitorHTML(pp));
		return sb.toString();
	}

	protected String toMonitorHTML(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div id='idActivityMgrTPage_tbl'></div>");
		return sb.toString();
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final ProcessBean process = WorkflowUtils.getProcessBean(pp);
		final String params = "processId=" + (process != null ? process.getId() : "");
		return TabButtons.of(
				new TabButton($m("ActivityMgrPage.7"), uFactory.getUrl(pp, ActivityMgrTPage.class,
						params)),
				new TabButton($m("ActivityMgrPage.8"), uFactory.getUrl(pp, ActivityGraphMgrTPage.class,
						params)));
	}

	public static class _ActivityStatusDescPage extends ActivityStatusDescPage {
	}

	public static class _ActivityTbl extends ActivityTbl {
		@Override
		protected ButtonElement createLogButton(final ComponentParameter cp,
				final ActivityBean activity) {
			return super.createLogButton(cp, activity).setOnclick(
					"$Actions['AbstractWorkflowMgrTPage_update_log']('activityId=" + activity.getId()
							+ "');");
		}

		@Override
		protected LinkElement createUsernodeElement(final ActivityBean activity) {
			return new LinkElement(activity)
					.setOnclick("$Actions['ActivityMgrTPage_workitems']('activityId=" + activity.getId()
							+ "');");
		}

		@Override
		protected MenuItem MI_STATUS_RUNNING() {
			return super.MI_STATUS_RUNNING().setOnclick_act("AbstractWorkflowMgrTPage_status",
					"activityId", "op=running");
		}

		@Override
		protected MenuItem MI_STATUS_SUSPENDED() {
			return super.MI_STATUS_SUSPENDED().setOnclick_act("AbstractWorkflowMgrTPage_status",
					"activityId", "op=suspended");
		}
	}
}