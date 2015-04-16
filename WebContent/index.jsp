<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="cn.edu.nju.ws.expedia.search.Util" %>
<%
  String ststr = request.getParameter("t");
  int searchType = Util.SYNSET_TAGS_SEARCH;
  if (ststr  != null)
    searchType = Integer.valueOf(ststr);
%>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Expedia | Explore DBpedia.</title>
  <link rel="stylesheet" type="text/css" href="./css/bootstrap.min.css" />
  <link rel="stylesheet" type="text/css" href="./css/bootstrap-theme.min.css" />
  <link rel="stylesheet" type="text/css" href="./css/expedia.css" />
</head>
<body>
<nav class="navbar navbar-default">
  <div class="container-fluid">
    <!-- Brand and toggle get grouped for better mobile display -->
      <a class="navbar-brand" href="./index.jsp"><img alt="Expedia" class="logo" src="./img/logo.jpg"> Expedia</a>
    <p class="navbar-text">Explore DBpedia.</p>
    </div>
  </div><!-- /.container-fluid -->
</nav>

<div class="search-box">
<h1>Expedia</h1>
  <form class="form-horizontal qbox" id="qform">
    <input class="form-control query-input" type="text" id="query" placeholder="Input some keywords.">
    <button type="submit" class="btn btn-success">Search</button>
  </form>
</div>

<p style="display:none" id="search-type"><%=searchType%></p>
<script type="text/javascript" src="./script/jquery-2.1.3.min.js"></script>
<script type="text/javascript" src="./script/bootstrap.min.js"></script>
<script type="text/javascript" src="./script/cryptojs.min.js"></script>
<script type="text/javascript" src="./script/expedia.js"></script>
</body>
</html>
