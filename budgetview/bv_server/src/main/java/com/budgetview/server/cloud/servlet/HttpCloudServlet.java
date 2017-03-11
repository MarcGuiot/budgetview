package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudConstants;
import org.globsframework.utils.directory.Directory;
import org.json.JSONWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class HttpCloudServlet extends HttpServlet {

  protected final Directory directory;

  public HttpCloudServlet(Directory directory) {
    this.directory = directory;
  }

  protected void setSubscriptionError(SubscriptionCheckFailed e, JSONWriter writer) {
    writer.object();
    writer.key(CloudConstants.STATUS).value("no_subscription");
    writer.key(CloudConstants.SUBSCRIPTION_STATUS).value(e.getStatus().getName());
    writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
    writer.endObject();
  }

  protected void setSubscriptionError(HttpServletResponse response, SubscriptionCheckFailed e, JSONWriter writer) throws IOException {
    writer.key(CloudConstants.STATUS).value("no_subscription");
    writer.key(CloudConstants.SUBSCRIPTION_STATUS).value(e.getStatus().getName());
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
