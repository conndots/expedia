package cn.edu.nju.ws.expedia.search.filter;

import java.util.Arrays;
import java.util.List;

import cn.edu.nju.ws.expedia.search.SemTag;
import cn.edu.nju.ws.expedia.util.UriUtil;

public class TypeTag implements SemTag {
	private String uri = null;
	private String label = null;
	public static TypeTag getInstance(String uri) {
		TypeTag tag = new TypeTag();
		tag.uri = uri;
		String lname = UriUtil.getLocalnameFromUri(uri);
		if (lname != null)
			tag.label = UriUtil.localnameSplit(lname, " ");
		return tag;
	}

	@Deprecated
	@Override
	public double getCost(SemanticTagsFilter filter) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getRepresentiveLabel() {
		return this.label;
	}

	@Override
	public List<String> getLabels() {
		// TODO Auto-generated method stub
		return Arrays.asList(this.label);
	}

	@Override
	public String getIdentifier() {
		return this.uri;
	}
	
	public int hashCode() {
		return this.uri.hashCode();
	}
	public boolean equals(Object o) {
		if (o instanceof TypeTag) {
			TypeTag tt = (TypeTag) o;
			return this.uri.equals(tt.uri);
		}
		return false;
	}

}
