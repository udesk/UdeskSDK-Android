package cn.udesk.rich;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import udesk.core.UdeskConst;
import udesk.core.utils.UdeskUtils;


public class BaseImageLoader implements ImageLoader {
    protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 KB

    protected final Context context;
    protected final int connectTimeout;
    protected final int readTimeout;

    public BaseImageLoader(Context context) {
        this(context, DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
    }

    public BaseImageLoader(Context context, int connectTimeout, int readTimeout) {
        this.context = context.getApplicationContext();
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public Bitmap getBitmap(final String url) throws IOException {

//        if (UdeskUtils.fileIsExitByUrl(context, UdeskConst.FileImg, url)) {
//
//            Bitmap localBitMap = UdeskUtil.compressRatio(BitmapFactory.decodeFile(UdeskUtils.getPathByUrl(context, UdeskConst.FileImg, url)));
//            int bitmapSize = UdeskUtil.getBitmapSize(localBitMap);
//            Log.i("xxxx", "bitmapsize = " + bitmapSize);
//            if (localBitMap != null && bitmapSize > 0) {
//                return localBitMap;
//            }
//        }

        HttpURLConnection conn = createConnection(url);
        conn.connect();
        InputStream imageStream;
        try {
            imageStream = conn.getInputStream();
        } catch (IOException e) {
            readAndCloseStream(conn.getErrorStream());
            throw e;
        }
        if (!shouldBeProcessed(conn)) {
            closeSilently(imageStream);
            throw new IOException("Image request failed with response code " + conn.getResponseCode());
        }
        imageStream = new BufferedInputStream(imageStream, DEFAULT_BUFFER_SIZE);
        if (imageStream != null) {
            final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            Bitmap comPressBitmap = UdeskUtil.compressRatio(UdeskUtil.compressImage(context.getApplicationContext(), url, bitmap));
            if (comPressBitmap != null) {
                return comPressBitmap;
            }
            return bitmap;
        }
        return null;
    }


    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
        HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setDoInput(true);
        conn.addRequestProperty("Connection", "Keep-Alive");

        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) conn;
            httpsURLConnection.setHostnameVerifier(DO_NOT_VERIFY);
            httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        }
        return conn;
    }

    protected boolean shouldBeProcessed(HttpURLConnection conn) throws IOException {
        return conn.getResponseCode() == 200;
    }

    public static void readAndCloseStream(InputStream is) {
        final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
        try {
            while (is.read(bytes, 0, DEFAULT_BUFFER_SIZE) != -1) {
                ;
            }
        } catch (IOException ignored) {
        } finally {
            closeSilently(is);
        }
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }


    private static SSLContext sslContext;

    private static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @SuppressLint("BadHostnameVerifier")
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    static {
        // 设置https为全部信任
        X509TrustManager xtm = new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };


        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
