package cn.udesk.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;


import java.util.ArrayList;
import java.util.List;


public class XPermissionUtils {

    private static int mRequestCode = -1;
    private static OnPermissionListener mOnPermissionListener;

    public interface OnPermissionListener {

        void onPermissionGranted();

        void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissionsAgain(Activity activity, String[] permissions,
                                               int requestCode) {
        try {
            activity.requestPermissions(permissions, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissions(Activity activity, int requestCode,
                                          String[] permissions, OnPermissionListener listener) {
        try {
            mRequestCode = requestCode;
            mOnPermissionListener = listener;
            String[] deniedPermissions = getDeniedPermissions(activity, permissions);
            if (deniedPermissions.length > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissionsAgain(activity, permissions, requestCode);
            } else {
                if (mOnPermissionListener != null) mOnPermissionListener.onPermissionGranted();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求权限结果，对应Activity中onRequestPermissionsResult()方法。
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void onRequestPermissionsResult(Activity activity, int requestCode,
                                                  String[] permissions, int[] grantResults) {
        try {
            if (mRequestCode != -1 && requestCode == mRequestCode) {
                if (mOnPermissionListener != null) {
                    String[] deniedPermissions = getDeniedPermissions(activity, permissions);
                    if (deniedPermissions.length > 0) {
                        boolean alwaysDenied = hasAlwaysDeniedPermission(activity, permissions);
                        mOnPermissionListener.onPermissionDenied(deniedPermissions, alwaysDenied);
                    } else {
                        mOnPermissionListener.onPermissionGranted();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取请求权限中需要授权的权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static String[] getDeniedPermissions(Activity activity, String[] permissions) {
        List<String> deniedPermissions = new ArrayList();
        try {
            for (String permission : permissions) {
                if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    deniedPermissions.add(permission);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deniedPermissions.toArray(new String[deniedPermissions.size()]);
    }

    /**
     * 是否彻底拒绝了某项权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean hasAlwaysDeniedPermission(Activity activity, String... deniedPermissions) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;
            boolean rationale;
            for (String permission : deniedPermissions) {
                rationale = activity.shouldShowRequestPermissionRationale(permission);
                if (!rationale) return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}