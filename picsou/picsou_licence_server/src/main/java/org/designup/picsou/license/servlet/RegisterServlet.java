package org.designup.picsou.license.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.generator.LicenseGenerator;
import org.designup.picsou.license.mail.Mailer;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.RepoInfo;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.Encoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.logging.Logger;

public class RegisterServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("RegisterServlet");
  private SqlService sqlService;
  private Mailer mailer;

  public RegisterServlet(Directory directory) {
    sqlService = directory.get(SqlService.class);
    mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mail = req.getHeader(ConfigService.HEADER_MAIL).trim();
    String activationCode = req.getHeader(ConfigService.HEADER_CODE).trim();
    String repoId = req.getHeader(ConfigService.HEADER_REPO_ID).trim();
    String lang = req.getHeader(ConfigService.HEADER_LANG).trim();
    logger.info("mail : '" + mail + "' code d'activation :'" + activationCode + "' repoId : '" +
                repoId + "' lang : " + lang);
    SqlConnection db = sqlService.getDb();
    try {
      register(resp, mail, lang, repoId, activationCode, sqlService.getDb());
    }
    catch (Exception e) {
      logger.throwing("RegisterServlet", "doPost", e);
      SqlConnection db2 = sqlService.getDb();
      try {
        register(resp, mail, lang, repoId, activationCode, db2);
      }
      catch (Exception e1) {
        if (db2 != null) {
          db2.commitAndClose();
        }
      }
    }
    finally {
      if (db != null) {
        db.commitAndClose();
      }
    }
  }

  private void register(HttpServletResponse resp, String mail, String lang, String repoId, String activationCode,
                        SqlConnection db)
    throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    SelectQuery query = db.getQueryBuilder(License.TYPE,
                                           Constraints.equal(License.MAIL, mail))
      .selectAll()
      .getQuery();
    GlobList globList = query.executeAsGlobs();
    db.commit();
    if (globList.isEmpty()) {
      resp.setHeader(ConfigService.HEADER_MAIL_UNKNOWN, "true");
    }
    else {
      Glob license = globList.get(0);
      if (activationCode.equals(license.get(License.ACTIVATION_CODE))) {
        logger.info("License activation ok");
        byte[] signature = LicenseGenerator.generateSignature(mail);
        db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
          .update(License.ACCESS_COUNT, 1L)
          .update(License.SIGNATURE, signature)
          .update(License.ACTIVATION_CODE, (String)null)
          .update(License.LAST_ACTIVATION_CODE, activationCode)
          .update(License.REPO_ID, repoId)
          .update(License.KILLED_REPO_ID, license.get(License.REPO_ID))
          .update(License.DATE_KILLED_1, new Date())
          .update(License.DATE_KILLED_2, license.get(License.DATE_KILLED_1))
          .update(License.DATE_KILLED_3, license.get(License.DATE_KILLED_2))
          .update(License.DATE_KILLED_4, license.get(License.DATE_KILLED_3))
          .update(License.KILLED_COUNT, license.get(License.KILLED_COUNT) + 1)
          .getRequest()
          .run();
        db.getUpdateBuilder(RepoInfo.TYPE, Constraints.equal(RepoInfo.REPO_ID, repoId))
          .update(RepoInfo.LICENSE_ID, license.get(License.ID))
          .getRequest()
          .run();
        db.commit();
        resp.setHeader(ConfigService.HEADER_SIGNATURE, Encoder.byteToString(signature));
      }
      else if (Utils.equal(activationCode, license.get(License.LAST_ACTIVATION_CODE))) {
        String newCode = LicenseGenerator.generateActivationCode();
        logger.info("Mail sent with new code " + newCode);
        db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
          .update(License.ACTIVATION_CODE, newCode)
          .getRequest()
          .run();
        db.commit();
        resp.setHeader(ConfigService.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT, "true");
        if (!mailer.sendNewLicense(mail, newCode, lang)) {
          logger.finest("Fail to send mail retrying.");
        }
      }
      else {
        logger.info("No mail sent");
        resp.setHeader(ConfigService.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_NOT_SENT, "true");
      }
    }
  }
}