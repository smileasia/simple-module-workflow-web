package net.simpleframework.workflow.web.participant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.script.IScriptEval;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.workflow.engine.ActivityComplete;
import net.simpleframework.workflow.engine.EWorkitemStatus;
import net.simpleframework.workflow.engine.bean.ActivityBean;
import net.simpleframework.workflow.engine.bean.ProcessBean;
import net.simpleframework.workflow.engine.bean.WorkitemBean;
import net.simpleframework.workflow.engine.participant.IParticipantHandler.AbstractParticipantHandler;
import net.simpleframework.workflow.engine.participant.Participant;
import net.simpleframework.workflow.schema.AbstractTaskNode;
import net.simpleframework.workflow.schema.ProcessDocument;
import net.simpleframework.workflow.schema.ProcessNode;
import net.simpleframework.workflow.schema.TransitionNode;
import net.simpleframework.workflow.schema.UserNode;

public class PRelativeRoleHandler extends AbstractParticipantHandler
		implements IOrganizationContextAware {

	// 指定前一任务节点node: (默认为前一节点)
	private final String PARAMS_KEY_NODE = "node";
	// 相对角色名role: (默认为部门所有人员)
	private final String PARAMS_KEY_ROLE = "role";
	// 如果当前部门不存在当前角色，是否自动查找上一部门角色，默认false
	private final String PARAMS_KEY_AUTOPARENT = "autoparent";

	// 相对角色的级别level:下级lower,上级higher (默认是本部门)
	private final String PARAMS_KEY_level = "level";
	// 指定部门，默认为空
	private final String PARAMS_KEY_dept = "dept";
	// send=1时，过虑已经过送过的并还在处理中的用户,并且包括后续有处理
	// send=2时，过虑已经过送过的并还在处理中的用户,不包括后续
	// send=3时，过虑前一任务实际执行人员
	private final String PARAMS_KEY_send = "send";

	public enum Level {
		internal {// 本部门

		},
		level {// 平级

		},
		lower {// 下级

		},
		higher {// 上级

		},
		all {// 指定角色,即所有部门

		}
	}

	protected UserNode getUserNode(final Map<String, Object> variables) {
		return (UserNode) ((TransitionNode) variables.get("transition")).to();
	}

	@Override
	public Collection<Participant> getParticipants(final IScriptEval script,
			final ActivityComplete activityComplete, final Map<String, Object> variables) {
		final ArrayList<Participant> participants = new ArrayList<Participant>();
		// UserNode node = ((UserNode) ((TransitionNode)
		// variables.get("transition")).to());
		ActivityBean preActivity = activityComplete.getActivity();// 前一任务步骤实例
		final ID preActid = preActivity.getId();
		final UserNode.RuleRole rRole = (UserNode.RuleRole) getParticipantType(variables);
		final Map<String, String> params = getParams(rRole.getParams());
		final String node = params.get(PARAMS_KEY_NODE);
		final String role = params.get(PARAMS_KEY_ROLE);
		final String autoparent = params.get(PARAMS_KEY_AUTOPARENT);
		if (StringUtils.hasText(node)) {
			// 获取前一指定任务步骤实例
			preActivity = wfaService.getPreActivity(preActivity, node);
			if (null == preActivity) {
				return null;
			}
		}

		ID userId = null;
		ID roleId = null;
		ID deptId = null;
		WorkitemBean workitem = null;
		if (preActivity.getId().toString().equals(preActid.toString())) {
			// 如果前一指定节点就是上一节点
			workitem = activityComplete.getWorkitem();
		} else {
			final List<WorkitemBean> items = wfwService.getWorkitems(preActivity,
					EWorkitemStatus.complete);
			if (null != items && items.size() > 0) {
				workitem = items.get(0);
			}
		}
		if (workitem != null) {
			userId = workitem.getUserId();
			roleId = workitem.getRoleId();
			deptId = workitem.getDeptId();
		} else {
			final AbstractTaskNode tasknode = wfaService.getTaskNode(preActivity);
			if (tasknode instanceof UserNode && ((UserNode) tasknode).isEmpty()) {
				// 处理空节点的执行者
				final List<Participant> mps = wfaService.getEmptyParticipants(preActivity);
				if (null != mps && mps.size() > 0) {
					final Participant mp = mps.get(0);
					userId = mp.getUserId();
					roleId = mp.getRoleId();
					deptId = mp.getDeptId();
				}
			}
		}
		final String deptName = params.get(PARAMS_KEY_dept);
		if (StringUtils.hasText(deptName)) {// 指定部门
			final Department dept = _deptService.getDepartmentByName(deptName);
			deptId = dept.getId();
		}
		if (null == userId || null == roleId || null == deptId) {
			return null;
		}

		Level level = Level.internal;
		final String levelStr = params.get(PARAMS_KEY_level);
		if (StringUtils.hasText(levelStr)) {
			level = Level.valueOf(levelStr);
		}

		final WorkflowPermissionHandler wph = (WorkflowPermissionHandler) permission;
		if (StringUtils.hasText(role)) {
			final String[] _r = role.split(",");// 多个角色采用豆号分隔
			final Map<String, String> tempmap = new HashMap<String, String>();
			for (final String r : _r) {
				Collection<Participant> _participants = wph.getRelativeParticipantsOfLevel(userId,
						roleId, deptId, variables, r, level);

				if ((_participants == null || _participants.size() == 0) && level.equals(Level.internal)
						&& null != autoparent && autoparent.equals("true")) {
					// 本部门,自动查找上一部门角色
					final Department dept = _deptService.getBean(deptId);
					_participants = wph.getRelativeParticipantsOfLevel(userId, roleId,
							dept.getParentId(), variables, r, level);
				}
				if (_participants != null && _participants.size() > 0) {
					// participants.addAll(_participants);
					for (final Participant ppr : _participants) {
						final String key = "" + ppr.getDeptId() + ppr.getUserId();
						if (null == tempmap.get(key)) {
							participants.add(ppr);
							tempmap.put(key, "1");
						}
					}
				}
			}
		} else {
			final Collection<Participant> _participants = wph.getRelativeParticipantsOfLevel(userId,
					roleId, deptId, variables, null, level);
			if (_participants != null && _participants.size() > 0) {
				participants.addAll(_participants);
			}
		}

		if (participants != null && participants.size() > 0) {
			final String send = params.get(PARAMS_KEY_send);
			if (StringUtils.hasText(send) && ("1".equals(send) || "2".equals(send))) {
				// send=1时,过虑已经过送过的并还在处理中的用户
				final UserNode unode = getUserNode(variables);
				final ID pid = activityComplete.getActivity().getProcessId();
				final List<ActivityBean> sends = wfaService.getActivities(wfpService.getBean(pid),
						unode.getId());
				if (null != sends) {
					final ProcessBean pb = workflowContext.getProcessService()
							.getBean(preActivity.getProcessId());
					String yznodes = getProcessNodeProperty(pb, "process.yzyz.nodes");
					if (StringUtils.isBlank(yznodes)) {
						yznodes = "主任阅知";
					}

					for (final ActivityBean act : sends) {
						if (isFinalRunning(act, "1".equals(send), yznodes)) {
							List<WorkitemBean> items = null;
							if ("2".equals(send)) {
								items = wfwService.getWorkitems(act, EWorkitemStatus.running);
							} else {
								items = wfwService.getWorkitems(act);
							}
							if (null != items) {
								for (final WorkitemBean item : items) {
									for (final Participant p : participants) {
										if (p.getUserId().equals(item.getUserId())
												&& p.getDeptId().equals(item.getDeptId())) {
											participants.remove(p);
											break;
										}
									}
								}
							}
						}
					}
				}
			} else if (StringUtils.hasText(send) && ("3".equals(send))) {
				// 过虑前一任务实际执行人员
				for (final Participant p : participants) {
					if (p.getUserId().equals(workitem.getUserId())
							&& p.getDeptId().equals(workitem.getDeptId())) {
						participants.remove(p);
						break;
					}
				}
			}
		}
		return participants;
	}

	protected String getProcessNodeProperty(final ProcessBean pb, final String key) {
		final ProcessDocument doc = wfpService.getProcessDocument(pb);
		final ProcessNode node = doc == null ? null : doc.getProcessNode();
		return node == null ? null : node.getProperty(key);
	}

	// n是否包括后续
	private boolean isFinalRunning(final ActivityBean act, final boolean n, final String yznodes) {
		if (wfaService.isFinalStatus(act)) {
			if (n) {
				final List<ActivityBean> nexts = wfaService.getNextActivities(act);
				if (null != nexts) {
					for (final ActivityBean next : nexts) {
						// 不包括阅知的节点
						boolean isyznode = false;
						if (StringUtils.hasText(yznodes)) {
							final AbstractTaskNode nextnode = wfaService.getTaskNode(next);
							if (null != nextnode
									&& ("," + yznodes + ",").indexOf("," + nextnode.getName() + ",") > -1) {
								isyznode = true;
							}
						}
						if ((!isyznode) && isFinalRunning(next, n, yznodes)) {
							return true;
						}
					}
				}
			}
		} else {
			return true;
		}
		return false;
	}

	/**
	 * 获取前一指定节点实例
	 * 
	 * @param ab
	 * @param preNodeName
	 * @return
	 */
	// public ActivityBean getPreActivityBean(final ActivityBean ab,
	// final String preNodeName) {
	// if (null == ab) {
	// return null;
	// }
	// if (ab.getTasknodeText().equals(preNodeName)) {
	// return ab;
	// }
	// return getPreActivityBean(accountService.getBean(ab.getPreviousId()),
	// preNodeName);
	// }

	private Map<String, String> getParams(final String pas) {
		final Map<String, String> params = new HashMap<String, String>();
		if (StringUtils.hasText(pas)) {
			final String[] ps = pas.split(";");
			if (null != ps) {
				for (final String _p : ps) {
					if (StringUtils.hasText(_p)) {
						final String[] p = _p.split("=");
						if (p.length == 2) {
							params.put(p[0], p[1]);
						}
					}
				}
			}
		}
		return params;
	}
}
