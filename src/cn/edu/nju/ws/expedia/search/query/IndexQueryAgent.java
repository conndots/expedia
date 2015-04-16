package cn.edu.nju.ws.expedia.search.query;

import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SearchableFactory;
import cn.edu.nju.ws.expedia.search.SemTag;
import cn.edu.nju.ws.expedia.search.filter.SemanticTagsFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Xiangqian on 2015/4/10.
 */
public interface IndexQueryAgent {
    public ArrayList<Searchable> getEntitiesForQuery(String query, List<SemTag> selectedTags, List<SemTag> excludedCandidateTypeIDs, SemanticTagsFilter filter,
                                                     SearchableFactory factory, Comparator<Searchable> comp, int maximumNum);
    public ArrayList<Searchable> getEntitiesForQuery(String query, SearchableFactory factory, Comparator<Searchable> comp, int maximumNum);
}
