package cn.udesk.model;

/**
 * Created by user on 2018/3/28.
 */

public class NavigationMode {

    //文字的显示内容
    private String name;
    //用来映射选择后对应的操作
    private int id;

    public NavigationMode(String name, int id) {
        this.name = name;
        this.id = id;
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
}
