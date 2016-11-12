package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.utils.CloudSubscriptionException;
import com.budgetview.shared.cloud.CloudConstants;
import org.json.JSONWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class HttpCloudServlet extends HttpServlet {
  protected void setOk(HttpServletResponse response) throws IOException {
    JSONWriter writer = new JSONWriter(response.getWriter());
    writer.object();
    setOk(response, writer);
    writer.endObject();
  }

  protected void setOk(HttpServletResponse response, JSONWriter writer) {
    writer.key(CloudConstants.STATUS).value("ok");
    response.setStatus(HttpServletResponse.SC_OK);
  }

  protected void setSubscriptionError(HttpServletResponse response, CloudSubscriptionException e) throws IOException {
    JSONWriter writer = new JSONWriter(response.getWriter());
    writer.object();
    writer.key(CloudConstants.STATUS).value("no_subscription");
    writer.key(CloudConstants.SUBSCRIPTION_STATUS).value(e.getStatus().getName());
    writer.endObject();
    response.setStatus(HttpServletResponse.SC_OK);
  }

  protected void setSubscriptionError(HttpServletResponse response, CloudSubscriptionException e, JSONWriter writer) throws IOException {
    writer.key(CloudConstants.STATUS).value("no_subscription");
    writer.key(CloudConstants.SUBSCRIPTION_STATUS).value(e.getStatus().getName());
    response.setStatus(HttpServletResponse.SC_OK);
  }

  protected void setInternalError(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  protected void setUnauthorized(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  protected void setBadRequest(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }
}
