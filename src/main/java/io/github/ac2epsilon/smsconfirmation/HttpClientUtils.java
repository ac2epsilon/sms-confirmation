package io.github.ac2epsilon.smsconfirmation;

/**
 * Created by ac2 on 25.01.17.
 */
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.util.HashMap;
import java.util.Map;

/**
 * HttpClientUtils.java<br><br>
 *
 * A Helper factory for instantiating HttpClient instances<br><br>
 *
 * Created on 5 January 2011, 17:34
 *
 * @author  Paul Cook
 * @version 1.0
 */
class HttpClientUtils {

    private final static Map<String, HttpClientUtils> instances = new HashMap<>();

    private final ThreadSafeClientConnManager threadSafeClientConnManager;

    private final int connectionTimeout;
    private final int soTimeout;

    private HttpClientUtils(int connectionTimeout, int soTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;

        this.threadSafeClientConnManager = new ThreadSafeClientConnManager();
        this.threadSafeClientConnManager.setDefaultMaxPerRoute(200);
        this.threadSafeClientConnManager.setMaxTotal(200);
    }

    /**
     * Return an existing or instantiate a new HttpClient factory instance with explicitly specified connection and read timeout values
     *
     * @param connectionTimeout the timeout value in milliseconds to use when establishing a new http socket
     * @param soTimeout         the timeout value in milliseconds to wait for a http response before closing the socket
     * @return HttpClientUtils an instance of the HttpClient factory primed with the requested timeout values
     */
    public static HttpClientUtils getInstance(int connectionTimeout, int soTimeout) {
        String key = "c-" + connectionTimeout + "-so-" + soTimeout;
        HttpClientUtils instance = instances.get(key);
        if (instance == null) {
            instance = new HttpClientUtils(connectionTimeout, soTimeout);
            instances.put(key, instance);
        }
        return instance;
    }

    /**
     * Instantiate a new HttpClient instance that uses the timeout values associated with this factory instance
     *
     * @return HttpClient a new HttpClient instance
     */
    public HttpClient getNewHttpClient() {
        HttpParams httpClientParams = new BasicHttpParams();
        HttpProtocolParams.setUserAgent(httpClientParams, "Nexmo Java SDK 1.5");
        HttpProtocolParams.setContentCharset(httpClientParams, "UTF-8");
        HttpProtocolParams.setHttpElementCharset(httpClientParams, "UTF-8");
        HttpConnectionParams.setConnectionTimeout(httpClientParams, this.connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpClientParams, this.soTimeout);
        HttpConnectionParams.setStaleCheckingEnabled(httpClientParams, true);
        HttpConnectionParams.setTcpNoDelay(httpClientParams, true);

        return new DefaultHttpClient(this.threadSafeClientConnManager, httpClientParams);
    }

}
