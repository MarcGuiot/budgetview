package com.budgetview.server.license.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractHttpServlet extends HttpServlet {

  protected abstract void action(HttpServletRequest req, HttpServletResponse resp) throws IOException, Exception;

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      req.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");
      action(req, resp);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
