<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<title><fmt:message key="message.list.accounts"/></title>
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
				alert("<fmt:message key='message.select.account'/>");
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
		$("#create").click(function() { submitForm("account.do", "create"); });
		$("#delete").click(function() { submitForm("console.do", "delete"); });
		$("#import").click(function() { submitForm("console.do", "import"); });
		$(".xlist a[href=#]").click(function() {
			submitForm("account.do", "update", $(this).parents("tr:first").find("input[name=IDs]:checkbox").val());
		});
	});
	</script>
</head>
<body>
	<h1><fmt:message key="message.list.accounts"/></h1>
	<form name="aform" method="post">
		<input type="hidden" name="acton" value="account"/>
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
					<li><a href="#" id="import"><span><fmt:message key="menu.import"/></span></a></li>
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
							<th scope="col"><a href="#"><fmt:message key="account.address"/></a></th>
							<th scope="col"><a href="#"><fmt:message key="account.quota"/></a></th>
							<th scope="col"><a href="#">&nbsp</a></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="user" items="${users}" varStatus="status">
						<tr>
							<td><input type="checkbox" name="IDs" value="<c:out value='${user.ID}'/>"/></td>
							<td class="text-left"><a href="#"><c:out value="${user.userID}"/></a></td>
							<td class="text-right"><c:out value="${user.quota}"/> MB</td>
							<td>&nbsp;</td>
						</tr>
						</c:forEach>
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
