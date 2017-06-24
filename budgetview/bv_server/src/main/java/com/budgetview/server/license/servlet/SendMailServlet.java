package com.budgetview.server.license.servlet;

import com.budgetview.server.license.mail.Mailbox;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
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
  static Logger logger = Logger.getLogger("SendMailServlet");
  private Mailer mailer;

  public SendMailServlet(Directory directory) {
    mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
    try {
      request.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");

      String mailTo = request.getHeader(LicenseConstants.HEADER_MAIL_TO);
      if (Strings.isNullOrEmpty(mailTo)) {
        logger.info("Missing mail address: " + (mailTo == null ? "<no email>" : mailTo));
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      String mailFrom = request.getHeader(LicenseConstants.HEADER_MAIL_FROM);
      String title = request.getHeader(LicenseConstants.HEADER_MAIL_TITLE);
      String lang = request.getHeader(MobileConstants.HEADER_LANG);
      String header = request.getHeader(LicenseConstants.HEADER_MAIL_CONTENT);
      String content;
      if (Strings.isNotEmpty(header)) {
        content = LicenseConstants.decodeContent(header);
      }
      else {
        content = Files.loadStreamToString(request.getInputStream(), "UTF-8");
      }
      logger.info("mail from " + mailFrom + "\ntitle " + title + "\n mail : " + content + "\n");
      if (Strings.isNullOrEmpty(content)) {
        logger.info("Empty content - mail will not be sent");
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      mailTo = mailTo.trim();
      content = content.trim();
      mailFrom = mailFrom.trim();
      if (mailTo.equals(LicenseConstants.SUPPORT_EMAIL)) {
        mailer.sendToUs(Mailbox.SUPPORT, mailFrom, title, content);
      }
      else if (mailTo.equals(LicenseConstants.ADMIN_EMAIL)) {
        mailer.sendToUs(Mailbox.ADMIN, mailFrom, title, content);
      }
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception e) {
      logger.error("sendMail failed: ", e);
    }
  }
}
