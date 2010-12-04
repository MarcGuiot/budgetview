package org.designup.picsou.license.servlet;

import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Strings;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.mail.Mailer;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

public class SendMailServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("sendMail");
  private Directory directory;
  private Mailer mailer;

  public SendMailServlet(Directory directory) {
    mailer = directory.get(Mailer.class);
    this.directory = directory;
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mailTo = req.getHeader(ConfigService.HEADER_TO_MAIL);
    if (Strings.isNullOrEmpty(mailTo)) {
      logger.info("sendMail : missing mail adresse " + (mailTo == null ? "<no mail>" : mailTo));
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String content = req.getHeader(ConfigService.HEADER_MAIL_CONTENT);
    String mailFrom = req.getHeader(ConfigService.HEADER_MAIL);
    String title = req.getHeader(ConfigService.HEADER_MAIL_TITLE);
    if (Strings.isNullOrEmpty(content)) {
      logger.info("sendMail : empty content" + mailTo + " from : " + mailFrom + " title : " + title);
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String lang = req.getHeader(ConfigService.HEADER_LANG);
    mailTo = mailTo.trim();
    content = content.trim();
    mailFrom = mailFrom.trim();
    if (mailTo.equals(ConfigService.MAIL_CONTACT)){
      mailer.sendToSupport(mailFrom, title, content);
    }
    resp.setStatus(HttpServletResponse.SC_OK);
  }
}
