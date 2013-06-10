<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<title><fmt:message key="message.list.aliases"/></title>
	<script src='js/jquery/jquery.js' type='text/javascript'></script>
	<script src='js/jquery/ui.core.js' type='text/javascript'></script>
    <script src='js/jquery/jquery.cookie.js' type='text/javascript'></script>
	<script src="js/jquery/ui.simplepager.js" type="text/javascript"></script>
 	<link href='styles/styles.css' rel='stylesheet' type='text/css'>
 	<link href='styles/ui.simplepager.css' rel='stylesheet' type='text/css'>
	<script type='text/javascript'>
	function submitForm(action, dome, ID) {
		document.aform.action = action;
		document.aform.todo.value = dome;
		if (typeof ID == "undefined") {
			if (dome == "delete" && $("input[name=IDs]:checked").length == 0 ) {
				alert("<fmt:message key='message.select.alias'/>");
				return;
			}
		} else {
			document.aform.ID.value = ID;
		}
		document.aform.submit();
	}
	function gotopage(page) {
		var url = document.aform.returl.value;
		var re = new RegExp("([\\?&])page=[^&]*");
		window.location.href = re.test(url) ? url.replace(re, "$1page=" + page) : url + (url.indexOf('?') > 0 ? '&' : '?') + 'page=' + page;
	}
	$(function() {
    	$("#pager").simplepager({
			totalRecords: <c:out value="${pager.itemCount}" />, currentPage: <c:out value="${param.page}" default="1" />, perPage: <c:out value="${param.pageSize}" default="12" />,
			switchPage: function(event,page) { gotopage(page); }
		});
		$("#create").click(function() { submitForm("alias.do", "create"); });
		$("#delete").click(function() { submitForm("console.do", "delete"); });
		$(".xlist a[href=#]").click(function() {
			submitForm("alias.do", "update", $(this).parents("tr:first").find("input[name=IDs]:checkbox").val());
		});
	});
	</script>
</head>
<body>
	<h1><fmt:message key="message.list.aliases"/></h1>
	<form name="aform" method="post" action="console.do">
		<input type="hidden" name="acton" value="alias"/>
		<input type="hidden" name="todo"/>
		<input type="hidden" name="domain" value="<c:out value='${param.domain}'/>"/>
		<input type="hidden" name="ID"/>
		<input type="hidden" name="returl" value="<c:out value='${session.returnUrl}'/>"/>
		<!--//main-basic Start-->
		<div id="main" class="main-basic">
			<!--//xmenu Start-->
			<div class="xmenu">
				<ul>
					<li><a href="#" id="create"><span><fmt:message key="menu.new"/></span></a></li>
					<li><a href="#" id="delete"><span><fmt:message key="menu.delete"/></span></a></li>
				</ul>
			</div>
			<!--//xmenu End-->
			<!--//xlist Start-->
			<div class="xlist">
				<table>
					<caption></caption>
					<thead>
						<tr>
							<th scope="col" width="15"><input type="checkbox" title="checkbox" /></th>
							<th scope="col"><a href="#"><fmt:message key="alias.alias"/></a></th>
							<th scope="col"><a href="#"><fmt:message key="alias.redirect.to"/></a></th>
							<th scope="col"><a href="#">&nbsp</a></th>
						</tr>
					</thead>
					<tbody>
					<c:choose>
						<c:when test="${not empty aliases}">
							<c:forEach var="alias" items="${aliases}" varStatus="status">
								<tr>
									<td><input type="checkbox" name="IDs" value="<c:out value='${alias.ID}'/>"/></td>
									<td class="text-left"><a href="#"><c:out value="${alias.alias}"/></a></td>
									<td class="text-left"><c:out value="${alias.userID}"/></td>
									<td>&nbsp;</td>
								</tr>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<tr>
								<td colspan="4"><fmt:message key="data.not.found"/></td>
							</tr>
						</c:otherwise>
					</c:choose>
					</tbody>
				</table>
				<!--//pager Start-->
				<div id="pager" class="pager"></div>
				<!--//pager End-->
			</div>
			<!--//xlist End-->
		</div>
		<!--//main-basic End-->
	</form>
</body>
</html>
