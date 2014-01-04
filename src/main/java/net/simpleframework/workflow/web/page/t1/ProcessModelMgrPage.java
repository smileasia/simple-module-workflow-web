package net.simpleframework.workflow.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.attachments.AbstractAttachmentHandler;
import net.simpleframework.mvc.component.ext.attachments.AttachmentBean;
import net.simpleframework.mvc.component.ext.attachments.IAttachmentSaveCallback;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.swfupload.SwfUploadBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.t1.T1ResizedTemplatePage;
import net.simpleframework.workflow.engine.EProcessModelStatus;
import net.simpleframework.workflow.engine.IWorkflowContextAware;
import net.simpleframework.workflow.engine.ProcessModelBean;
import net.simpleframework.workflow.schema.ProcessDocument;
import net.simpleframework.workflow.web.WorkflowLogRef.StatusDescLogPage;
import net.simpleframework.workflow.web.page.ProcessPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ProcessModelMgrPage extends T1ResizedTemplatePage implements IWorkflowContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);
		pp.addImportCSS(ProcessModelMgrPage.class, "/pm_mgr.css");

		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp,
				"ProcessModelMgrPage_tbl", TablePagerBean.class)
				.setPagerBarLayout(EPagerBarLayout.bottom).setContainerId("idProcessModelMgrPage_tbl")
				.setHandleClass(ProcessModelTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("modelText", $m("ProcessModelMgrPage.0")).setTextAlign(
								ETextAlign.left).setSort(false))
				.addColumn(
						new TablePagerColumn("processCount", $m("ProcessModelMgrPage.1"), 80)
								.setSort(false))
				.addColumn(
						new TablePagerColumn("userText", $m("ProcessModelMgrPage.2"), 115).setSort(false))
				.addColumn(
						new TablePagerColumn("createDate", $m("ProcessModelMgrPage.3"), 115)
								.setPropertyClass(Date.class))
				.addColumn(
						new TablePagerColumn("status", $m("ProcessModelMgrPage.4"), 70)
								.setPropertyClass(EProcessModelStatus.class))
				.addColumn(TablePagerColumn.OPE().setWidth(100));

		// 删除
		addAjaxRequest(pp, "ProcessModelMgrPage_del").setHandleMethod("doDelete").setConfirmMessage(
				$m("Confirm.Delete"));

		// status
		addAjaxRequest(pp, "ProcessModelMgrPage_statusPage", StatusDescLogPage.class);
		addWindowBean(pp, "ProcessModelMgrPage_statusWindow")
				.setContentRef("ProcessModelMgrPage_statusPage").setWidth(420).setHeight(240);

		// 上传模型文件
		addComponentBean(pp, "ProcessModelMgrPage_upload_page", AttachmentBean.class)
				.setShowSubmit(true).setShowEdit(false).setHandleClass(ModelUploadAction.class);
		addComponentBean(pp, "ProcessModelMgrPage_upload", WindowBean.class)
				.setContentRef("ProcessModelMgrPage_upload_page").setTitle($m("ProcessModelMgrPage.7"))
				.setHeight(480).setWidth(400);
	}

	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("modelId"));
		if (ids != null) {
			context.getProcessModelService().delete(ids);
		}
		return new JavascriptForward("$Actions['ProcessModelMgrPage_tbl']();");
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String variable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div align='center' class='ProcessModelMgrPage'>");
		sb.append(" <div id='idProcessModelMgrPage_tbl'></div>");
		sb.append("</div>");
		return sb.toString();
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(new LinkButton($m("ProcessModelMgrPage.5"))
				.setOnclick("$Actions['ProcessModelMgrPage_upload']();"));
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		return super.getNavigationBar(pp).append(new SpanElement("#(WorkflowWebContext.1)"));
	}

	public static class ProcessModelTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			return context.getProcessModelService().getModelList();
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			return MenuItems.of(MenuItem.sep(),
					MenuItem.itemDelete().setOnclick_act("ProcessModelMgrPage_del", "modelId"));
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final ProcessModelBean processModel = (ProcessModelBean) dataObject;
			final EProcessModelStatus status = processModel.getStatus();
			final KVMap row = new KVMap()
					.add("modelText",
							new LinkElement(processModel).setHref(url(ProcessPage.class, "modelId="
									+ processModel.getId()))).add("processCount", 0)
					.add("userText", cp.getUser(processModel.getUserId()))
					.add("createDate", processModel.getCreateDate())
					.add("status", processModel.getStatus());
			final StringBuilder sb = new StringBuilder();
			if (status == EProcessModelStatus.edit) {
				sb.append(LinkButton.corner(EProcessModelStatus.deploy));
			} else if (status == EProcessModelStatus.deploy) {
				sb.append(LinkButton.corner("恢复"));
			}
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			row.add(TablePagerColumn.OPE, sb.toString());
			return row;
		}
	}

	public static class ModelUploadAction extends AbstractAttachmentHandler {
		@Override
		public void setSwfUploadBean(final ComponentParameter cp, final SwfUploadBean swfUpload) {
			super.setSwfUploadBean(cp, swfUpload);
			swfUpload.setFileTypes("*.xml").setFileTypesDesc($m("ProcessModelMgrPage.6"));
		}

		@Override
		public JavascriptForward doSave(final ComponentParameter cp,
				final IAttachmentSaveCallback callback) throws IOException {
			final Map<String, AttachmentFile> attachments = getUploadCache(cp);
			for (final AttachmentFile aFile : attachments.values()) {
				context.getProcessModelService().addModel(cp.getLoginId(),
						new ProcessDocument(new FileInputStream(aFile.getAttachment())));
			}
			// 清除
			clearCache(cp);
			return new JavascriptForward("$Actions['ProcessModelMgrPage_tbl']();")
					.append("$Actions['ProcessModelMgrPage_upload'].close();");
		}
	}
}
