package cn.edu.nju.ws.expedia.model.rdf.ont;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Xiangqian on 2015/4/13.
 */
public class OntologyAgent {
    public static Set<String> getSuperClasses(String uri, boolean isDirect) {
        if (uri == null)
            return null;
        if (isDirect) {
            return new HashSet<String>(SuperClassCache.getInstance().getSuperClassesOf(uri));
        }
        else {
            HashSet<String> supers = new HashSet<String>();
            LinkedList<String> queue = new LinkedList<String>();
            queue.add(uri);
            while(! queue.isEmpty()) {
                String current = queue.pollFirst();
                if (supers.contains(current))
                    continue;
                if (! current.equals(uri)) {
                    supers.add(current);
                }
                List<String> sups = SuperClassCache.getInstance().getSuperClassesOf(current);
                for (String sup : sups) {
                    if (! supers.contains(sup))
                        queue.addLast(sup);
                }
                sups = null;
            }
            return supers;
        }
    }
}
