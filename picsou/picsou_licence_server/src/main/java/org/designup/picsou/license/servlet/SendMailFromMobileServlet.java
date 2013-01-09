package org.designup.picsou.license.servlet;

import org.apache.log4j.Logger;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.mail.Mailer;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
      String mailTo = req.getParameter(ConfigService.HEADER_TO_MAIL);
      if (Strings.isNullOrEmpty(mailTo)) {
        logger.info("sendMail: missing mail address " + (mailTo == null ? "<no email>" : mailTo));
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      String lang = req.getParameter(ConfigService.HEADER_LANG);
      if (Strings.isNullOrEmpty(lang)){
        lang = "fr";
      }

      mailTo = mailTo.trim();
      mailer.sendFromMobileToUseBV(mailTo, lang);
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception e) {
      logger.error("sendMail failed: ", e);
    }
  }
}
