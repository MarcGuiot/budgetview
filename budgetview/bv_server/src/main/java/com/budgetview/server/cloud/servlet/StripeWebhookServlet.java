package com.budgetview.server.cloud.servlet;

import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.net.APIResource;
import org.apache.log4j.Logger;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class StripeWebhookServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("StripeWebhookServlet");

  public StripeWebhookServlet(Directory directory) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // Set your secret key: remember to change this to your live secret key in production
    // See your keys here: https://dashboard.stripe.com/account/apikeys

    InputStream inputStream = request.getInputStream();
    String json = Files.loadStreamToString(inputStream, "UTF-8");
    Event event = APIResource.GSON.fromJson(json, Event.class);

    // invoice.payment_succeeded


    // invoice.payment_failed



    if (event.getType().equals("")) {
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }
}
