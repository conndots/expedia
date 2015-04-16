package cn.edu.nju.ws.expedia.search.filter;

import cn.edu.nju.ws.expedia.model.ownmap.SynsetID2typeIDCache;
import cn.edu.nju.ws.expedia.model.ownmap.TypeID2SynsetIDCache;
import cn.edu.nju.ws.expedia.search.QueryContext;
import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SemTag;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SemanticTagSelectionMethod  implements SemanticTagsFilter {

	List<Searchable> entities;
	Set<SemTag> synsets;
	List<SemTag> synlist;
	QueryContext context;

	EntityStagDistanceFunction getDis;
	int a[];
	int b[];
	double c[];
	double[] w;

	public SemanticTagSelectionMethod() {
		this.getDis = EntityStagDistanceFunctionDefaultImpl.getInstance(this);
	}

	public void setEntities(List<Searchable> entities) {
		// TODO Auto-generated method stub
		this.entities=entities;
		this.getDis.initiation(this.entities);
	}
	public void setQueryContext(QueryContext context) {
		this.context = context;
	}

	public List<SemTag> getSelectedStags() {
		// TODO Auto-generated method stub
		List<SemTag> ans=new ArrayList<SemTag>();
		setInfo();
		EbmcAlgrithm alg=new EbmcAlgrithm();
		alg.SetN(this.synlist.size());
		alg.SetM(this.entities.size());
		alg.SetL(5.50);
		alg.SetWeight(this.w);
		alg.SetProfit(a, b, c);
		int ret[]=alg.getSelectID();
		for(int i=0;i<ret.length;i++)
		{
			ans.add(this.synlist.get(ret[i]));
		}
		return ans;
	}

	private Logger logger = Logger.getLogger(SemanticTagSelectionMethod.class);
	public List<String> getTypeIDsOfSemTag(SemTag tag) {
		return SynsetID2typeIDCache.getInstance().getMappedTypeIDsFromSynsetID(tag.getIdentifier());
	}

	public SemTag getSemTagForTypeID(String typeID) {
		String sid = TypeID2SynsetIDCache.getInstance().getMappedWordnetIDsFromTypeID(typeID);
		if (sid == null)
			return null;
		return SynsetTag.getInstance(sid);
	}

	public SemTag getSemTagInstance(String tagID) {
		return SynsetTag.getInstance(tagID);
	}

	public double getSemTagCost(SemTag stag) {
		int count = this.getDis.getSemTagTaggingCount(stag);
		int max = this.getDis.getMaximumSemTagTaggingCount();

		double cost = 1d - ((count) / max);

		return cost;
	}

	void setInfo()
	{
		HashSet<SemTag> selected = new HashSet<SemTag>();
		List<SemTag> s = this.context.getSelectedTags();
		if (s != null)
			selected.addAll(s);
		this.synsets=new HashSet<SemTag>();
		int size=0;
		for(int i=0;i<this.entities.size();i++)
		{
			Set<SemTag> temp=this.entities.get(i).getTaggedSemTags(this);
			temp.removeAll(selected);
			size+=temp.size();
			this.synsets.addAll(temp);
		}
		a=new int[size];
		b=new int[size];
		c=new double[size];
		w=new double[this.synsets.size()];
		size=0;
		this.synlist=new ArrayList<SemTag>(this.synsets);
		for(int i=0;i<this.entities.size();i++)
		{
			Set<SemTag> temp=this.entities.get(i).getTaggedSemTags(this);
//			System.out.println("entity " + this.entities.get(i).getIdentifier() + " candidate num: " + temp.size());
			for(SemTag t:temp)
			{
				a[size]=this.synlist.indexOf(t);
				b[size]=i;
				c[size]=(double) (this.entities.get(i).getSortScore()*(1.0/getDis.getDistanceOfEntityAndStag(this.entities.get(i), t)));
				size++;
			}
		}
		for(int i=0;i<this.synlist.size();i++)
		{
			w[i]=(double)this.synlist.get(i).getCost(this);
		}
		selected.clear();
		
	}

}
