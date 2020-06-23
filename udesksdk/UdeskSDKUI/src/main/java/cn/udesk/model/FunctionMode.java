package cn.udesk.model;

import cn.udesk.R;

/**
 * Created by user on 2018/3/19.
 */

public class FunctionMode {
    //显示内容
    private String name;
    //用来映射选择后对应的操作 id值 前20 是udesk 预留的,  客户自定义添加的
    private int id;
    //如 R.drawable.udesk_001
    //显示的图标
    private int mIconSrc ;

    public FunctionMode(String name, int id, int mIconSrc) {
        this.name = name;
        this.id = id;
        this.mIconSrc = mIconSrc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getmIconSrc() {
        return mIconSrc;
    }

    public void setmIconSrc(int mIconSrc) {
        this.mIconSrc = mIconSrc;
    }
}
