package com.budgetview.license.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class LicenseClient {

  private static String URL = "https://register.mybudgetview.fr:8443/getMobileData?mail=regis.medina%40gmail.com&info=2d7aa19702c8bfaf2461fe258b82195c77761b51";

  public static void main(String[] args) throws Exception {
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet method = new HttpGet(URL);
    HttpResponse response = client.execute(method);
    int statusCode = response.getStatusLine().getStatusCode();
    System.out.println("LicenseClient.main: " + statusCode);
    method.releaseConnection();
  }
}
