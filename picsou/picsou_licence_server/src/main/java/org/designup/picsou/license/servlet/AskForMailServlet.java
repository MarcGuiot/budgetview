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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AskForMailServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("mail");
  private Mailer mailer;
  private SqlService sqlService;

  public AskForMailServlet(Directory directory) {
    mailer = directory.get(Mailer.class);
    sqlService = directory.get(SqlService.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mailTo = req.getHeader(ConfigService.HEADER_MAIL);
    if (Strings.isNullOrEmpty(mailTo)) {
      logger.info("Bad ask for code from " + (mailTo == null ? "<no mail>" : mailTo));
      resp.addHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_BAD_ADRESS);
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
          resp.addHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_MAIL_UNKNOWN);
          logger.info("unknown user " + mailTo);
          return;
        }
        if (registeredMail.size() >= 1) {
          SqlConnection db = sqlService.getDb();
          try {
            db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mailTo))
              .update(License.ACTIVATION_CODE, activationCode)
              .getRequest().run();
          }
          finally {
            db.commitAndClose();
          }
          if (mailer.sendExistingLicense(registeredMail.get(0), lang, activationCode)) {
            logger.info("Send new activation code " + activationCode + " to " + mailTo);
            resp.addHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_MAIL_SENT);
          }
          else {
            resp.addHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_MAIL_SENT_FAILED);
          }
        }
        if (registeredMail.size() > 1) {
          logger.severe("mail registered multiple time '" + mailTo + "'");
        }
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
    }
    catch (Exception e) {
      logger.throwing("AskForMailServlet", "doPost", e);
      replyFailed(resp);
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
    resp.addHeader(ConfigService.HEADER_STATUS, ConfigService.HEADER_BAD_ADRESS);
  }

  private void replyFailed(HttpServletResponse resp) {
    resp.addHeader(ConfigService.HEADER_STATUS, "fail");
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
