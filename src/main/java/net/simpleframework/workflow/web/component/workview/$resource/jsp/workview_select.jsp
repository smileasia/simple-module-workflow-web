<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="net.simpleframework.mvc.component.ComponentParameter"%>
<%@ page import="net.simpleframework.workflow.web.component.workview.DoWorkviewUtils"%>
<%
	final ComponentParameter nCP = DoWorkviewUtils.get(request,
			response);
	final String componentName = nCP.getComponentName();
	final String params = DoWorkviewUtils.BEAN_ID + "=" + nCP.hashId();
%>
<div class="workview_select">
  <%=DoWorkviewUtils.toSelectHTML(nCP)%>
</div>
<script type="text/javascript">
  function DoWorkview_user_selected(selects) {
    var act = $Actions['<%=componentName%>_ulist'];
    act.container = $(".workview_select .wv_cc");
    act('<%=params%>&userIds='
        + $(selects).inject([], function(r, o) {
          r.push(o.id);
          return r;
        }).join(";"));
    return true;
  }
</script>
<style type="text/css">
</style>