<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="net.simpleframework.workflow.web.component.abort.ActivityAbortUtils"%>
<%
	ActivityAbortUtils.doActivityAbort(ActivityAbortUtils.get(request,
			response));
%>