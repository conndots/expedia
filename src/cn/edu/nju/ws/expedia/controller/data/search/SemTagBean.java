package cn.edu.nju.ws.expedia.controller.data.search;

import cn.edu.nju.ws.expedia.search.SemTag;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/13.
 */
public class SemTagBean {
    protected String tagID = null,
            label = null;

    public static SemTagBean getInstance(SemTag tag) {
        SemTagBean instance = new SemTagBean();
        instance.tagID = tag.getIdentifier();
        instance.label = tag.getRepresentiveLabel();
        return instance;
    }

    public String getTagID() {
        return tagID;
    }

    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
