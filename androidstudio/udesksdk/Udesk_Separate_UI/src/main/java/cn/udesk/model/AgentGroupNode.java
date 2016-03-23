package cn.udesk.model;

/**
 * Created by sks on 2016/3/15.
 */
public class AgentGroupNode {

    String id;
    String parentId;
    String item_name;
    String has_next;
    String group_id;
    String link;

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getHas_next() {
        return has_next;
    }

    public void setHas_next(String has_next) {
        this.has_next = has_next;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
