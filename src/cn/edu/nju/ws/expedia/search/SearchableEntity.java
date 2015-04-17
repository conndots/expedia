package cn.edu.nju.ws.expedia.search;

import cn.edu.nju.ws.expedia.model.ownmap.TypeID2SynsetIDCache;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.search.filter.SemanticTagsFilter;
import cn.edu.nju.ws.expedia.util.TwoTuple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Xiangqian on 2015/4/10.
 */
public class SearchableEntity implements Searchable {
    protected URIResource resource = null;
    protected double queryRelativeScore = -1d, sortScore = -1d, pagerankScore;
    protected String[] typeIDs = null;
    protected Set<SemTag> taggedTags = null;
    protected int hashCode;

    protected SearchableEntity(URIResource res) {
        this.resource = res;
        this.hashCode = res.getURI().hashCode();
    }

    protected SearchableEntity(URIResource res, String[] typeIDs) {
        this.resource = res;
        this.typeIDs = typeIDs;
        this.hashCode = res.getURI().hashCode();
    }


    public Set<SemTag> getTaggedSemTags(SemanticTagsFilter tagsFilter) {
        if (this.taggedTags != null)
            return this.taggedTags;

        this.taggedTags = new HashSet<SemTag>();
        for (String typeID : typeIDs) {
            if (Util.FILTERED_TYPES.contains(typeID))
                continue;
            SemTag stag = tagsFilter.getSemTagForTypeID(typeID);
            if (stag != null)
                this.taggedTags.add(stag);
        }
        return taggedTags;
    }

    public double getSortScore() {
        return this.sortScore;
    }

    public double getQueryRelativeScore() {
        return this.queryRelativeScore;
    }

    public void setSortScore(double sscore) {
        this.sortScore = sscore;
    }

    public String getIdentifier() {
        return this.resource.getIdentifier();
    }

    public String getURI() {
        return this.resource.getURI();
    }

    public String getLabel(boolean loadFromDB) {
        return this.resource.getLabel(loadFromDB);
    }

    public List<TwoTuple<URIResource, Node>> getDescriptions(boolean loadFromDB) {
        return this.resource.getDescriptions(loadFromDB);
    }
    
    public int hashCode() {
    	return this.hashCode;
    }
    public boolean equals(Object o) {
    	if (o instanceof Searchable) {
    		Searchable s = (Searchable) o;
    		return this.getIdentifier().equals(s.getIdentifier());
    	}
    	return false;
    }

	@Override
	public List<URIResource> getTypes(boolean loadFromDB) {
		return this.resource.getTypes(loadFromDB);
	}
    
}
