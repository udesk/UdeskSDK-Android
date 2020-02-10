package cn.udesk.aac.livedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cn.udesk.JsonUtils;
import cn.udesk.UdeskUtil;
import cn.udesk.aac.MergeMode;
import cn.udesk.aac.MergeModeManager;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.rich.LoaderTask;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.http.UdeskHttpCallBack;
import udesk.core.model.MessageInfo;
import udesk.core.model.UploadBean;
import udesk.core.utils.UdeskUtils;

public class FileLiveData<M> extends MutableLiveData<MergeMode> {

    private String domain = "";
    private String secretKey = "";
    private String sdktoken = "";
    private String appid = "";
    OkHttpClient okHttpClient;
    private UdeskChatActivity.MyHandler  myHandler;

    private Map<String, Call> concurrentHashMap = new ConcurrentHashMap();


    public void setBaseValue(String domain, String secretKey, String sdktoken,
                             String appid) {
        this.domain = domain;
        this.secretKey = secretKey;
        this.sdktoken = sdktoken;
        this.appid = appid;
    }
    public void setHandler(UdeskChatActivity.MyHandler handler){
        myHandler=handler;
    }
    public void cancleUploadFile(MessageInfo message) {
        try {
            Call call = concurrentHashMap.get(message.getMsgId());
            if (call != null ){
                call.cancel();
            }
            concurrentHashMap.remove(message.getMsgId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upLoadFile(final MessageInfo messageInfo) {
        final String filePath = messageInfo.getLocalPath();
        File file = new File(filePath);
        final String fileName = file.getName();
        try {
            UdeskHttpFacade.getInstance().getUploadService(domain, secretKey, sdktoken, appid, fileName, new UdeskCallBack() {
                @Override
                public void onSuccess(String message) {
                    try {
                        UploadBean uploadBean = JsonUtils.parseUploadBean(message);
                        if (uploadBean!=null&&uploadBean.getUpload_token()!=null){
                            String referer = uploadBean.getReferer();
                            UploadBean.UploadTokenBean upload_token = uploadBean.getUpload_token();
                            String storage = upload_token.getStorage_policy();
                            if (storage.equals("minio")) {
                                String url = upload_token.getHost();
                                String fields = upload_token.getFields();
                                minioUpload(url, fields, filePath, messageInfo,referer);
                            } else if (storage.equals("ali")) {
                                final String accessid = upload_token.getAccessid();
                                final String bucket = upload_token.getBucket();
                                final String host = upload_token.getHost();
                                final String policy = upload_token.getPolicy();
                                final String signature = upload_token.getSignature();
                                final String dir = upload_token.getDir();
                                final String expire = UdeskUtils.objectToString( upload_token.getExpire());
                                aliUpload(host, filePath, accessid, bucket, policy, signature, dir, expire, fileName, messageInfo,referer);
                            } else if (storage.equals("qiniu")) {
                                String token = upload_token.getToken();
                                String bucket = upload_token.getBucket();
                                String host = upload_token.getHost();
                                String download_host = upload_token.getDownload_host();
                                if (!download_host.endsWith("/")){
                                    download_host += "/";
                                }
                                qiNiuUpload(host, filePath, token, bucket, messageInfo, fileName,referer,download_host);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(String message) {
                    updateFailure(messageInfo.getMsgId());
                    UdeskDBManager.getInstance().updateMsgSendFlagDB(messageInfo.getMsgId(),
                            UdeskConst.SendFlag.RESULT_FAIL);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void minioUpload(final String url, final String fields, final String filePath, final MessageInfo messageInfo,String referer){
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder().addInterceptor
                    (new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build();
        }
        try {
            String urlkey = "";
            File file = new File(filePath);
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            JSONObject jsonObject = new JSONObject(fields);
            if (jsonObject.has("key")) {
                urlkey = jsonObject.optString("key");
                builder.addFormDataPart("key", jsonObject.optString("key"));
            }
            if (jsonObject.has("policy")) {
                builder.addFormDataPart("policy", jsonObject.optString("policy"));
            }

            if (jsonObject.has("x-amz-credential")) {
                builder.addFormDataPart("x-amz-credential", jsonObject.optString("x-amz-credential"));
            }

            if (jsonObject.has("x-amz-algorithm")) {
                builder.addFormDataPart("x-amz-algorithm", jsonObject.optString("x-amz-algorithm"));
            }

            if (jsonObject.has("x-amz-date")) {
                builder.addFormDataPart("x-amz-date", jsonObject.optString("x-amz-date"));
            }
            if (jsonObject.has("x-amz-signature")) {
                builder.addFormDataPart("x-amz-signature", jsonObject.optString("x-amz-signature"));
            }
            addCustomRequestBody(builder,file, messageInfo, file.getName());
            Call call = getCall(url, messageInfo, builder,referer);
            final String finalUrlkey = urlkey;
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    uploadFailure(messageInfo);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    concurrentHashMap.remove(messageInfo.getMsgId());
                    String string = response.body().string();
                    Log.i("xxxxx", "response = " + string);
                    String temp = url;
                    if (!temp.endsWith("/")) {
                        temp += "/";
                    }
                    temp = temp + finalUrlkey;
                    UdeskDBManager.getInstance().updateMsgContentDB(messageInfo.getMsgId(), temp);
                    messageInfo.setMsgContent(temp);
                    addMessage(messageInfo);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void aliUpload(final String url, final String filePath, final String accessid, final String bucket, final String policy,
                           final String signature, final String dir,
                           final String expire, final String fileName, final MessageInfo messageInfo,String referer) {
        try{
            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient.Builder().addInterceptor
                        (new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build();
            }
            File file = new File(filePath);
            final String alikey = dir + fileName;
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("key", dir+URLEncoder.encode(fileName,"UTF-8"));
            builder.addFormDataPart("OSSAccessKeyId", accessid);
            builder.addFormDataPart("bucket", bucket);
            builder.addFormDataPart("policy", policy);
            builder.addFormDataPart("Signature", signature);
            builder.addFormDataPart("expire", expire);
            addCustomRequestBody(builder,file, messageInfo, fileName);
            Call call = getCall(url, messageInfo, builder,referer);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    uploadFailure(messageInfo);
                }

                @Override
                public void onResponse(Call call, Response response){

                    try {
                        if (response.code() == 204
                                || response.code() == 200
                                || response.code() == 201
                                || response.code() == 202
                                || response.code() == 205){
                            concurrentHashMap.remove(messageInfo.getMsgId());
                            String temp = url;
                            if (!temp.endsWith("/")) {
                                temp += "/";
                            }
                            temp = temp + alikey;
                            UdeskDBManager.getInstance().updateMsgContentDB(messageInfo.getMsgId(), temp);
                            messageInfo.setMsgContent(temp);
                            addMessage(messageInfo);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    uploadFailure(messageInfo);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void qiNiuUpload(final String url, final String filePath, final String token, final String bucket,
                             final MessageInfo messageInfo, final String fileName, String referer, final String download_host) {
        try{
            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient();
            }
            File file = new File(filePath);
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("key", messageInfo.getMsgId() + "_" + URLEncoder.encode(fileName,"UTF-8"));
            builder.addFormDataPart("token", token);
            builder.addFormDataPart("bucket", bucket);
            addCustomRequestBody(builder,file, messageInfo, fileName);
            Call call = getCall(url, messageInfo, builder,referer);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    uploadFailure(messageInfo);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    concurrentHashMap.remove(messageInfo.getMsgId());
                    String string = response.body().string();
                    if (!TextUtils.isEmpty(string)) {
                        try {
                            Log.i("UdeskSdk", "onsuccess strng=" + string);
                            JSONObject jsonObject = new JSONObject(string);
                            String key = jsonObject.optString("key");
                            StringBuilder builder = new StringBuilder();
                            if (bucket.equals("udesk")) {
                                builder.append(download_host).append(key).append("?attname=");
                            } else {
                                builder.append(download_host).append(key);
                            }
                            UdeskDBManager.getInstance().updateMsgContentDB(messageInfo.getMsgId(), builder.toString());
                            messageInfo.setMsgContent(builder.toString());
                            addMessage(messageInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void uploadFailure(MessageInfo messageInfo) {
        try {
            concurrentHashMap.remove(messageInfo.getMsgId());
            updateFailure(messageInfo.getMsgId());
            UdeskDBManager.getInstance().updateMsgSendFlag(messageInfo.getMsgId(),
                    UdeskConst.SendFlag.RESULT_FAIL);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @NonNull
    private Call getCall(String url, MessageInfo messageInfo, MultipartBody.Builder builder,String referer) {
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("referer",referer)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        concurrentHashMap.put(messageInfo.getMsgId(), call);
        return call;
    }

    @NonNull
    private  void addCustomRequestBody (MultipartBody.Builder builder,  File file ,final MessageInfo messageInfo, String fileName) {
        try {
            builder.addFormDataPart("file", URLEncoder.encode(fileName,"UTF-8"), createCustomRequestBody(file, new ProgressListener() {
                int lastProgress = 0;
                @Override
                public void onProgress(long totalBytes, long remainingBytes, boolean done) {
                    try {
                        if (done) {
                            return;
                        }
                        float percent = (totalBytes - remainingBytes) * 100 / totalBytes;
                        int progress = Float.valueOf(percent).intValue();
                        if (progress != lastProgress ){
                            lastProgress=progress;
                            messageInfo.setPrecent(progress);
                            if (myHandler!=null){
                                Message message = myHandler.obtainMessage(UdeskConst.LiveDataType.UpLoadFileLiveData_progress);
                                message.obj = messageInfo;
                                myHandler.sendMessage(message);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static RequestBody createCustomRequestBody(final File file, final ProgressListener listener) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source;
                try {
                    source = Okio.source(file);
                    //sink.writeAll(source);
                    Buffer buf = new Buffer();
                    Long remaining = contentLength();
                    for (long readCount; (readCount = source.read(buf, 2048)) != -1; ) {
                        sink.write(buf, readCount);
                        listener.onProgress(contentLength(), remaining -= readCount, remaining == 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    interface ProgressListener {
        void onProgress(long totalBytes, long remainingBytes, boolean done);
    }


    public void fileProgress(MessageInfo info) {
        MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.UpLoadFileLiveData_progress, info,UUID.randomUUID().toString());
        MergeModeManager.getmInstance().putMergeMode(mergeMode,FileLiveData.this);
    }


    //下载语言
    public void downAudio(final MessageInfo info, Context context) {
        try {
            final File file = new File(UdeskUtils.getDirectoryPath(context, UdeskConst.FileAudio),
                    UdeskUtils.getFileName(info.getMsgContent(), UdeskConst.FileAudio));

            UdeskHttpFacade.getInstance().downloadFile(file.getAbsolutePath(), info.getMsgContent(), UdeskConst.REFERER_VALUE,new UdeskHttpCallBack() {

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载视频
    public void downVideo(final MessageInfo info, Context context) {
        try {
            final File file = new File(UdeskUtils.getDirectoryPath(context, UdeskConst.FileVideo),
                    UdeskUtils.getFileName(info.getMsgContent(), UdeskConst.FileVideo));

            UdeskHttpFacade.getInstance().downloadFile(file.getAbsolutePath(), info.getMsgContent(),UdeskConst.REFERER_VALUE, new UdeskHttpCallBack() {

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载文件
    public void downFile(final MessageInfo info, Context context) {
        try {
            final File file = new File(UdeskUtils.getDirectoryPath(context, UdeskConst.File_File),
                    UdeskUtils.getFileName(info.getMsgContent(), UdeskConst.File_File));
            UdeskHttpFacade.getInstance().downloadFile(file.getAbsolutePath(), info.getMsgContent(),UdeskConst.REFERER_VALUE, new UdeskHttpCallBack() {


                @Override
                public void onSuccess(byte[] t) {
                    try {
                        UdeskDBManager.getInstance().updateMsgLoaclUrl(info.getMsgId(), file.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int errorNo, String strMsg) {
                    MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.DownFileError, info.getMsgId(),UUID.randomUUID().toString());
                    MergeModeManager.getmInstance().putMergeMode(mergeMode,FileLiveData.this);

                }

                @Override
                public void onLoading(long count, long current) {
                    double percent = current / (double) count;
                    int progress = Double.valueOf(percent * 100).intValue();
                    info.setPrecent(progress);
                    if (myHandler!=null){
                        Message message = myHandler.obtainMessage(UdeskConst.LiveDataType.UpLoadFileLiveData_progress);
                        message.obj = info;
                        myHandler.sendMessage(message);
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void getBitmap(final Context context, final MessageInfo info) {
        LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = UdeskUtil.getVideoThumbnail(info.getMsgContent());
                    if (bitmap != null) {
                        UdeskUtils.saveBitmap(context, info.getMsgContent(), bitmap);
                        MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.ChangeVideoThumbnail, info.getMsgId(),UUID.randomUUID().toString());
                        MergeModeManager.getmInstance().putMergeMode(mergeMode,FileLiveData.this);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addMessage(MessageInfo msg) {
        UdeskUtils.printStackTrace();
        MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.AddMessage, msg,UUID.randomUUID().toString());
        MergeModeManager.getmInstance().putMergeMode(mergeMode,FileLiveData.this);

    }

    private void updateFailure(String msgId) {
        UdeskUtils.printStackTrace();
        MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.Send_Message_Failure, msgId,UUID.randomUUID().toString());
        MergeModeManager.getmInstance().putMergeMode(mergeMode,FileLiveData.this);

    }

    @Override
    protected void onActive() {
        super.onActive();
        if (UdeskConst.isDebug) {
            Log.i("aac", " FileLiveData onActive");
        }
    }

    @Override
    protected void onInactive() {
        if (UdeskConst.isDebug) {
            Log.i("aac", " FileLiveData onInactive");
        }
        super.onInactive();
    }
}

