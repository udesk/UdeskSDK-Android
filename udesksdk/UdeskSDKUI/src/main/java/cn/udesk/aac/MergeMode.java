package cn.udesk.aac;

public class MergeMode<T> {

    int type ;
    private T data;
    String id;

    public MergeMode() {
    }

    public MergeMode(int type,String id) {
        this.type = type;
        this.id = id;
    }

    public MergeMode(int type, T data, String id) {
        this.type = type;
        this.data = data;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    @Override
    public String toString() {
        return "type="+type+",id="+id;
    }
}
