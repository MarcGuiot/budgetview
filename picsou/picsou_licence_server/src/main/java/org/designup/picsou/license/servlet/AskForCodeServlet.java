package org.designup.picsou.license.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.generator.LicenseGenerator;
import org.designup.picsou.license.mail.Mailer;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AskForCodeServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("askForCode");
  private Mailer mailer;
  private SqlService sqlService;

  public AskForCodeServlet(Directory directory) {
    mailer = directory.get(Mailer.class);
    sqlService = directory.get(SqlService.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
    String mailTo = req.getHeader(ConfigService.HEADER_MAIL);
    if (Strings.isNullOrEmpty(mailTo)) {
      logger.info("Bad ask for code from " + (mailTo == null ? "<no mail>" : mailTo));
      resp.setHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_MAIL_UNKNOWN);
      return;
    }
    String lang = req.getHeader(ConfigService.HEADER_LANG);
    mailTo = mailTo.trim();
    logger.info("code requested for '" + mailTo + "' in " + lang);
    try {
      if (checkIsAMailAdress(mailTo)) {
        GlobList registeredMail;
        String activationCode = LicenseGenerator.generateActivationCode();
        registeredMail = request(mailTo);
        if (registeredMail.isEmpty()) {
          resp.setHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_MAIL_UNKNOWN);
          logger.info("unknown user " + mailTo);
          return;
        }
        if (registeredMail.size() >= 1) {
          String currentCode = registeredMail.get(0).get(License.ACTIVATION_CODE);
          if (Strings.isNotEmpty(currentCode)) {
            logger.info("request for code on an not activated code, resend same code");
            activationCode = currentCode;
          }
          else {
            SqlConnection db = sqlService.getDb();
            try {
              db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mailTo))
                .update(License.ACTIVATION_CODE, activationCode)
                .getRequest().run();
            }
            finally {
              db.commitAndClose();
            }
          }
          if (mailer.sendRequestLicence(lang, activationCode, registeredMail.get(0).get(License.MAIL))) {
            logger.info("Send new activation code " + activationCode + " t  o " + mailTo);
            resp.setHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_MAIL_SENT);
          }
          else {
            resp.setHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_MAIL_SENT_FAILED);
          }
        }
//        if (registeredMail.size() > 1) {
//          logger.error("mail registered multiple time '" + mailTo + "'");
//        }
      }
      else {
        SqlConnection db = sqlService.getDb();
        try {
          db.getCreateBuilder(MailError.TYPE)
            .set(MailError.MAIL, mailTo)
            .getRequest().run();
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
    SqlConnection db = sqlService.getDb();
    try {
      return db.getQueryBuilder(License.TYPE,
                                Constraints.equal(License.MAIL, mailTo))
        .select(License.MAIL)
        .getQuery().executeAsGlobs();
    }
    finally {
      db.commitAndClose();
    }
  }

  private void replyBadAdress(HttpServletResponse resp) {
    resp.setHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_MAIL_UNKNOWN);
  }

  private void replyFailed(HttpServletResponse resp) {
    resp.setHeader(ConfigService.HEADER_STATUS, "fail");
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
