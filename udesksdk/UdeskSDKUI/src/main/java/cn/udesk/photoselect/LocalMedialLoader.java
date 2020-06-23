package cn.udesk.photoselect;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.photoselect.entity.LocalMedia;
import cn.udesk.photoselect.entity.LocalMediaFolder;

/**
 * Created by user on 2018/3/6.
 */

public class LocalMedialLoader {

    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
    private static final String DURATION = "duration";
    private static final String NOT_GIF = "!='image/gif'";
    private long videoMaxS = 0;
    private long videoMinS = 0;

    // 媒体文件数据库字段
    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.ORIENTATION,
            };

    // 获取图片or视频
    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private String getDurationCondition(long exMaxLimit, long exMinLimit) {
        try {
            long maxS = videoMaxS == 0 ? Long.MAX_VALUE : videoMaxS;
            if (exMaxLimit != 0) {
                maxS = Math.min(maxS, exMaxLimit);
            }

            return String.format(Locale.CHINA, "%d <%s duration and duration <= %d",
                    Math.max(exMinLimit, videoMinS),
                    Math.max(exMinLimit, videoMinS) == 0 ? "" : "=",
                    maxS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getSelectionArgsForAllMediaCondition(String time_condition, boolean isGif) {
        String condition = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + (isGif ? "" : " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)
                + " OR "
                + (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + time_condition) + ")"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0";
        return condition;
    }


    public void loadAllMedia(final FragmentActivity activity, final LocalMediaLoadListener imageLoadListener) {

        LoaderManager.getInstance(activity).initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String all_condition = getSelectionArgsForAllMediaCondition(getDurationCondition(0, 0), false);
                CursorLoader cursorLoader = new CursorLoader(activity, QUERY_URI, PROJECTION, all_condition, SELECTION_ALL_ARGS, ORDER_BY);
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                try {
                    List<LocalMediaFolder> fileFolders = new ArrayList<>();
                    LocalMediaFolder allImageFolder = new LocalMediaFolder();
                    LocalMediaFolder allVideoFolder = new LocalMediaFolder();
                    List<LocalMedia> allfiles = new ArrayList<>();
                    List<LocalMedia> allvideos = new ArrayList<>();
                    if (data != null) {
                        int count = data.getCount();
                        if (count > 0) {
                            data.moveToFirst();
                            do {
                                long id = data.getLong
                                        (data.getColumnIndexOrThrow(PROJECTION[0]));
                                String filePath = data.getString
                                        (data.getColumnIndexOrThrow(PROJECTION[1]));
                                String pictureType = data.getString
                                        (data.getColumnIndexOrThrow(PROJECTION[2]));

                                int w = data.getInt
                                        (data.getColumnIndexOrThrow(PROJECTION[3]));

                                int h = data.getInt
                                        (data.getColumnIndexOrThrow(PROJECTION[4]));

                                int duration = data.getInt
                                        (data.getColumnIndexOrThrow(PROJECTION[5]));
                                int orientation = data.getInt
                                        (data.getColumnIndexOrThrow(PROJECTION[6]));
                                String path;
                                final int mediaMimeType = UdeskUtil.isPictureType(pictureType);
                                if (UdeskUtil.isAndroidQ()){
                                    if (mediaMimeType == UdeskUtil.TYPE_IMAGE){
                                        path= ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id).toString();
                                    }else {
                                        path= ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,id).toString();
                                    }
                                }else {
                                    path = filePath;
                                }
                                LocalMedia file = new LocalMedia
                                        (path, duration, pictureType, w, h,orientation);

                                LocalMediaFolder folder = getImageFolder(activity.getApplicationContext(),path,filePath,fileFolders);
                                List<LocalMedia> files = folder.getMedia();
                                files.add(file);
                                allfiles.add(file);
                                if (mediaMimeType == UdeskUtil.TYPE_SHORT_VIDEO){
                                    allvideos.add(file);
                                }
                            } while (data.moveToNext());

                            if (allfiles.size() > 0) {
                                sortFolder(fileFolders);
                                allImageFolder.setFirstFilePath(allfiles.get(0).getPath());
                                allImageFolder.setMedia(allfiles);
                                allImageFolder.setName(activity.getString(R.string.udesk_img_video));
                                fileFolders.add(0, allImageFolder);
                            }
                            if (allvideos.size() > 0) {
                                allVideoFolder.setFirstFilePath(allvideos.get(0).getPath());
                                allVideoFolder.setMedia(allvideos);
                                allVideoFolder.setName(activity.getString(R.string.udesk_all_video));
                                fileFolders.add(1, allVideoFolder);
                            }
                            imageLoadListener.loadComplete(fileFolders);
                        }

                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });

    }

    /**
     * 创建相应文件夹
     *
     * @param path
     * @param imageFolders
     * @return
     */
    private LocalMediaFolder getImageFolder(Context context,String path,String filePath, List<LocalMediaFolder> imageFolders) {
        LocalMediaFolder newFolder = new LocalMediaFolder();
        try {
            File imageFile = new File(filePath);
            File folderFile = imageFile.getParentFile();
            for (LocalMediaFolder folder : imageFolders) {
                // 同一个文件夹下，返回自己，否则创建新文件夹
                if (folder.getName().equals(folderFile.getName())) {
                    return folder;
                }
            }
            newFolder.setName(folderFile.getName());
            newFolder.setPath(folderFile.getAbsolutePath());
            newFolder.setFirstFilePath(path);
            imageFolders.add(newFolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFolder;
    }


    /**
     * 文件夹数量进行排序
     *
     * @param imageFolders
     */
    private void sortFolder(List<LocalMediaFolder> imageFolders) {
        // 文件夹按图片数量排序
        try {
            Collections.sort(imageFolders, new Comparator<LocalMediaFolder>() {
                @Override
                public int compare(LocalMediaFolder lhs, LocalMediaFolder rhs) {
                    if (lhs.getMedia() == null || rhs.getMedia() == null) {
                        return 0;
                    }
                    int lsize = lhs.getNum();
                    int rsize = rhs.getNum();
                    return lsize == rsize ? 0 : (lsize < rsize ? 1 : -1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface LocalMediaLoadListener {
        void loadComplete(List<LocalMediaFolder> folders);
    }

}
