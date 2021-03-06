package net.simpleframework.workflow.web.component.comments;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.common.web.html.HtmlConst;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.ctx.permission.IPermissionHandler;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.Radio;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentHandlerEx;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.workflow.engine.IActivityService;
import net.simpleframework.workflow.engine.bean.AbstractWorkitemBean;
import net.simpleframework.workflow.engine.bean.ProcessBean;
import net.simpleframework.workflow.engine.bean.WorkitemBean;
import net.simpleframework.workflow.engine.bean.WorkviewBean;
import net.simpleframework.workflow.engine.comment.IWfCommentLogService;
import net.simpleframework.workflow.engine.comment.IWfCommentService;
import net.simpleframework.workflow.engine.comment.WfComment;
import net.simpleframework.workflow.engine.comment.WfCommentLog;
import net.simpleframework.workflow.engine.comment.WfCommentLog.ELogType;
import net.simpleframework.workflow.schema.AbstractTaskNode;
import net.simpleframework.workflow.schema.Node;
import net.simpleframework.workflow.schema.ProcessNode;
import net.simpleframework.workflow.schema.UserNode;
import net.simpleframework.workflow.web.WorkflowUtils;
import net.simpleframework.workflow.web.component.comments.WfCommentBean.EGroupBy;
import net.simpleframework.workflow.web.component.comments.mgr2.MyCommentsMgrTPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultWfCommentHandler extends ComponentHandlerEx implements IWfCommentHandler {

	protected ProcessBean getProcessBean(final PageParameter pp) {
		return WorkflowUtils.getProcessBean(pp);
	}

	@Override
	public IDataQuery<WfComment> comments(final ComponentParameter cp) {
		final ProcessBean processBean = getProcessBean(cp);
		if (processBean == null) {
			return DataQueryUtils.nullQuery();
		}
		return workflowContext.getCommentService().queryComments(processBean.getId());
	}

	protected AbstractWorkitemBean getWorkitemBean(final PageParameter pp) {
		return WorkflowUtils.getWorkitemBean(pp);
	}

	protected WfComment getCurComment(final PageParameter pp) {
		return workflowContext.getCommentService().getCurComment(getWorkitemBean(pp));
	}

	@Override
	public void onSave(final ComponentParameter cp) {
		final String ccomment = cp.getParameter("ta_wfcomment");
		if (!StringUtils.hasText(ccomment)) {
			return;
		}

		final AbstractWorkitemBean workitem = getWorkitemBean(cp);
		final IWfCommentService cService = workflowContext.getCommentService();
		WfComment comment = getCurComment(cp);
		if (comment == null) {
			comment = cService.createBean();
			comment.setCreateDate(new Date());
			comment.setContentId(workitem.getProcessId());
			comment.setWorkitemId(workitem.getId());
			comment.setDeptId(workitem.getDeptId());
			if (workitem instanceof WorkitemBean) {
				comment.setUserId(((WorkitemBean) workitem).getUserId2());
				comment.setTaskname(wfwService.getActivity((WorkitemBean) workitem).getTasknodeText());
			} else {
				comment.setUserId(workitem.getUserId());
			}
			comment.setCcomment(ccomment);
			cService.insert(comment);
		} else {
			comment.setCcomment(ccomment);
			cService.update(new String[] { "ccomment" }, comment);
		}

		if (cp.getBoolParameter("cb_wfcomment")) {
			final IWfCommentLogService lService = workflowContext.getCommentLogService();
			final WfCommentLog log = lService.getLog(comment.getUserId(), comment.getCcomment(),
					ELogType.collection);
			if (log == null) {
				lService.insertLog(comment, ELogType.collection);
			}
		}
	}

	protected InputElement createCommentTa(final PageParameter pp) {
		final InputElement ele = InputElement.textarea().setRows(4).setAutoRows(true)
				.setName("ta_wfcomment").setId("ta_wfcomment")
				.addAttribute("maxlength", pp.getBeanProperty("maxlength"));
		final WfComment bean = getCurComment(pp);
		if (bean != null) {
			ele.setValue(bean.getCcomment());
		}
		return ele;
	}

	protected Map<String, String[]> getTasknames(final PageParameter pp,
			final AbstractWorkitemBean workitem) {
		final Map<String, String[]> data = new LinkedHashMap<String, String[]>();
		if (workitem instanceof WorkitemBean) {
			final IActivityService aService = workflowContext.getActivityService();
			final AbstractTaskNode tasknode = aService
					.getTaskNode(aService.getBean(((WorkitemBean) workitem).getActivityId()));
			for (final Node node : ((ProcessNode) tasknode.getParent()).nodes()) {
				if (node instanceof UserNode) {
					final String name = node.getName();
					if (StringUtils.hasText(name)) {
						data.put(node.getText(), new String[] { name });
					}
				}
			}
		}
		return data;
	}

	protected Map<Object, List<WfComment>> comments_map(final PageParameter pp,
			final IDataQuery<WfComment> dq, final AbstractWorkitemBean workitem,
			final EGroupBy groupBy) {
		final Map<Object, List<WfComment>> data = new LinkedHashMap<Object, List<WfComment>>();
		Map<String, String[]> tasknames = null;
		if (groupBy == EGroupBy.taskname) {
			// 数据按tasknames的顺序
			tasknames = getTasknames(pp, workitem);
			for (final String key : tasknames.keySet()) {
				data.put(key, new ArrayList<WfComment>());
			}
		}

		final IPermissionHandler phdl = pp.getPermission();
		WfComment comment;
		while ((comment = dq.next()) != null) {
			Object key = null;
			if (groupBy == EGroupBy.dept) {
				final PermissionDept dept = phdl.getDept(comment.getDeptId());
				if (dept == null) {
					continue;
				}
				key = dept;
			} else if (tasknames != null) { // 按任务
				for (final Map.Entry<String, String[]> e : tasknames.entrySet()) {
					final WorkitemBean workitem2 = wfwService.getBean(comment.getWorkitemId());
					if (workitem2 == null) {
						continue;
					}
					if (ArrayUtils.contains(e.getValue(),
							wfaService.getTaskNode(wfwService.getActivity(workitem2)).getName())) {
						key = e.getKey();
						break;
					}
				}
			}

			if (key != null) {
				List<WfComment> list = data.get(key);
				if (list == null) {
					data.put(key, list = new ArrayList<WfComment>());
				}
				list.add(comment);
			}
		}

		// 默认排序
		if (groupBy == EGroupBy.dept) {
			final List<Object> keys = new ArrayList<Object>(data.keySet());
			Collections.sort(keys, new Comparator<Object>() {
				@Override
				public int compare(final Object o1, final Object o2) {
					final PermissionDept d1 = (PermissionDept) o1;
					final PermissionDept d2 = (PermissionDept) o2;
					final int l1 = d1.getLevel();
					final int l2 = d2.getLevel();
					if (l1 == l2) {
						final int order1 = d1.getOorder();
						final int order2 = d2.getOorder();
						if (order1 == order2) {
							return 0;
						} else {
							return order1 > order2 ? 1 : -1;
						}
					} else {
						return l1 > l2 ? 1 : -1;
					}
				}
			});
			final Map<Object, List<WfComment>> _data = new LinkedHashMap<Object, List<WfComment>>();
			for (final Object key : keys) {
				_data.put(key, data.get(key));
			}
			return _data;
		}
		return data;
	}

	protected final static String COOKIE_GROUPBY = "wfcomment_groupby";

	protected EGroupBy[] allGroups() {
		return EGroupBy.values();
	}

	protected String getGroupText(final EGroupBy g) {
		return g.toString();
	}

	@Override
	public String toHTML(final ComponentParameter cp) {
		EGroupBy groupBy = cp.getEnumParameter(EGroupBy.class, "groupBy");
		if (groupBy == null) {
			groupBy = Convert.toEnum(EGroupBy.class, cp.getCookie(COOKIE_GROUPBY));
		}
		if (groupBy == null) {
			groupBy = (EGroupBy) cp.getBeanProperty("groupBy");
		}

		final AbstractWorkitemBean workitem = getWorkitemBean(cp);
		if (workitem instanceof WorkviewBean && groupBy == EGroupBy.taskname) {
			// Workview不支持EGroupBy.taskname
			groupBy = EGroupBy.none;
		}

		final IDataQuery<WfComment> dq = comments(cp);
		final String commentName = cp.getComponentName();
		final StringBuilder sb = new StringBuilder();
		final boolean editable = (Boolean) cp.getBeanProperty("editable");
		if (editable) {
			sb.append("<div class='ta'>");
			sb.append(createCommentTa(cp));
			sb.append("</div>");
		}
		sb.append("<div class='btns clearfix'>");
		if (editable) {
			sb.append(" <div class='left'>");
			sb.append("   <a class='simple_btn2' onclick=\"$Actions['").append(commentName)
					.append("_log_popup']();\">#(DefaultWfCommentHandler.0)</a>");
			sb.append("	  <span class='ltxt'>&nbsp;</span>");
			sb.append(" </div>");
		}
		sb.append(" <div class='right'>");
		int i = 0;
		for (final EGroupBy g : allGroups()) {
			final String rn = "comments_groupby";
			if (workitem instanceof WorkviewBean && g == EGroupBy.taskname) {
				continue;
			}
			sb.append(new Radio(rn + i++, getGroupText(g)).setName(rn).setChecked(groupBy == g)
					.setOnclick("_wf_comment_radio_click('" + g.name() + "');"));
			sb.append(SpanElement.SPACE);
		}
		sb.append(SpanElement.SPACE(20));
		if (editable) {
			sb.append(new Checkbox("id" + commentName + "_addCheck", $m("DefaultWfCommentHandler.1"))
					.setName("cb_wfcomment").setValue("true"));
		}
		sb.append(" </div>");
		sb.append("</div>");
		sb.append(HtmlConst.TAG_SCRIPT_START);
		sb.append("function _wf_comment_radio_click(groupBy) {");
		sb.append(" var val = $F('ta_wfcomment');");
		sb.append(" var act = $Actions['").append(cp.getComponentName()).append("'];");
		sb.append(" act.jsCompleteCallback = function() {");
		sb.append("  var ta = $('ta_wfcomment');");
		sb.append("  if (ta) ta.setValue(val);");
		sb.append("  document.setCookie('").append(COOKIE_GROUPBY).append("', groupBy, 24 * 365);");
		sb.append(" };");
		sb.append(" act('");
		if (workitem != null) {
			sb.append(workitem instanceof WorkitemBean ? "workitemId" : "workviewId").append("=");
			sb.append(workitem.getId()).append("&");
		}
		sb.append("groupBy=' + groupBy);");
		sb.append("}");
		sb.append(HtmlConst.TAG_SCRIPT_END);

		final WfComment comment2 = getCurComment(cp);
		final StringBuilder sb2 = new StringBuilder();
		if (groupBy == EGroupBy.none) {
			i = 0;
			WfComment comment;
			while ((comment = dq.next()) != null) {
				if (editable && comment2 != null && comment2.equals(comment)) {
					continue;
				}
				sb2.append(toCommentItemHTML(cp, comment, i++ == 0, groupBy));
			}
		} else {
			for (final Map.Entry<Object, List<WfComment>> e : comments_map(cp, dq, workitem, groupBy)
					.entrySet()) {
				final List<WfComment> list = new ArrayList<WfComment>();
				for (final WfComment comment : e.getValue()) {
					if (editable && comment2 != null && comment2.equals(comment)) {
						continue;
					}
					list.add(comment);
				}
				if (list.size() > 0) {
					sb2.append("<div class='comment-group-item'>").append(e.getKey()).append("</div>");
					i = 0;
					for (final WfComment comment : list) {
						sb2.append(toCommentItemHTML(cp, comment, i++ == 0, groupBy));
					}
				}
			}
		}

		if (sb2.length() > 0) {
			sb.append("<div class='comment-list");
			if (groupBy != EGroupBy.none) {
				sb.append(" comment-group");
			}
			sb.append("'>");
			sb.append(sb2);
			sb.append("</div>");
		}
		return sb.toString();
	}

	protected String toCommentItemHTML(final ComponentParameter cp, final WfComment comment,
			final boolean first, final EGroupBy groupBy) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='comment-item");
		if (first) {
			sb.append(" item-first");
		}
		sb.append("'>");
		// cp.getPhotoUrl(comment.getUserId())
		sb.append("<img src='").append(cp.getCssResourceHomePath(DefaultWfCommentHandler.class))
				.append("/images/none_user.gif' />");
		sb.append(" <div class='i1'>").append(HtmlUtils.convertHtmlLines(comment.getCcomment()))
				.append("</div>");
		sb.append(" <div class='i2 clearfix'>");
		sb.append(toCommentInfo_LeftHTML(cp, comment, groupBy));
		sb.append(toCommentInfo_RightHTML(cp, comment, groupBy));
		sb.append(" </div>");
		sb.append("</div>");
		return sb.toString();
	}

	protected String toCommentInfo_LeftHTML(final ComponentParameter cp, final WfComment comment,
			final EGroupBy groupBy) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='left'>");
		final PermissionUser ouser = cp.getUser(comment.getUserId());
		if (groupBy != EGroupBy.dept) {
			if (null != comment.getDeptId()) {
				sb.append(cp.getDept(comment.getDeptId()));
			} else {
				sb.append(ouser.getDept());
			}
			sb.append(SpanElement.SPACE);
		}
		sb.append(ouser);

		final int nYear = Calendar.getInstance().get(Calendar.YEAR);
		final Calendar cal = Calendar.getInstance();
		final Date commentDate = comment.getCreateDate();
		cal.setTime(commentDate);
		final String format = nYear == cal.get(Calendar.YEAR) ? "MM-dd HH:mm" : "yy-MM-dd HH:mm";
		sb.append(SpanElement.SPACE).append(Convert.toDateString(commentDate, format));
		sb.append("</div>");
		return sb.toString();
	}

	protected String toCommentInfo_RightHTML(final ComponentParameter cp, final WfComment comment,
			final EGroupBy groupBy) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='right'>");
		sb.append(comment.getTaskname());
		if (cp.isLmember(cp.getBeanProperty("managerRole"))) {
			sb.append(SpanElement.SPACE10).append("[");
			final String componentName = cp.getComponentName();
			sb.append(new LinkElement($m("Edit")).setOnclick(
					"$Actions['" + componentName + "_edit']('commentId=" + comment.getId() + "');"));
			sb.append(SpanElement.SPACE);
			sb.append(new LinkElement($m("Delete")).setOnclick(
					"$Actions['" + componentName + "_del']('commentId=" + comment.getId() + "');"));
			sb.append("]");
		}
		sb.append("</div>");
		return sb.toString();
	}

	@Override
	public Map<String, Object> getFormParameters(final ComponentParameter cp) {
		final Map<String, Object> data = super.getFormParameters(cp);
		data.put(WfCommentUtils.BEAN_ID, cp.getParameter(WfCommentUtils.BEAN_ID));
		return data;
	}

	@Override
	public String getMycommentsUrl(final PageParameter pp) {
		return uFactory.getUrl(pp, MyCommentsMgrTPage.class);
	}
}
