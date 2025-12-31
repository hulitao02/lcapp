package tool;

import com.alibaba.fastjson.JSON;
import com.cloud.model.bean.vo.es.EsKnowlegdeJsonBean;
import com.cloud.utils.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
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


    public PooledHttpClientAdaptor() {
        Registry<ConnectionSocketFactory> build = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", PlainConnectionSocketFactory.getSocketFactory()).build();

        phttpManager = new PoolingHttpClientConnectionManager(build);
        phttpManager.setMaxTotal(DEFAULT_POOL_MAX_TOTAL);
        phttpManager.setDefaultMaxPerRoute(DEFAULT_POOL_MAX_PER_TOTAL);

        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(DEFAULT_CONNECT_REQUEST_TIMEOUT)
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();

        httpClient = HttpClients.custom().setConnectionManager(phttpManager).setDefaultRequestConfig(config).build();

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

        PooledHttpClientAdaptor adaptor = new PooledHttpClientAdaptor();
//        String url = "http://192.168.10.172:3000/jeecg-boot/enc/encSenses/queryDataPageList?" +
//                "_t=1634285480&field=id,,,bkClassLabel,sensesName,sensesVersion,statusLabel,photo,sensesClicks,sensesLabelLabel,action" +
//                "sensesName='wwwww'&pageNo=1&pageSize=10";
//      知识
        String sensesId = "1451116442723786754";
//      知识点
        String classId = "1438799685271138309";
        String knowledge_Url = "http://192.168.10.172:8888/jeecg-boot/enc/encSenses/queryById?id=" + sensesId;
        String point_url = "http://192.168.10.172:8888/jeecg-boot/enc/encSenses/queryDataPageList?classId=" + classId;
        String queryContent = "http://192.168.10.172:8888/jeecg-boot/enc/encSenses/searchSenses?searchContent=" + "大炮";
        Map<String,String> hearder = new HashMap<>();
        hearder.put("X-Access-Token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MzU3NDgwMjMsInVzZXJuYW1lIjoiYWRtaW4ifQ.V7NLZIrXCs1aNbaz3VPbUkgFKylpm21363oPqozHTsI");
        String resultJSON = adaptor.doGet(point_url,hearder,Collections.EMPTY_MAP);
        if (StringUtils.isNotEmpty(resultJSON)) {
            EsKnowlegdeJsonBean esKnowlegdeJsonBean = JSON.parseObject(resultJSON, EsKnowlegdeJsonBean.class);
            System.out.println(JSON.toJSON(esKnowlegdeJsonBean));
        } else {
            System.out.println("查询结果为空");
        }
    }


}

