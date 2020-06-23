package cn.udesk.photoselect.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 2018/3/6.
 */

public class LocalMedia implements Parcelable {

    private String path;
    private long duration;
    private String pictureType;
    private int width;
    private int height;
    private int orientation;

    private boolean selected;//是否被选中,内部使用,无需关心
    private boolean selectedOriginal;//用户选择时是否选择了原图选项

    public LocalMedia(String path, long duration,  String pictureType, int width, int height,int orientation) {
        this.path = path;
        this.duration = duration;
        this.pictureType = pictureType;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
    }

    protected LocalMedia(Parcel in) {
        path = in.readString();
        duration = in.readLong();
        pictureType = in.readString();
        width = in.readInt();
        height = in.readInt();
        orientation = in.readInt();
        selected = in.readByte() != 0;
        selectedOriginal = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeLong(duration);
        dest.writeString(pictureType);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(orientation);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (selectedOriginal ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocalMedia> CREATOR = new Creator<LocalMedia>() {
        @Override
        public LocalMedia createFromParcel(Parcel in) {
            return new LocalMedia(in);
        }

        @Override
        public LocalMedia[] newArray(int size) {
            return new LocalMedia[size];
        }
    };

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelectedOriginal() {
        return selectedOriginal;
    }

    public void setSelectedOriginal(boolean selectedOriginal) {
        this.selectedOriginal = selectedOriginal;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPictureType() {
        return pictureType;
    }

    public void setPictureType(String pictureType) {
        this.pictureType = pictureType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
