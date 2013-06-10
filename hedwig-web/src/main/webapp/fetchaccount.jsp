<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<%@ taglib prefix="form" uri="/WEB-INF/spring-form.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
	<title><fmt:message key="fetch.account"/></title>
	<script src='js/jquery/jquery.js' type='text/javascript'></script>
	<script src='js/jquery/ui.core.js' type='text/javascript'></script>
    <script src='js/jquery/jquery.cookie.js' type='text/javascript'></script>
 	<link href='styles/styles.css' rel='stylesheet' type='text/css'>
	<script type='text/javascript'>
	function submitForm(action, dome) {
		document.aform.action = action; document.aform.todo.value = dome; document.aform.submit();
	}
	$(function() {
		$("input[name=useSSL]").click(function() {
			$("input[name=port]").val(this.checked ? 995 : 110);
		});
		$("#download").click(function() { submitForm("fetch.do", "docreate"); });
	});
	</script>
</head>
<body>
	<h1><fmt:message key="fetch.account"/></h1>
	<form:form name="aform" method="post" commandName="FetchAccount">
		<input type="hidden" name="acton" value="account"/>
		<input type="hidden" name="todo"/>
		<form:hidden path="ID"/>
		<form:hidden path="userID"/>
		<input type="hidden" name="destUserName" value="<c:out value='${param.userName}'/>@<c:out value='${param.domain}'/>"/>
		<input type="hidden" name="destPassword" value="<c:out value='${param.password}'/>"/>
		<c:url value='${param.returl}' var='returl'/>
		<input type="hidden" name="returl" value="<c:url value='${returl}'/>"/>
		<div id="main" class="main-basic">
			<!--//xmenu Start-->
			<div class="xmenu">
				<ul>
					<li><a href="#" id="download"><span><fmt:message key="menu.download"/></span></a></li>
					<li><a href="<c:url value='${returl}'/>" id="display"><span><fmt:message key="menu.list"/></span></a></li>
				</ul>
			</div>
			<!--//xdetail Start-->
			<div class="xdetail">
				<table>
					<caption></caption>
					<tbody>
						<tr>
							<th><fmt:message key="fetch.account.name"/></th>
							<td><form:input path="name"/><form:errors path="name"/></td>
						</tr>
						<tr>
							<th><fmt:message key="fetch.account.protocol"/></th>
							<td>
								<form:select path="protocol">
									<form:option value="pop3" label="POP3"/>
								</form:select>
							</td>
						</tr>
						<tr>
							<th><fmt:message key="fetch.account.server.addr"/></th>
							<td><form:input path="serverName"/><form:errors path="serverName"/></td>
						</tr>
						<tr>
							<th><fmt:message key="fetch.account.port"/></th>
							<td><form:input path="port"/></td>
						</tr>
						<tr>
							<th><fmt:message key="fetch.account.username"/></th>
							<td><form:input path="userName"/><form:errors path="userName"/></td>
						</tr>
						<tr>
							<th><fmt:message key="fetch.account.password"/></th>
							<td><form:password path="password" showPassword="true"/><form:errors path="password"/></td>
						</tr>
						<tr>
							<th><fmt:message key="fetch.account.ssl"/></th>
							<td><form:checkbox path="useSSL"/><fmt:message key="fetch.account.usessl"/></td>
						</tr>
						<tr>
							<th rowspan="2"><fmt:message key="fetch.account.settings"/></th>
							<td><form:radiobutton path="autoEmpty" value="true"/><fmt:message key="fetch.account.autoempty"/></td>
						</tr>
						<tr>
							<td><form:radiobutton path="autoEmpty" value="false"/><fmt:message key="fetch.account.notempty"/></td>
						</tr>
					</tbody>
				</table>
			</div>
			<!--//xdetail End-->
		</div>
		<!--//main-basic End-->
	</form:form>
</body>
	
