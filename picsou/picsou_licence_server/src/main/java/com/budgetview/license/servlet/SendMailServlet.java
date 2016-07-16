package com.budgetview.license.servlet;

import com.budgetview.http.HttpBudgetViewConstants;
import com.budgetview.license.mail.Mailbox;
import com.budgetview.license.mail.Mailer;
import com.budgetview.shared.utils.MobileConstants;
import org.apache.log4j.Logger;
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
      String mailTo = req.getHeader(HttpBudgetViewConstants.HEADER_TO_MAIL);
      if (Strings.isNullOrEmpty(mailTo)) {
        logger.info("sendMail: missing mail address " + (mailTo == null ? "<no email>" : mailTo));
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      String mailFrom = req.getHeader(HttpBudgetViewConstants.HEADER_MAIL);
      String title = req.getHeader(HttpBudgetViewConstants.HEADER_MAIL_TITLE);
      String lang = req.getHeader(MobileConstants.HEADER_LANG);
      String header = req.getHeader(HttpBudgetViewConstants.HEADER_MAIL_CONTENT);
      String content;
      if (header != null) {
        content = HttpBudgetViewConstants.decodeContent(header);
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
      if (mailTo.equals(HttpBudgetViewConstants.SUPPORT_EMAIL)) {
        mailer.sendToUs(Mailbox.SUPPORT, mailFrom, title, content);
      }
      else if (mailTo.equals(HttpBudgetViewConstants.ADMIN_EMAIL)) {
        mailer.sendToUs(Mailbox.ADMIN, mailFrom, title, content);
      }
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception e) {
      logger.error("sendMail failed: ", e);
    }
  }
}
