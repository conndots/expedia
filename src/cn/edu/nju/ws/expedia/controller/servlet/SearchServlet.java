package cn.edu.nju.ws.expedia.controller.servlet;

import cn.edu.nju.ws.expedia.controller.data.search.QueryContextBean;
import cn.edu.nju.ws.expedia.controller.data.search.SemTagBean;
import cn.edu.nju.ws.expedia.controller.data.search.SnippetBean;
import cn.edu.nju.ws.expedia.controller.data.search.SnippetsBean;
import cn.edu.nju.ws.expedia.model.rdf.estore.EStoreNodeFactory;
import cn.edu.nju.ws.expedia.search.QueryContext;
import cn.edu.nju.ws.expedia.search.SemTag;
import cn.edu.nju.ws.expedia.search.Util;
import cn.edu.nju.ws.expedia.search.filter.SemFilterFactory;
import cn.edu.nju.ws.expedia.search.filter.SemanticTagsFilter;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/13.
 */
@WebServlet(name="searchServlet", urlPatterns="/search")
public class SearchServlet extends HttpServlet{
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
        this.doProcess(request, response);
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException{
        this.doProcess(request, response);
    }

    private static QueryContext getNewQueryContext(QueryContextBean qcbean) {
        int searchType = qcbean.getSearchType();
        String query = qcbean.getQuery();
        List<String> selectedTagIDs = qcbean.getSeleTagIDs(),
                excludedTagIDs = qcbean.getExclTagIDs();
        SemanticTagsFilter filter = SemFilterFactory.getSemanticTagsFilter(searchType);
        QueryContext qcontext = QueryContext.getInstance(query, selectedTagIDs, excludedTagIDs, EStoreNodeFactory.getInstance(), filter);
        return qcontext;
    }
    public void doProcess(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException{
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        String qcontext = request.getParameter("qcontext");
        String qcontextDecoded = URLDecoder.decode(qcontext, "utf-8");
        QueryContextBean qcbean = JSON.parseObject(qcontextDecoded, QueryContextBean.class);

        String data = request.getParameter("data");
        if (data != null)
            data = URLDecoder.decode(data, "utf-8");

        QueryContext queryContext = (QueryContext) session.getAttribute("qcontext");

        if (queryContext == null) {
            queryContext = getNewQueryContext(qcbean);
            session.setAttribute("qcontext", queryContext);
        }
        else {
            String currID = Util.getQueryContextIDFrom(qcbean.getQuery(), qcbean.getSeleTagIDs(), qcbean.getExclTagIDs());
            if (! currID.equals(queryContext.getIdentifier())) {
                queryContext = getNewQueryContext(qcbean);
                session.setAttribute("qcontext", queryContext);
            }
        }

        String respJSONStr = null;

        if ("getSnippetsForPage".equals(action)) {
            int pageNum = Integer.valueOf(data);
            List<List<SnippetBean>> snippets = queryContext.getEntitySnippetsForPage(pageNum, 2, TimeUnit.SECONDS);
            if (snippets != null)
            	respJSONStr = JSON.toJSONString(snippets);
            else
            	respJSONStr = "[]";
        }
        else if ("getCandidateTags".equals(action)) {
            try {
                List<SemTag> ctags = queryContext.getCandidateSemTags(2, TimeUnit.SECONDS);
                List<SemTagBean> candTags = new ArrayList<SemTagBean>(ctags == null ? 0 : ctags.size());
                if (ctags != null) {
                	for (SemTag ctag : ctags) {
                    	candTags.add(SemTagBean.getInstance(ctag));
                	}

                    respJSONStr = JSON.toJSONString(candTags);
                }
                else {
                	respJSONStr = "{\"status\": \"not ready\"}";
                }
                
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        else {
            respJSONStr = "";
        }

        response.getWriter().write(respJSONStr);
    }

}
