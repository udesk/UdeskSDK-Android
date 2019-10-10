package cn.udesk.upload;


import android.text.TextUtils;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Progress;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Response;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.MessageInfo;

public class UdeskUploadManager {

    public static void cancleRequest(String key) {
        OkGo.getInstance().cancelTag(key);
    }


    public void uploadFile(String mDomain, String mSecretKey, String sdkToken, String appid, final String fileName,
                           final String filePath, final MessageInfo messageInfo, final UdeskUploadCallBack callBack) {
        try {
            UdeskHttpFacade.getInstance().getUploadService(mDomain, mSecretKey, sdkToken, appid, fileName, new UdeskCallBack() {
                @Override
                public void onSuccess(String message) {
                    try {
                        JSONObject object = new JSONObject(message);
                        String storage = object.optString("storage_policy");
                        if (storage.equals("minio")) {
                            String url = object.optString("host");
                            final String fields = object.optString("fields");
                            minioUpload(url, fields, filePath, messageInfo, callBack);
                        } else if (storage.equals("ali")) {
                            final String accessid = object.optString("accessid");
                            final String bucket = object.optString("bucket");
                            final String host = object.optString("host");
                            final String policy = object.optString("policy");
                            final String signature = object.optString("signature");
                            final String dir = object.optString("dir");
                            final String expire = object.optString("expire");
                            aliUpload(host, filePath, accessid, bucket, policy, signature, dir, expire, fileName, messageInfo, callBack);
                        } else if (storage.equals("qiniu")) {
                            String token = object.optString("token");
                            String bucket = object.optString("bucket");
                            String host = object.optString("host");
                            qiNiuUpload(host, filePath, token, bucket, messageInfo, fileName, callBack);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onFail(String message) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //url+key
    private void minioUpload(final String url, final String fields, final String filePath, final MessageInfo messageInfo, final UdeskUploadCallBack callBack) {

        OkGo.<String>post(url).isMultipart(true).params("file", new File(filePath))
                .tag(messageInfo.getMsgId())
                .execute(new Callback<String>() {
                    String urlkey = "";

                    @Override
                    public void onStart(com.lzy.okgo.request.base.Request<String, ? extends com.lzy.okgo.request.base.Request> request) {

                        HttpParams params = new HttpParams();
                        try {
                            JSONObject jsonObject = new JSONObject(fields);
                            if (jsonObject.has("key")) {
                                urlkey = jsonObject.optString("key");
                                params.put("key", jsonObject.optString("key"));
                            }
                            if (jsonObject.has("policy")) {
                                params.put("policy", jsonObject.optString("policy"));
                            }

                            if (jsonObject.has("x-amz-credential")) {
                                params.put("x-amz-credential", jsonObject.optString("x-amz-credential"));
                            }

                            if (jsonObject.has("x-amz-algorithm")) {
                                params.put("x-amz-algorithm", jsonObject.optString("x-amz-algorithm"));
                            }

                            if (jsonObject.has("x-amz-date")) {
                                params.put("x-amz-date", jsonObject.optString("x-amz-date"));
                            }
                            if (jsonObject.has("x-amz-signature")) {
                                params.put("x-amz-signature", jsonObject.optString("x-amz-signature"));
                            }
                            params.put("file", new File(filePath));
                            request.getParams().put(params);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                        try {
                            if (callBack != null) {
                                String temp = url;
                                if (!temp.endsWith("/")) {
                                    temp += "/";
                                }
                                callBack.onSuccess(messageInfo, temp + urlkey);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCacheSuccess(com.lzy.okgo.model.Response<String> response) {

                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        if (callBack != null) {
                            callBack.onFailure(messageInfo, messageInfo.getMsgId());
                        }
                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void uploadProgress(Progress progress) {

                        try {
                            if (callBack != null) {
                                callBack.progress(messageInfo, messageInfo.getMsgId(), progress.fraction);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void downloadProgress(Progress progress) {

                    }

                    @Override
                    public String convertResponse(Response response) throws Throwable {
                        String reponseStr = "";
                        try {
                            if (callBack != null) {
                                String temp = url;
                                if (!temp.endsWith("/")) {
                                    temp += "/";
                                }
                                callBack.onSuccess(messageInfo, temp + urlkey);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return reponseStr;
                    }
                });
    }


    //url+dir+filename
    private void aliUpload(final String url, final String filePath, final String accessid, final String bucket, final String policy,
                           final String signature, final String dir,
                           final String expire, final String fileName, final MessageInfo messageInfo, final UdeskUploadCallBack callBack) {
        final String alikey = dir + fileName;
        Log.i("xxxxxxxxxxxxxxxx", "aliUpload begin");
        OkGo.<String>post(url).isMultipart(true).params("file", new File(filePath))
                .tag(messageInfo.getMsgId())
                .execute(new Callback<String>() {
                    @Override
                    public void onStart(com.lzy.okgo.request.base.Request<String, ? extends com.lzy.okgo.request.base.Request> request) {

                        HttpParams params = new HttpParams();
                        try {
                            params.put("OSSAccessKeyId", accessid);
                            params.put("bucket", bucket);
                            params.put("policy", policy);
                            params.put("Signature", signature);
                            params.put("key", alikey);
                            params.put("expire", expire);
                            params.put("file", new File(filePath));
                            request.getParams().put(params);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                        try {
                            if (callBack != null) {
                                String temp = url;
                                if (!temp.endsWith("/")) {
                                    temp += "/";
                                }
                                Log.i("xxxxxxxxxxxxxxxx", "url = " + temp + alikey);
                                callBack.onSuccess(messageInfo, temp + alikey);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCacheSuccess(com.lzy.okgo.model.Response<String> response) {

                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        try {
                            if (callBack != null) {
                                callBack.onFailure(messageInfo, messageInfo.getMsgId());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void uploadProgress(Progress progress) {

                        try {
                            if (callBack != null) {
                                Log.i("xxxxxxxxxxxxxxxx", "aliUpload progress = " + progress.fraction);
                                callBack.progress(messageInfo, messageInfo.getMsgId(), progress.fraction);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void downloadProgress(Progress progress) {

                    }

                    @Override
                    public String convertResponse(Response response) throws Throwable {
                        String reponseStr = "";
                        try {
                            if (response.body() != null) {
                                reponseStr = response.body().string();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return reponseStr;
                    }
                });
    }

    private void qiNiuUpload(final String url, final String filePath, final String token, final String bucket,
                             final MessageInfo messageInfo, final String fileName, final UdeskUploadCallBack callBack) {
        OkGo.<String>post(url).isMultipart(true).params("file", new File(filePath))
                .tag(messageInfo.getMsgId())
                .execute(new Callback<String>() {
                    @Override
                    public void onStart(com.lzy.okgo.request.base.Request<String, ? extends com.lzy.okgo.request.base.Request> request) {

                        HttpParams params = new HttpParams();
                        try {
                            params.put("token", token);
                            params.put("key", messageInfo.getMsgId());
                            params.put("bucket", bucket);
                            params.put("file", new File(filePath));
                            request.getParams().put(params);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {
//                        body:{"hash":"lq9QDf83F15q6fStoM5XL07oBhG5","key":"lq9QDf83F15q6fStoM5XL07oBhG5"}
                        String string = response.body();
                        if (!TextUtils.isEmpty(string)) {
                            try {
                                Log.i("UdeskSdk", "onsuccess string=" + string);
                                JSONObject jsonObject = new JSONObject(string);
                                String key = jsonObject.optString("key");
                                if (callBack != null) {
                                    StringBuilder builder = new StringBuilder();
                                    if (bucket.equals("udesk")) {
                                        builder.append("https://dn-udeskpvt.qbox.me/").append(key).append("?attname=");
                                    } else if (bucket.equals("udeskpub")) {
                                        builder.append("https://dn-udeskpub.qbox.me/").append(key);
                                    } else if (bucket.equals("udeskim")) {
                                        builder.append("https://dn-udeskim.qbox.me/").append(key);
                                    }
                                    callBack.onSuccess(messageInfo, builder.toString());

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCacheSuccess(com.lzy.okgo.model.Response<String> response) {

                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {

                        try {
                            if (callBack != null) {
                                callBack.onFailure(messageInfo, messageInfo.getMsgId());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void uploadProgress(Progress progress) {


                        try {
                            if (callBack != null) {
                                callBack.progress(messageInfo, messageInfo.getMsgId(), progress.fraction);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void downloadProgress(Progress progress) {

                    }

                    @Override
                    public String convertResponse(Response response) throws Throwable {

                        String reponseStr = "";
                        try {
                            if (response.body() != null) {
                                reponseStr = response.body().string();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return reponseStr;
                    }
                });
    }


}
