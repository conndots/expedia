package cn.edu.nju.ws.expedia.model.rdf;

import cn.edu.nju.ws.expedia.util.MD5;

/**
 * Created by Xiangqian on 2015/4/8.
 */
public class Util {
    public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";
    public static final String RDFS_LABEL = RDFS_NS + "label",
            RDF_TYPE = RDF_NS + "type",
            RDFS_SUBCLASSOF = RDFS_NS + "subClassOf";

    public static final String RDFS_LABEL_MD5 = "4f75ce12336ab89a4edebd807cd62265",
            RDF_TYPE_MD5 = "c74e2b735dd8dc85ad0ee3510c33925f",
            PURL_SUBJECT_MD5 = "256bd1507ee9b0e675388546e57db7e8";

    public static void main(String[] args) {
        System.out.println(MD5.makeMD5Str("http://purl.org/dc/terms/subject"));
    }
}
