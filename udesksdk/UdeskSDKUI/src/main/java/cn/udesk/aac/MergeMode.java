package cn.udesk.aac;

public class MergeMode<T> {

    int type ;
    private T data;
    long id;

    public MergeMode() {
    }

    public MergeMode(int type,long id) {
        this.type = type;
    }

    public MergeMode(int type, T data, long id) {
        this.type = type;
        this.data = data;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
