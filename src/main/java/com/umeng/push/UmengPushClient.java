package com.umeng.push;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UmengPushClient {

  private static final Logger logger = LoggerFactory.getLogger(UmengPushClient.class);

  // The host
  protected static final String host = "http://msg.umeng.com";
  // The post path
  protected static final String postPath = "/api/send";
  // The user agent
  protected final String USER_AGENT = "Mozilla/5.0";

  private boolean production;

  public UmengPushClient(boolean production) {
    this.production = production;
  }

  private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

  public String send(UmengNotification msg) throws Exception {
    msg.setProductionMode(production);
    String timestamp = Integer.toString((int) (System.currentTimeMillis() / 1000));
    msg.setPredefinedKeyValue("timestamp", timestamp);
    String url = host + postPath;
    String postBody = msg.getPostBody();
    logger.info("Umeng client request. [body='{}']", postBody);
    String sign = DigestUtils.md5Hex(("POST" + url + postBody + msg.getAppMasterSecret()).getBytes("utf8"));
    url = url + "?sign=" + sign;
    HttpPost post = new HttpPost(url);
    post.setHeader("User-Agent", USER_AGENT);
    StringEntity se = new StringEntity(postBody, "UTF-8");
    post.setEntity(se);

    try (CloseableHttpResponse response = httpClient.execute(post)) {
      int status = response.getStatusLine().getStatusCode();
      String result = EntityUtils.toString(response.getEntity());
      logger.info("send umeng push. [code={}, content='{}']", status, result);
      return result;
    }
  }

  public void shutdown() {
    try {
      if (httpClient != null) {
        httpClient.close();
      }
    } catch (IOException e) {
      logger.warn("close http client failed", e);
    }
  }
}
