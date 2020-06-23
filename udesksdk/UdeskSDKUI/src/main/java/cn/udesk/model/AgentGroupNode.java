package cn.udesk.model;

import java.io.Serializable;

import udesk.core.utils.UdeskUtils;

/**
 * Created by sks on 2016/3/15.
 */
public class AgentGroupNode implements Serializable {

    String id;//menu_id
    String parentId;//父级菜单id
    String item_name;
    String has_next;//是否有下一个
    String link;//弃用

    public boolean getHas_next() {
        return UdeskUtils.objectToBoolean(has_next);
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
