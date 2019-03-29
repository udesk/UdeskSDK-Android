package cn.udesk;

import android.net.Uri;

import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.HttpUrlConnectionNetworkFetcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ElnImageDownloaderFetcher extends HttpUrlConnectionNetworkFetcher {
    private static final int NUM_NETWORK_THREADS = 3;
    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5000;
    public static final int DEFAULT_HTTP_READ_TIMEOUT = 20000;
    private final ExecutorService mExecutorService;
    private final String referer;

    public ElnImageDownloaderFetcher(String referer) {
        mExecutorService = Executors.newFixedThreadPool(NUM_NETWORK_THREADS);
        this.referer = referer;
    }

    @Override
    public void fetch(final FetchState fetchState, final Callback callback) {
        final Future<?> future = mExecutorService.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection connection = null;
                        Uri uri = fetchState.getUri();
                        String scheme = uri.getScheme();
                        String uriString = fetchState.getUri().toString();
                        while (true) {
                            String nextUriString;
                            String nextScheme;
                            InputStream is;
                            try {
                                connection = createConnection(uriString);
                                nextUriString = connection.getHeaderField("Location");
                                nextScheme = (nextUriString == null) ? null : Uri.parse(nextUriString).getScheme();
                                if (nextUriString == null || nextScheme.equals(scheme)) {
                                    is = connection.getInputStream();
                                    callback.onResponse(is, -1);
                                    break;
                                }
                                uriString = nextUriString;
                                scheme = nextScheme;
                            } catch (Exception e) {
                                callback.onFailure(e);
                                break;
                            } finally {
                                if (connection != null) {
                                    connection.disconnect();
                                }
                            }
                        }
                    }
                });
        fetchState.getContext().addCallbacks(new BaseProducerContextCallbacks() {
            @Override
            public void onCancellationRequested() {
                if (future.cancel(false)) {
                    callback.onCancellation();
                }
            }
        });
    }

    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~\'%");
        HttpURLConnection conn = (HttpURLConnection) (new URL(encodedUrl)).openConnection();
        conn.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_HTTP_READ_TIMEOUT);
        conn.setRequestProperty("referer", referer);
        return conn;
    }

}
