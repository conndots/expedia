package cn.edu.nju.ws.expedia.search.filter;

import cn.edu.nju.ws.expedia.search.QueryContext;
import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SemTag;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/8.
 */
public interface SemanticTagsFilter {
    public void setEntities(List<Searchable> entities);
    public List<SemTag> getSelectedStags();
    public List<String> getTypeIDsOfSemTag(SemTag tag);
    public void setQueryContext(QueryContext context);
    public SemTag getSemTagForTypeID(String typeID);
    public SemTag getSemTagInstance(String tagID);
    public double getSemTagCost(SemTag stag);
}
