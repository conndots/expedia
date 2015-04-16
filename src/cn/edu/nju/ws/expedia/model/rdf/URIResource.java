package cn.edu.nju.ws.expedia.model.rdf;

import cn.edu.nju.ws.expedia.util.TwoTuple;
import cn.edu.nju.ws.expedia.util.UriUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xiangqian on 2015/4/7.
 */
public class URIResource implements Node {
    protected String uri = null, id = null, nodeType = null; //Load at first time.
    protected List<URIResource> types = null;
    protected EntityQuery equery = null;
    protected String lname = null, label = null;

    public URIResource(String uri, String id, String nodeType, EntityQuery equery) {
        this.uri = uri;
        this.id = id;
        this.nodeType = nodeType;
        this.equery = equery;
    }

    public String getURI() {
        return this.uri;
    }

    public String getIdentifier() {
        return this.id;
    }

    public String getNodeType() {
        return this.nodeType;
    }

    public String getLabel(boolean loadFromDB) {
        if (this.label != null)
            return this.label;
        String label = this.equery.getLabel(this.id, loadFromDB);
        if (label != null)
            return label;
        if (this.lname == null) {
            this.lname = UriUtil.getLocalnameFromUri(this.uri);
            if (this.nodeType.equals(Node.CATEGORY))
                this.lname = this.lname.replace("/Category:", "");
            this.lname = UriUtil.localnameSplit(this.lname, " ");
        }
        return lname;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<URIResource> getTypes(boolean loadFromDB) {
        if(this.types != null) {
            return this.types;
        }
        List<TwoTuple<URIResource, Node>> triples = null;
        triples = this.getDescriptions(loadFromDB);
        if(triples != null) {
            this.types = new ArrayList<URIResource>();
            for (TwoTuple<URIResource, Node> triple : triples) {
                if (triple.getFirst().getURI().equals(Util.RDF_TYPE)) {
                    this.types.add((URIResource) triple.getSecond());
                }
            }
        }
        return this.types;
    }

    public List<TwoTuple<URIResource, Node>> getDescriptions(boolean loadFromDB) {
        List<TwoTuple<URIResource, Node>> triples = this.equery.getEntityDescriptions(this.id, loadFromDB);
        if (triples != null && this.label == null) {
            for(TwoTuple<URIResource, Node> triple : triples) {
                if (triple.getFirst().getURI().equals(Util.RDFS_LABEL)) {
                    this.label = ((Literal) triple.getSecond()).content;
                    break;
                }
            }
        }
        return triples;
    }

    public static void main(String[] args) {
        String u = "http://dbpedia.org/resource/!%7B%7BKhandwa_district";
        try {
            System.out.println(URLDecoder.decode(u, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public boolean equals(Object o) {
        if (o instanceof URIResource) {
            URIResource u = (URIResource) o;
            return this.id.equals(u.id);
        }
        return false;
    }
    public int hashCode() {
        return this.id.hashCode();
    }
    public String toString() {
        return this.getLabel(false);
    }
}
