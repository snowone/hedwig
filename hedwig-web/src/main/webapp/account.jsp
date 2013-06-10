<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<%@ taglib prefix="form" uri="/WEB-INF/spring-form.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
	<title>
		<c:choose>
			<c:when test="${User.ID == 0}"><fmt:message key="account.add"/></c:when>
			<c:otherwise><fmt:message key="account.edit"/></c:otherwise>
		</c:choose>
	</title>
	<script src='js/jquery/jquery.js' type='text/javascript'></script>
	<script src='js/jquery/ui.core.js' type='text/javascript'></script>
    <script src='js/jquery/jquery.cookie.js' type='text/javascript'></script>
 	<link href='styles/styles.css' rel='stylesheet' type='text/css'>
	<script type='text/javascript'>
	function submitForm(action, dome) {
		document.aform.action = action; document.aform.todo.value = dome; document.aform.submit();
	}
	$(function() {
		$("#create").click(function() { submitForm("account.do", "docreate"); });
		$("#update").click(function() { submitForm("account.do", "doupdate"); });
		$("#delete").click(function() { if (confirm("<fmt:message key='message.confirm.delete'/>")) submitForm("console.do", "delete"); });
		$("#empty").click(function() { if (confirm("<fmt:message key='message.confirm.empty' />")) submitForm("console.do", "empty"); });
		$("#fetch").click(function() { submitForm("fetch.do", "create"); });
	});
	</script>
</head>
<body>
	<h1>
		<c:choose>
			<c:when test="${User.ID == 0}"><fmt:message key="account.add"/></c:when>
			<c:otherwise><fmt:message key="account.edit"/></c:otherwise>
		</c:choose>
	</h1>
	<form:form name="aform" method="post" commandName="User">
		<input type="hidden" name="acton" value="account"/>
		<input type="hidden" name="todo"/>
		<form:hidden path="ID"/>
		<c:url value='${param.returl}' var='returl'/>
		<input type="hidden" name="returl" value="<c:url value='${returl}'/>"/>
		<!--//main-basic Start-->
		<div id="main" class="main-basic">
			<!--//xmenu Start-->
			<div class="xmenu">
				<ul>
					<c:choose>
						<c:when test="${User.ID == 0}">
							<li><a href="#" id="create"><span><fmt:message key="menu.add"/></span></a></li>
						</c:when>
						<c:otherwise>
							<li><a href="#" id="update"><span><fmt:message key="menu.modify"/></span></a></li>
							<li><a href="#" id="delete"><span><fmt:message key="menu.delete"/></span></a></li>
							<li><a href="#" id="empty"><span><fmt:message key="menu.empty"/></span></a></li>
							<li><a href="#" id="fetch"><span><fmt:message key="menu.fetch"/></span></a></li>
						</c:otherwise>
					</c:choose>
					<li><a href="<c:url value='${returl}'/>" id="display"><span><fmt:message key="menu.list"/></span></a></li>
				</ul>
			</div>
			<!--//xdetail Start-->
			<div class="xdetail">
				<table>
					<caption></caption>
					<tbody>
						<tr>
							<th><fmt:message key="account.address"/></th>
							<td>
								<input type="text" name="userName" value="<c:out value='${User.userName}'/>"/>@<input type="text" readonly="true" name="domain" value="<c:out value='${param.domain}'/>"/><form:errors path="userName"/>
							</td>
						</tr>
						<tr>
							<th><fmt:message key="account.password"/></th>
							<td>
								<form:password path="password" showPassword="true"/>
							</td>
						</tr>
						<tr>
							<th><fmt:message key="account.size"/></th>
							<td>
								<input type="text" readonly="true" value="<fmt:formatNumber value='${usage / (1024 * 1024)}' maxFractionDigits='0' />"/> MB
							</td>
						</tr>
						<tr>
							<th><fmt:message key="account.maxsize"/></th>
							<td>
								<form:input path="quota"/> MB
							</td>
						</tr>
						<tr>
							<th><fmt:message key="account.forward"/></th>
							<td>
								<form:input path="forwardTo"/><form:errors path="forwardTo"/>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<!--//xdetail End-->
		</div>
		<!--//main-basic End-->
	</form:form>
</body>
</html>
