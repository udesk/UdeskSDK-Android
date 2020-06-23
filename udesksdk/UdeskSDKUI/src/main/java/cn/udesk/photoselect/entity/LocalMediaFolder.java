package cn.udesk.photoselect.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2018/3/6.
 */

public class LocalMediaFolder implements Parcelable{
    private String name;
    private String path;
    private  String firstFilePath;  //文件夹中第一个文件的路径
    private int checkNum;
    private boolean isChecked;
    private List<LocalMedia> media = new ArrayList<LocalMedia>();

    public LocalMediaFolder(){

    }

    protected LocalMediaFolder(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.firstFilePath = in.readString();
        this.checkNum = in.readInt();
        this.isChecked = in.readByte() != 0;
        this.media = in.createTypedArrayList(LocalMedia.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeString(this.firstFilePath);
        dest.writeInt(this.checkNum);
        dest.writeByte((byte) (this.isChecked ? 1 : 0));
        dest.writeTypedList(this.media);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocalMediaFolder> CREATOR = new Creator<LocalMediaFolder>() {
        @Override
        public LocalMediaFolder createFromParcel(Parcel in) {
            return new LocalMediaFolder(in);
        }

        @Override
        public LocalMediaFolder[] newArray(int size) {
            return new LocalMediaFolder[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFirstFilePath() {
        return firstFilePath;
    }

    public void setFirstFilePath(String firstFilePath) {
        this.firstFilePath = firstFilePath;
    }

    public int getNum() {
        return media.size();
    }

    public int getCheckNum() {
        return checkNum;
    }

    public void setCheckNum(int checkNum) {
        this.checkNum = checkNum;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public List<LocalMedia> getMedia() {
        if (media == null){
            media = new ArrayList<>();
        }
        return media;
    }

    public void setMedia(List<LocalMedia> media) {
        this.media = media;
    }




}
