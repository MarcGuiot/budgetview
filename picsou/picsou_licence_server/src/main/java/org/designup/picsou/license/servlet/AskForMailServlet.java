package org.designup.picsou.license.servlet;

import org.designup.picsou.license.mail.Mailer;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.designup.picsou.license.generator.LicenseGenerator;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.directory.Directory;

import javax.mail.MessagingException;
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
    String mailTo = req.getHeader("mailTo").trim();
    logger.info("mail : " + mailTo);
    try {
      if (checkIsAMailAdress(mailTo)) {
        SqlConnection db = sqlService.getDb();
        GlobList registeredMail;
        try {
          String activationCode = LicenseGenerator.generateActivationCode();
          registeredMail = db.getQueryBuilder(License.TYPE,
                                              Constraints.equal(License.MAIL, mailTo))
            .select(License.MAIL)
            .getQuery().executeAsGlobs();
          if (registeredMail.isEmpty()) {
            GlobBuilder.init(License.TYPE)
              .set(License.MAIL, mailTo)
              .set(License.ACTIVATION_CODE, activationCode);
            db.getCreateBuilder(License.TYPE)

              .set(License.MAIL, mailTo).getRequest().run();
          }
        }
        finally {
          db.commitAndClose();
        }
        if (registeredMail.size() >= 1) {
          mailer.sendExistingLicense(registeredMail.get(0));
          replyOk(resp);
        }
        else {
          mailer.sendRequestLicense(mailTo);
          replyOk(resp);
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
    catch (MessagingException e) {
      logger.throwing("AskForMailServlet", "doPost", e);
      replyFailed(resp);
    }
  }

  private void replyBadAdress(HttpServletResponse resp) {
    resp.addHeader("status", "badAdress");
  }

  private void replyFailed(HttpServletResponse resp) {
    resp.addHeader("status", "mailError");
  }

  private void replyOk(HttpServletResponse resp) {
    resp.addHeader("status", "mailSent");
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
