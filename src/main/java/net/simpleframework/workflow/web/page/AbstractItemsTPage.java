package net.simpleframework.workflow.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.List;

import net.simpleframework.common.object.ObjectFactory;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.SupElement;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.pager.ITablePagerHandler;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.Category_ListPage;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.CategoryItems;
import net.simpleframework.workflow.engine.IWorkflowContextAware;
import net.simpleframework.workflow.web.IWorkflowWebContext;
import net.simpleframework.workflow.web.WorkflowUrlsFactory;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractItemsTPage extends Category_ListPage implements IWorkflowContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);
		pp.addImportCSS(AbstractItemsTPage.class, "/my_work.css");

		final IModuleRef ref = ((IWorkflowWebContext) context).getLogRef();
		Class<? extends AbstractMVCPage> lPage;
		if (ref != null && (lPage = getUpdateLogPage()) != null) {
			pp.addComponentBean("AbstractItemsTPage_update_logPage", AjaxRequestBean.class)
					.setUrlForward(AbstractMVCPage.url(lPage));
			pp.addComponentBean("AbstractItemsTPage_update_log", WindowBean.class)
					.setContentRef("AbstractItemsTPage_update_logPage").setHeight(540).setWidth(864);
		}
	}

	protected Class<? extends AbstractMVCPage> getUpdateLogPage() {
		return null;
	}

	@Override
	protected TablePagerBean addTablePagerBean(final PageParameter pp, final String name,
			final Class<? extends ITablePagerHandler> handlerClass) {
		return (TablePagerBean) addTablePagerBean(pp, name, handlerClass, false).setResize(false)
				.setShowCheckbox(false).setShowFilterBar(true).setShowLineNo(false).setShowHead(true)
				.setPageItems(50);
	}

	protected CategoryItem createCategoryItem(final PageParameter pp, final String text,
			final Class<? extends AbstractItemsTPage> mClass) {
		return new CategoryItem(text).setHref(getUrlsFactory().getUrl(pp, mClass)).setSelected(
				mClass == ObjectFactory.original(getClass()));
	}

	@Override
	protected CategoryItems getCategoryList(final PageParameter pp) {
		final CategoryItem item0 = createCategoryItem(pp, $m("AbstractItemsTPage.0"),
				MyRunningWorklistTPage.class).setIconClass("my_work_icon");
		final int count = wService.getUnreadWorklist(pp.getLoginId()).getCount();
		if (count > 0) {
			item0.setNum(new SupElement(count).setHighlight(true));
		}

		// final String s = pp.getParameter("s");
		final List<CategoryItem> children = item0.getChildren();
		// children.add(createCategoryItem(pp, "待办工作",
		// MyRunningWorklistTPage.class,
		// "status=running")
		// .setSelected("running".equals(_status)));
		// .setSelected("complete".equals(s))
		children.add(createCategoryItem(pp, $m("AbstractItemsTPage.1"), MyFinalWorklistTPage.class)
				.setIconClass("my_work_complete_icon"));
		// children.add();
		final CategoryItem delegate = createCategoryItem(pp, $m("AbstractItemsTPage.3"),
				MyWorkDelegateListTPage.class).setIconClass("delegate_list_icon");
		delegate.setSelected(delegate.isSelected()
				|| UserDelegateListTPage.class == ObjectFactory.original(getClass()));
		return CategoryItems.of(item0,
				createCategoryItem(pp, $m("AbstractItemsTPage.2"), MyInitiateItemsTPage.class)
						.setIconClass("my_initiate_icon"), delegate);
	}

	protected static WorkflowUrlsFactory getUrlsFactory() {
		return ((IWorkflowWebContext) context).getUrlsFactory();
	}
}
