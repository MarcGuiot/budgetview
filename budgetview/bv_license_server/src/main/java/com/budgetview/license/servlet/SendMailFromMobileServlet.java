package com.budgetview.license.servlet;

import com.budgetview.license.mail.Mailer;
import com.budgetview.shared.utils.MobileConstants;
import org.apache.log4j.Logger;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

public class SendMailFromMobileServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("sendMailFromMobile");
  private Mailer mailer;

  public SendMailFromMobileServlet(Directory directory) {
    mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      req.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");
      String mailTo = req.getParameter(MobileConstants.MAIL);
      logger.info("request for reminder " + mailTo);
      if (Strings.isNullOrEmpty(mailTo)) {
        logger.info("sendMail: missing mail address " + (mailTo == null ? "<no email>" : mailTo));
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      String lang = req.getParameter(MobileConstants.HEADER_LANG);
      if (Strings.isNullOrEmpty(lang)){
        lang = "fr";
      }
      mailTo = URLDecoder.decode(mailTo.trim(), "UTF-8");
      mailer.sendFromMobileToUseBV(mailTo, lang);
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception e) {
      logger.error("sendMail failed: ", e);
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }
}
