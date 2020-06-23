package cn.udesk.photoselect;

import java.util.ArrayList;

import cn.udesk.photoselect.entity.LocalMedia;

/**
 * Created by user on 2018/3/7.
 */

public class SelectResult {
    public static ArrayList<LocalMedia> photos = new ArrayList<>();
    public static ArrayList<LocalMedia> allLocalMedia = new ArrayList<>();
    public static ArrayList<LocalMedia> selectLocalMedia = new ArrayList<>();

    public static boolean isSelected(LocalMedia photo) {
        if (photos.size() > 0) {
            for (LocalMedia localMedia : photos) {
                if (photo.getPath().equals(localMedia.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addPhoto(LocalMedia photo) {
        try {
            photo.setSelected(true);
            boolean isNeedAdd = true;
            if (photos.size() > 0) {
                for (LocalMedia localMedia : photos) {
                    if (photo.getPath().equals(localMedia.getPath())) {
                        isNeedAdd = false;
                        break;
                    }
                }
            }
            if (isNeedAdd) {
                photos.add(photo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void removePhoto(LocalMedia photo) {
        try {
            if (photos.size() > 0) {
                for (LocalMedia localMedia : photos) {
                    if (photo.getPath().equals(localMedia.getPath())) {
                        photos.remove(localMedia);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removePhoto(int photoIndex) {
        removePhoto(photos.get(photoIndex));
    }

    public static void clear() {
        photos.clear();
        allLocalMedia.clear();
        selectLocalMedia.clear();
    }

    public static boolean isEmpty() {
        return photos.isEmpty();
    }

    public static int count() {
        return photos.size();
    }

    /**
     * 获取选择器应该显示的数字
     *
     * @param photo 当前图片
     * @return 选择器应该显示的数字
     */
    public static String getSelectorNumber(LocalMedia photo) {
        try {
            for (int i = 0; i < photos.size(); i++) {
                if (photos.get(i).getPath().equals(photo.getPath())) {
                    return String.valueOf(i + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    public static String getPhotoPath(int position) {
        return photos.get(position).getPath();
    }

    public static String getPhotoType(int position) {
        try {
            return photos.get(position).getPictureType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}
