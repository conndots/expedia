package cn.edu.nju.ws.expedia.search.filter;

import cn.edu.nju.ws.expedia.search.Util;

/**
 * Created by Xiangqian on 2015/4/14.
 */
public class SemFilterFactory {
    public static SemanticTagsFilter getSemanticTagsFilter(int searchType) {
        if (searchType == Util.SYNSET_TAGS_SEARCH) {
            return new SemanticTagSelectionMethod();
        }
        else if (searchType == Util.TYPE_TAGS_SEARCH) {
            //TODO
        }
        else if (searchType == Util.NO_TAGS_SEARCH) {
            return null;
        }
        return null;
    }
}
