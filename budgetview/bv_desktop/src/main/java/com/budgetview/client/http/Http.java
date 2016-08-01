package com.budgetview.client.http;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.globsframework.gui.splits.utils.Disposable;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.*;

public class Http {

  public static class Post implements Disposable {

    private Map<String, String> headers;
    private Map<String, String> parameters;
    private HttpEntity entity;
    private String url;
    private HttpPost postMethod;
    private boolean changed = false;

    private Post(String url) {
      this.url = url;
    }

    public Post setUtf8() {
      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      nvps.add(new BasicNameValuePair("IDToken1", "username"));
      nvps.add(new BasicNameValuePair("IDToken2", "password"));
      setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
      return this;
    }

    public Post setHeader(String name, String value) {
      if (headers == null) {
        headers = new HashMap<String, String>();
      }
      headers.put(name, value);
      changed = true;
      return this;
    }

    public Post setUtf8(String content) {
      return setEntity(new StringEntity(content, "UTF-8"));
    }

    public Post setEntity(HttpEntity entity) {
      this.entity = entity;
      changed = true;
      return this;
    }

    public HttpResponse execute() throws IOException {
      HttpClient httpClient = getNewHttpClient();
      createPostMethod();
      return httpClient.execute(postMethod);
    }

    public HttpResponse executeWithRetry() throws IOException {
      createPostMethod();
      return getNewHttpClient().execute(postMethod);
    }

    private void createPostMethod() {
      if (changed && postMethod != null) {
        postMethod.releaseConnection();
        postMethod = null;
      }
      if (postMethod == null) {
        postMethod = new HttpPost(url);
        if (headers != null) {
          for (Map.Entry<String, String> entry : headers.entrySet()) {
            postMethod.setHeader(entry.getKey(), entry.getValue());
          }
        }
        if (parameters != null) {
          for (Map.Entry<String, String> entry : parameters.entrySet()) {
            postMethod.getParams().setParameter(entry.getKey(), entry.getValue());
          }
        }
      }
    }

    public void dispose() {
      if (postMethod != null) {
        postMethod.releaseConnection();
        postMethod = null;
      }
    }
  }

  public static Post utf8Post(String url) {
    return post(url).setUtf8();
  }

  public static Post post(String url) {
    return new Post(url);
  }

  private static HttpClient getNewHttpClient() {
    try {
      SchemeRegistry schemeRegistry = new SchemeRegistry();
      schemeRegistry.register(new Scheme("https", 443,
                                         new SSLSocketFactory(new TrustStrategy() {
                                           public boolean isTrusted(X509Certificate[] chain, String authType) {
                                             return true;
                                           }
                                         },
                                                              new AbstractVerifier() {
                                                                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                                                                }
                                                              }
                                         )));
      schemeRegistry.register(new Scheme("http", 5000, new PlainSocketFactory()));
      ClientConnectionManager connectionManager = new BasicClientConnectionManager(schemeRegistry);
      HttpClient httpClient = new DefaultHttpClient(connectionManager);
      httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
      return httpClient;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
