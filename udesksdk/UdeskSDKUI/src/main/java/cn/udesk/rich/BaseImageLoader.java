package cn.udesk.rich;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import cn.udesk.UdeskUtil;
import udesk.core.UdeskConst;
import udesk.core.utils.UdeskUtils;

import static cn.udesk.emotion.LQREmotionKit.getContext;


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
    public Bitmap getBitmap(final String url,int width) throws IOException {

        HttpURLConnection conn = createConnection(url);
        conn.connect();
        InputStream imageStream = null;
        OutputStream fileOutputStream = null;
        try {
            imageStream = conn.getInputStream();
            imageStream = new BufferedInputStream(imageStream, DEFAULT_BUFFER_SIZE);
            fileOutputStream=new FileOutputStream(UdeskUtil.getPathByUrl(getContext(), UdeskConst.FileImg, url));
            fileOutputStream = new BufferedOutputStream(fileOutputStream,DEFAULT_BUFFER_SIZE);
            byte[] data = new byte[DEFAULT_BUFFER_SIZE];
            int len = 0;
            while ((len = imageStream.read(data))!= -1 ){
                fileOutputStream.write(data,0,len);
            }
            fileOutputStream.flush();
        } catch (IOException e) {
            readAndCloseStream(conn.getErrorStream());
            throw e;
        }finally {
            closeSilently(fileOutputStream);
            closeSilently(imageStream);
            if (!shouldBeProcessed(conn)) {
                throw new IOException("Image request failed with response code " + conn.getResponseCode());
            }
        }
        if (imageStream != null) {
            Bitmap comPressBitmap = UdeskUtil.compressRatio(context,url,width);
            if (comPressBitmap != null) {
                return comPressBitmap;
            }
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
