package com.budgetview.bank.connectors.webcomponents.utils;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;

public interface HttpConnectionProvider {
  HttpWebConnection getHttpConnection(WebClient client);
}
