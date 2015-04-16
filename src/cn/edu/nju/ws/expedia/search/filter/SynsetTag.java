package cn.edu.nju.ws.expedia.search.filter;

import cn.edu.nju.ws.expedia.model.wordnet.Synset;
import cn.edu.nju.ws.expedia.model.wordnet.cache.SynsetFactory;
import cn.edu.nju.ws.expedia.search.SemTag;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/10.
 */
public class SynsetTag implements SemTag {
    static SynsetTag getInstance(String sid) {
        SynsetTag instance = new SynsetTag();
        instance.synset = SynsetFactory.getInstance().getSynset(sid);
        return instance;
    }

    protected Synset synset = null;


    public String getIdentifier() {
        return this.synset.getIdentifier();
    }

    public boolean equals(Object o) {
        if (o instanceof SynsetTag) {
            SynsetTag st = (SynsetTag) o;
            return this.getIdentifier().equals(st.getIdentifier());
        }
        return false;
    }

    public int hashCode() {
        return this.synset.getIdentifier().hashCode();
    }

    public double getCost(SemanticTagsFilter filter) {
        return filter.getSemTagCost(this);
    }

    public String getRepresentiveLabel() {
        return this.synset.getLabels().get(0);
    }

    public List<String> getLabels() {
        return this.synset.getLabels();
    }
}
