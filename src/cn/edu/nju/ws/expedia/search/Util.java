package cn.edu.nju.ws.expedia.search;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Xiangqian on 2015/4/14.
 */
public class Util {
    public static final int SYNSET_TAGS_SEARCH = 2,
            TYPE_TAGS_SEARCH = 1,
            NO_TAGS_SEARCH = 0;
    public static final HashSet<String> FILTERED_TYPES = new HashSet<String>();

    static {
        FILTERED_TYPES.add(cn.edu.nju.ws.expedia.model.rdf.Util.OWL_NS + "Thing");
    }

    public static String getQueryContextIDFrom(String query, List<String> selectedTagIDs, List<String> excludedTagIDs) {
        StringBuilder id = new StringBuilder(query);
        id.append("+[");
        if (selectedTagIDs != null) {
            Collections.sort(selectedTagIDs);
            for (String stagID : selectedTagIDs) {
                id.append(stagID).append(";");
            }
        }
        id.append("]-[");
        if (excludedTagIDs != null) {
            Collections.sort(excludedTagIDs);
            for (String etagID : excludedTagIDs) {
                id.append(etagID).append(";");
            }
        }
        id.append("]");
        return id.toString();
    }
}
