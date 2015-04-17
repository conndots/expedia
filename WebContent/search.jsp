<%--
  Created by IntelliJ IDEA.
  User: Xiangqian
  Date: 2015/4/14
  Time: 19:59
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="cn.edu.nju.ws.expedia.search.Util,cn.edu.nju.ws.expedia.search.QueryContext,cn.edu.nju.ws.expedia.search.Searchable"%>
<%@ page import="java.net.URLDecoder,cn.edu.nju.ws.expedia.search.filter.SemanticTagsFilter,cn.edu.nju.ws.expedia.search.filter.SemFilterFactory" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="cn.edu.nju.ws.expedia.model.rdf.estore.EStoreNodeFactory" %>
<%@ page import="cn.edu.nju.ws.expedia.search.SemTag" %>
<%@ page import="cn.edu.nju.ws.expedia.util.ConfReader" %>
<%@ page import="java.util.concurrent.Executors" %>
<%@ page import="java.util.concurrent.ExecutorService" %>
<%
  int pageEntityNum = Integer.valueOf(ConfReader.getConfProperty("page_entity_num"));
  String stypeStr = request.getParameter("t");
  int searchType = 2;
  if (stypeStr != null) {
    searchType = Integer.valueOf(stypeStr);
  }
  String query = request.getParameter("q");
  String pageStr = request.getParameter("p");
  int currPage = 0;
  if (pageStr != null) {
    currPage = Integer.valueOf(pageStr);
  }

  String setagsStr = request.getParameter("st");
  List<String> selectedTags = null;
  if (setagsStr != null && setagsStr.length() > 0) {
    String[] splits = setagsStr.split(";");
    selectedTags = new ArrayList<String>(splits.length);
    for (String setag : splits) {
      selectedTags.add(setag);
    }
  }

  String extagsStr = request.getParameter("et");
  List<String> excludedTags = null;
  if (extagsStr != null && extagsStr.length() > 0) {
    String[] splits = extagsStr.split(";");
    excludedTags = new ArrayList<String>(splits.length);
    for (String extag : splits) {
      excludedTags.add(extag);
    }
  }

  QueryContext qcontext = (QueryContext) session.getAttribute("qcontext");
  if (qcontext == null) {
    SemanticTagsFilter filter = SemFilterFactory.getSemanticTagsFilter(searchType);
    qcontext = QueryContext.getInstance(searchType, query, selectedTags, excludedTags, EStoreNodeFactory.getInstance(), filter);
    session.setAttribute("qcontext", qcontext);
  }
  else {
    String currID = Util.getQueryContextIDFrom(searchType, query, selectedTags, excludedTags);
    if (! currID.equals(qcontext.getIdentifier())) {
      SemanticTagsFilter filter = SemFilterFactory.getSemanticTagsFilter(searchType);
      qcontext = QueryContext.getInstance(searchType, query, selectedTags, excludedTags, EStoreNodeFactory.getInstance(), filter);
      session.setAttribute("qcontext", qcontext);
    }
  }

  List<Searchable> pageResult = new ArrayList<Searchable>(pageEntityNum);
  int totalResultsNum = qcontext.getQueryEntitiesInPage(currPage, pageResult);

  final QueryContext finalContext = qcontext;
  if (searchType > 0) {
    ExecutorService tagsExe = Executors.newSingleThreadExecutor();
    tagsExe.execute(new Runnable() {
      @Override
      public void run() {
        try {
          finalContext.startSetCandidateSemTags();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    });
    tagsExe.shutdown();
  }
  final int finalPage = currPage;
  ExecutorService snippetsExe = Executors.newSingleThreadExecutor();
  snippetsExe.execute(new Runnable() {
    @Override
    public void run() {
      finalContext.loadEntitySnippetsForPage(finalPage);
    }
  });
  snippetsExe.shutdown();
%>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title><%=query%> - Expedia</title>
  <link rel="stylesheet" type="text/css" href="./css/bootstrap.min.css" />
  <link rel="stylesheet" type="text/css" href="./css/bootstrap-theme.min.css" />
  <link rel="stylesheet" type="text/css" href="./css/expedia.css" />
</head>
<body>
<nav class="navbar navbar-default">
  <div class="container-fluid">
    <!-- Brand and toggle get grouped for better mobile display -->
    <a class="navbar-brand" href="./index.jsp"><img alt="Expedia" class="logo" src="./img/logo.jpg"> Expedia</a>
    <form class="navbar-form navbar-left" role="search" id="qform">
      <div class="form-group">
        <input type="text" class="form-control" id="query" placeholder="Input some keywords." value="<%=query%>" width="800px">
      </div>
      <button type="submit" class="btn btn-success">Search</button>
    </form>
  </div><!-- /.container-fluid -->
</nav>
<% List<SemTag> seleTags = qcontext.getSelectedTags();
  List<SemTag> exclTags= qcontext.getExcludedTags();
  if ((seleTags != null && seleTags.size() > 0) || (exclTags != null && exclTags.size() > 0)) {
%>
<div id="tag-restricts">
<%boolean hasRestrict = false;
  if (seleTags != null && seleTags.size() > 0) {
	  hasRestrict = true;
    for (SemTag setag : seleTags) {
      String label = setag.getRepresentiveLabel();
      String id = setag.getIdentifier();
      %>
        <div class="stag selected" tagID="<%=id%>" title="Click to remove this restrict."><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> &nbsp;<%=label%></div>
      <%
    }
  }
        if (exclTags != null && exclTags.size() > 0) {
      	  hasRestrict = true;
          for(SemTag extag : exclTags) {
            String label = extag.getRepresentiveLabel();
            String id = extag.getIdentifier();
            %>
  <div class="stag excluded" tagID="<%=id%>" title="Click to remove this restrict."><span class="glyphicon glyphicon-minus" aria-hidden="true"></span> &nbsp;<%=label%></div>
            <%
          }
        }
        if (hasRestrict) {
        	%>
        	<button class="btn btn-danger btn-xs removeall"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> &nbsp;remove all</button>
        	<% 
        }
%>
</div>
<%
  }
%>
<div class="results-block">
<%
  if (searchType > 0) {
    %>
  <div class="list-group semfilter-panel loading-data" >

  </div>
<%
	} 
%>

<div class="results-panel">
<%
  for (int i = 0; i < pageResult.size(); i ++) {
    Searchable entity = pageResult.get(i);
    String uri = entity.getURI(), id = entity.getIdentifier(), label = entity.getLabel(false);
    %>
    <div class="entity-block" id="<%=id%>" uri="<%=uri%>">
      <h3><span class="entity-link link" title="Click to browse." uri="<%=uri%>"><%=label%></span></h3>
      <div class="entity-snippets loading-data">
        <ul>
        </ul>
      </div>
    </div>
    <%
  }
  if (totalResultsNum > 0) {
    int pages = totalResultsNum / pageEntityNum + 1;
    int start = (currPage - 4 >= 0) ? currPage - 4 : 0;
    int end = (start + 9 < pages) ? start + 9 : pages;
    %>
    <nav>
      <ul class="pagination">
        <li class="<%=currPage > 0 ? "" : "disabled"%>"><span class="query-link link" aria-label="Previous" page="<%=currPage - 1%>"><span aria-hidden="true">&laquo;</span></span></li>
        <%
          if (start > 0) {
            %>
        <li class="disabled"><span>...</span></li>
        <%
          }

          for (int i = start; i < end; i ++) {
            %>
              <li class="<%=(i==currPage ? "active" : "")%>"><span class="query-link link" page="<%=i%>"><%=(i + 1)%></span></li>
            <%
          }


          if (end < pages) {
        %>
        <li class="disabled"><span>...</span></li>
        <%
          }
        %>
        <li class="<%=currPage < pages ? "" : "disabled"%>"><span class="query-link link" aria-label="Previous" page="<%=currPage + 1%>"><span aria-hidden="true">&raquo;</span></span></li>
      </ul>
    </nav>
    <%
  }
  %>

  </div>
</div>

<p style="display:none" id="search-type"><%=searchType%></p>
<p style="display:none" id="curr-query"><%=query%></p>
<p style="display:none" id="curr-page"><%=currPage%></p>
<script type="text/javascript" src="./script/jquery-2.1.3.min.js"></script>
<script type="text/javascript" src="./script/bootstrap.min.js"></script>
<script type="text/javascript" src="./script/spin.min.js"></script>
<script type="text/javascript" src="./script/jquery.spin.js"></script>
<script type="text/javascript" src="./script/cryptojs.min.js"></script>
<script type="text/javascript" src="./script/expedia.js"></script>
</body>
</html>
