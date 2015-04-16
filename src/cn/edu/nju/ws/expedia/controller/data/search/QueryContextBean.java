package cn.edu.nju.ws.expedia.controller.data.search;

import cn.edu.nju.ws.expedia.search.Util;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/15.
 */
public class QueryContextBean {
    private String query = null;
    private int searchType = Util.SYNSET_TAGS_SEARCH;
    private List<String> seleTagIDs = null;
    private List<String> exclTagIDs = null;

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getSeleTagIDs() {
        return seleTagIDs;
    }

    public void setSeleTagIDs(List<String> seleTagIDs) {
        this.seleTagIDs = seleTagIDs;
    }

    public List<String> getExclTagIDs() {
        return exclTagIDs;
    }

    public void setExclTagIDs(List<String> exclTagIDs) {
        this.exclTagIDs = exclTagIDs;
    }
}
