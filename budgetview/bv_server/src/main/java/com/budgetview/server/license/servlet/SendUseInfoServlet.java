package com.budgetview.server.license.servlet;

import com.budgetview.shared.license.LicenseConstants;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SendUseInfoServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("sendUseInfo");

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
    String useInfo = req.getHeader(LicenseConstants.HEADER_USE_INFO);
    logger.info("use info = " + useInfo);
    resp.setStatus(HttpServletResponse.SC_OK);
  }
}
