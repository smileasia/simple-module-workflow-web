package net.simpleframework.workflow.web.page.t2;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.t2.T2TemplatePage;
import net.simpleframework.workflow.web.page.AbstractItemsTPage;
import net.simpleframework.workflow.web.page.AbstractWorksTPage;
import net.simpleframework.workflow.web.page.MyDelegateListTPage;
import net.simpleframework.workflow.web.page.MyFinalWorklistTPage;
import net.simpleframework.workflow.web.page.MyInitiateItemsGroupTPage;
import net.simpleframework.workflow.web.page.MyInitiateItemsTPage;
import net.simpleframework.workflow.web.page.MyRunningWorklistTPage;
import net.simpleframework.workflow.web.page.MyWorklogsTPage;
import net.simpleframework.workflow.web.page.MyWorkstatTPage;
import net.simpleframework.workflow.web.page.MyWorkviewsTPage;
import net.simpleframework.workflow.web.page.UserDelegateListTPage;
import net.simpleframework.workflow.web.page.query.MyQueryWorksTPage;
import net.simpleframework.workflow.web.page.query.MyQueryWorksTPages.MyQueryWorks_DeptTPage;
import net.simpleframework.workflow.web.page.query.MyQueryWorksTPages.MyQueryWorks_OrgTPage;
import net.simpleframework.workflow.web.page.query.MyQueryWorksTPages.MyQueryWorks_RoleTPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractWorkPage extends T2TemplatePage {

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_ALL_ACCOUNT;
	}

	protected abstract Class<? extends AbstractWorksTPage> getWorkTPageClass();

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		return pp.includeUrl(getWorkTPageClass());
	}

	@PageMapping(url = "/workflow/my/running")
	public static class MyRunningWorklistPage extends AbstractWorkPage {

		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return MyRunningWorklistTPage.class;
		}
	}

	@PageMapping(url = "/workflow/my/final")
	public static class MyFinalWorklistPage extends AbstractWorkPage {

		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return MyFinalWorklistTPage.class;
		}
	}

	@PageMapping(url = "/workflow/my/initiate-group")
	public static class MyInitiateItemsGroupPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return MyInitiateItemsGroupTPage.class;
		}
	}

	@PageMapping(url = "/workflow/my/initiate")
	public static class MyInitiateItemsPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return MyInitiateItemsTPage.class;
		}
	}

	@PageMapping(url = "/workflow/my/delegate")
	public static class MyDelegateListPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return MyDelegateListTPage.class;
		}
	}

	@PageMapping(url = "/workflow/my/user-delegate")
	public static class UserDelegateListPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return UserDelegateListTPage.class;
		}
	}

	@PageMapping(url = "/workflow/my/views")
	public static class MyWorkviewsPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return MyWorkviewsTPage.class;
		}
	}

	@PageMapping(url = "/workflow/my/stat")
	public static class MyWorkstatPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return MyWorkstatTPage.class;
		}
	}

	@PageMapping(url = "/workflow/my/log")
	public static class MyWorklogsPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractItemsTPage> getWorkTPageClass() {
			return MyWorklogsTPage.class;
		}
	}

	/*-------------------------------query--------------------------------*/

	@PageMapping(url = "/workflow/query/my-works")
	public static class MyQueryWorksPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractWorksTPage> getWorkTPageClass() {
			return MyQueryWorksTPage.class;
		}
	}

	@PageMapping(url = "/workflow/query/dept-works")
	public static class MyQueryWorks_DeptPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractWorksTPage> getWorkTPageClass() {
			return MyQueryWorks_DeptTPage.class;
		}
	}

	@PageMapping(url = "/workflow/query/org-works")
	public static class MyQueryWorks_OrgPage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractWorksTPage> getWorkTPageClass() {
			return MyQueryWorks_OrgTPage.class;
		}
	}

	@PageMapping(url = "/workflow/query/role-works")
	public static class MyQueryWorks_RolePage extends AbstractWorkPage {
		@Override
		protected Class<? extends AbstractWorksTPage> getWorkTPageClass() {
			return MyQueryWorks_RoleTPage.class;
		}
	}
}
