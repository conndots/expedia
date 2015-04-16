package cn.edu.nju.ws.expedia.model.wordnet;

/**
 * Created by Xiangqian on 2015/1/4.
 */
public class NounSynsetPointer extends SynsetPointer {
    public static final String INSTANCE_HYPONYM = "~i",
            INSTANCE_HYPERNYM = "@i",
            MEMBER_HOLONYM = "#m",
            SUBSTANCE_HOLONYM = "#s",
            PART_HOLONYM = "#p",
            MEMBER_MERONYM = "%m",
            SUBSTANCE_MERONYM = "%s",
            PART_MERONYM = "%p",
            ATTRIBUTE = "=",
            DERIVATIONALLY_RELATED_FORM = "+",
            MEMBER_OF_DOMAIN_TOPIC = "-c",
            MEMBER_OF_DOMAIN_REGION = "-r",
            MEMBER_OF_DOMAIN_USAGE = "-u";
}
