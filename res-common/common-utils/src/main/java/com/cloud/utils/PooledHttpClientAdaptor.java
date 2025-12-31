package com.cloud.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 创建HttpClient 工具
 */
public class PooledHttpClientAdaptor {

    private static final int DEFAULT_POOL_MAX_TOTAL = 200;

    private static final int DEFAULT_POOL_MAX_PER_TOTAL = 200;

    private static final int DEFAULT_CONNECT_TIMEOUT = 200;

    private static final int DEFAULT_SOCKET_TIMEOUT = 2000;

    private static final int DEFAULT_CONNECT_REQUEST_TIMEOUT = 2000;

    private PoolingHttpClientConnectionManager phttpManager = null;

    private CloseableHttpClient httpClient = null;


    public PooledHttpClientAdaptor pooledHttpClientAdaptor() {
        Registry<ConnectionSocketFactory> build = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", PlainConnectionSocketFactory.getSocketFactory()).build();

        phttpManager = new PoolingHttpClientConnectionManager(build);
        phttpManager.setMaxTotal(DEFAULT_POOL_MAX_TOTAL);
        phttpManager.setDefaultMaxPerRoute(DEFAULT_POOL_MAX_PER_TOTAL);

        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(DEFAULT_CONNECT_REQUEST_TIMEOUT)
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();

        httpClient = HttpClients.custom().setConnectionManager(phttpManager).setDefaultRequestConfig(config).build();
        return this;
    }

    public PooledHttpClientAdaptor defaultHttpClientAdaptor() {
        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(DEFAULT_CONNECT_REQUEST_TIMEOUT)
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();

        httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
        return this;
    }




    public String doGet(String url) {
        return this.doGet(url, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    }

    public String doGet(String url, Map<String, String> headers, Map<String, Object> params) {
        String apiUrl = getUrlWithParams(url, params);
        HttpGet httpGet = new HttpGet(apiUrl);
        if (null != headers && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            if (Objects.nonNull(response) && Objects.nonNull(response.getStatusLine())) {
                if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                    HttpEntity entity = response.getEntity();
                    if (Objects.nonNull(entity)) {
                        return EntityUtils.toString(entity, "UTF-8");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            if (Objects.nonNull(response)) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     *  POST 请求
      * @param url
     * @param headers
     * @param jsonBody
     * @return
     */
    public String doPost(String url, Map<String, String> headers, String jsonBody) {
        HttpPost httpPost = new HttpPost(url);
//      设置请求headers
        if (!CollectionUtils.isEmpty(headers) && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }
//      设置body内容
        if(StringUtils.isNotBlank(jsonBody)){
            httpPost.setEntity(new StringEntity(jsonBody,"UTF-8"));
        }
        CloseableHttpResponse response = null;
        String resultResponse = null ;
        try {
            response = httpClient.execute(httpPost);
            if (Objects.nonNull(response) && Objects.nonNull(response.getStatusLine())) {
                if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                    HttpEntity entity = response.getEntity();
                    if (Objects.nonNull(entity)) {
                        resultResponse = EntityUtils.toString(entity, "UTF-8");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (Objects.nonNull(response)) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultResponse;
    }





    private String getUrlWithParams(String url, Map<String, Object> params) {
        boolean first = true;
        StringBuilder stringBuilder = new StringBuilder(url);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            char ch = '&';
            if (first == true) {
                ch = '?';
                first = false;
            }
            String value = entry.getValue().toString();
            try {
                String encode = URLEncoder.encode(value, "UTF-8");
                stringBuilder.append(ch).append(key).append("=").append(encode);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }










    public static void main(String[] args) {






    }


}

