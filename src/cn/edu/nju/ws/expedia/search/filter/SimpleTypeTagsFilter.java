package cn.edu.nju.ws.expedia.search.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.HashMultiset;

import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.search.QueryContext;
import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SemTag;
import cn.edu.nju.ws.expedia.search.Util;
import cn.edu.nju.ws.expedia.util.MD5;

public class SimpleTypeTagsFilter implements SemanticTagsFilter {
	protected List<Searchable> entities = null;
	protected final HashMultiset<TypeTag> tagTaggingCounts = HashMultiset.create();
	protected QueryContext qcontext = null;
	@Override
	public void setEntities(List<Searchable> entities) {
		List<SemTag> s = this.qcontext.getSelectedTags();
		Set<SemTag> selected = new HashSet<SemTag>();
		if (s != null)
			selected.addAll(selected);
		this.entities = entities;
		
		for (Searchable entity : entities) {
			List<URIResource> types = entity.getTypes(true);
			for (URIResource type : types) {
				String uri = type.getURI();
				if (Util.FILTERED_TYPES.contains(uri))
					continue;
				TypeTag tag = TypeTag.getInstance(uri);
				if (selected.contains(tag))
					continue;
				this.tagTaggingCounts.add(tag);
			}
		}
	}

	@Override
	public List<SemTag> getSelectedStags() {
		final HashMultiset<TypeTag> counts = this.tagTaggingCounts;
		PriorityQueue<TypeTag> pqueue = 
				new PriorityQueue<TypeTag>(this.tagTaggingCounts.size(), new Comparator<TypeTag>() {

					@Override
					public int compare(TypeTag t0, TypeTag t1) {
						int n0 = counts.count(t0),
								n1 = counts.count(t1);
						return n1 - n0;
					}
					
				});
		for (TypeTag tag : this.tagTaggingCounts.elementSet()) {
			pqueue.add(tag);
		}
		List<SemTag> ret = new ArrayList<SemTag>(13);
		int threshold = this.entities.size() / 3;
		while (! pqueue.isEmpty()) {
			TypeTag tag = pqueue.poll();
			if (this.tagTaggingCounts.count(tag) > threshold)
				ret.add(tag);
			else
				break;
		}
		return ret;
	}

	@Override
	public List<String> getTypeIDsOfSemTag(SemTag tag) {
		return Arrays.asList(MD5.makeMD5Str(tag.getIdentifier()));
	}

	@Override
	public void setQueryContext(QueryContext context) {
		this.qcontext = context;
	}

	@Deprecated
	@Override
	public SemTag getSemTagForTypeID(String typeID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SemTag getSemTagInstance(String tagID) {
		return TypeTag.getInstance(tagID);
	}

	@Deprecated
	@Override
	public double getSemTagCost(SemTag stag) {
		// TODO Auto-generated method stub
		return 0;
	}

}
