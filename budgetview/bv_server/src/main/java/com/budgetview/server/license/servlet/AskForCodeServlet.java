package com.budgetview.server.license.servlet;

import com.budgetview.server.license.generator.LicenseGenerator;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.MailError;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import org.apache.log4j.Logger;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AskForCodeServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("AskForCodeServlet");
  private Mailer mailer;
  private GlobsDatabase globsDB;

  public AskForCodeServlet(Directory directory) {
    mailer = directory.get(Mailer.class);
    globsDB = directory.get(GlobsDatabase.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
    String mailTo = req.getHeader(LicenseConstants.HEADER_MAIL_FROM);
    if (Strings.isNullOrEmpty(mailTo)) {
      logger.info("Bad ask for code from " + (mailTo == null ? "<no mail>" : mailTo));
      resp.setHeader(LicenseConstants.HEADER_STATUS, LicenseConstants.HEADER_MAIL_UNKNOWN);
      return;
    }
    String lang = req.getHeader(MobileConstants.HEADER_LANG);
    mailTo = mailTo.trim();
    logger.info("code requested for '" + mailTo + "' in " + lang);
    try {
      if (checkIsAMailAdress(mailTo)) {
        String activationCode = LicenseGenerator.generateActivationCode();
        GlobList registeredMails = request(mailTo);
        if (registeredMails.isEmpty()) {
          resp.setHeader(LicenseConstants.HEADER_STATUS, LicenseConstants.HEADER_MAIL_UNKNOWN);
          logger.info("unknown user " + mailTo);
          return;
        }
        if (registeredMails.size() >= 1) {
          String currentCode = registeredMails.get(0).get(License.ACTIVATION_CODE);
          if (Strings.isNotEmpty(currentCode)) {
            logger.info("request for code on an not activated code, resend same code");
            activationCode = currentCode;
          }
          else {
            SqlConnection db = globsDB.connect();
            try {
              db.startUpdate(License.TYPE, Where.fieldEquals(License.MAIL, mailTo))
                .set(License.ACTIVATION_CODE, activationCode)
                .run();
            }
            finally {
              db.commitAndClose();
            }
          }
          if (mailer.sendRequestLicence(lang, activationCode, registeredMails.get(0).get(License.MAIL))) {
            logger.info("Send new activation code " + activationCode + " to " + mailTo);
            resp.setHeader(LicenseConstants.HEADER_STATUS, LicenseConstants.HEADER_MAIL_SENT);
          }
          else {
            resp.setHeader(LicenseConstants.HEADER_STATUS, LicenseConstants.HEADER_MAIL_SENT_FAILED);
          }
        }
//        if (registeredMail.size() > 1) {
//          logger.error("mail registered multiple time '" + mailTo + "'");
//        }
      }
      else {
        SqlConnection db = globsDB.connect();
        try {
          db.startCreate(MailError.TYPE)
            .set(MailError.MAIL, mailTo)
            .run();
        }
        finally {
          db.commitAndClose();
        }
        replyBadAdress(resp);
      }
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception e) {
      logger.error("AskForCodeServlet:doPost", e);
      replyFailed(resp);
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private GlobList request(String mailTo) {
    try {
      return requestDb(mailTo);
    }
    catch (Exception e) {
      return requestDb(mailTo);
    }
  }

  private GlobList requestDb(String mailTo) {
    SqlConnection db = globsDB.connect();
    try {
      return db.startSelect(License.TYPE, Where.fieldEquals(License.MAIL, mailTo))
        .select(License.MAIL)
        .getList();
    }
    finally {
      db.commitAndClose();
    }
  }

  private void replyBadAdress(HttpServletResponse resp) {
    resp.setHeader(LicenseConstants.HEADER_STATUS, LicenseConstants.HEADER_MAIL_UNKNOWN);
  }

  private void replyFailed(HttpServletResponse resp) {
    resp.setHeader(LicenseConstants.HEADER_STATUS, "fail");
  }

  private boolean checkIsAMailAdress(String to) {
    // from http://www.regular-expressions.info/email.html
    Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$");
    Matcher matcher = pattern.matcher(to.toUpperCase());
    if (matcher.matches()) {
      return true;
    }
    else {
      return false;
    }
  }
}
