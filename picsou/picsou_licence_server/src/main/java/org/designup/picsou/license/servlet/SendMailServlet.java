package org.designup.picsou.license.servlet;

import com.budgetview.shared.utils.ComCst;
import org.apache.log4j.Logger;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.mail.Mailbox;
import org.designup.picsou.license.mail.Mailer;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SendMailServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("sendMail");
  private Mailer mailer;

  public SendMailServlet(Directory directory) {
    mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      req.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");
      String mailTo = req.getHeader(ConfigService.HEADER_TO_MAIL);
      if (Strings.isNullOrEmpty(mailTo)) {
        logger.info("sendMail: missing mail address " + (mailTo == null ? "<no email>" : mailTo));
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      String mailFrom = req.getHeader(ConfigService.HEADER_MAIL);
      String title = req.getHeader(ConfigService.HEADER_MAIL_TITLE);
      String lang = req.getHeader(ComCst.HEADER_LANG);
      String header = req.getHeader(ConfigService.HEADER_MAIL_CONTENT);
      String content;
      if (header != null) {
        content = ConfigService.decodeContent(header);
      }
      else {
        content = Files.loadStreamToString(req.getInputStream(), "UTF-8");
      }
      logger.info("mail from " + mailFrom + "\ntitle " + title + "\n mail : " + content + "\n");
      if (Strings.isNullOrEmpty(content)) {
        logger.info("sendMail : empty content" + mailTo + " from : " + mailFrom + " title : " + title);
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      mailTo = mailTo.trim();
      content = content.trim();
      mailFrom = mailFrom.trim();
      if (mailTo.equals(ConfigService.SUPPORT_EMAIL)) {
        mailer.sendToUs(Mailbox.SUPPORT, mailFrom, title, content);
      }
      else if (mailTo.equals(ConfigService.ADMIN_EMAIL)) {
        mailer.sendToUs(Mailbox.ADMIN, mailFrom, title, content);
      }
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception e) {
      logger.error("sendMail failed: ", e);
    }
  }
}
