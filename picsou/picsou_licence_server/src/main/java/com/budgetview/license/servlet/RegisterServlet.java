package com.budgetview.license.servlet;

import com.budgetview.gui.config.ConfigService;
import com.budgetview.license.generator.LicenseGenerator;
import com.budgetview.license.mail.Mailer;
import com.budgetview.license.model.License;
import com.budgetview.license.model.RepoInfo;
import com.budgetview.shared.utils.ComCst;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobFieldsComparator;
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

public class RegisterServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("RegisterServlet");
  public static final GlobFieldsComparator COMPARATOR = 
    new GlobFieldsComparator(License.ACTIVATION_CODE, false, License.TIME_STAMP, true);
  private SqlService sqlService;
  private Mailer mailer;

  public RegisterServlet(Directory directory) {
    sqlService = directory.get(SqlService.class);
    mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
    String mail = req.getHeader(ConfigService.HEADER_MAIL).trim();
    String activationCode = req.getHeader(ConfigService.HEADER_CODE).trim();
    String repoId = req.getHeader(ConfigService.HEADER_REPO_ID).trim();
    String lang = req.getHeader(ComCst.HEADER_LANG).trim();
    logger.info("mail : '" + mail + "' code d'activation :'" + activationCode + "' repoId : '" +
                repoId + "' lang : " + lang);
    SqlConnection db = sqlService.getDb();
    try {
      register(resp, mail, lang, repoId, activationCode, sqlService.getDb());
    }
    catch (Exception e) {
      logger.error("RegisterServlet:doPost", e);
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
    GlobList globList = query.executeAsGlobs()
      .sortSelf(COMPARATOR);
    db.commit();
    if (globList.isEmpty()) {
      resp.setHeader(ConfigService.HEADER_MAIL_UNKNOWN, "true");
      logger.info("unknown user " + mail);
    }
    else {
      for (Glob license : globList) {
        if (activationCode.equals(license.get(License.ACTIVATION_CODE))) {
          logger.info("License activation ok " + license.get(License.ID));
          if (license.get(License.REPO_ID) != null) {
            logger.info("Invalidating previous " + license.get(License.ID) + " ropId : " + license.get(License.REPO_ID));
          }
          byte[] signature = LicenseGenerator.generateSignature(mail);
          db.getUpdateBuilder(License.TYPE, Constraints.equal(License.ID, license.get(License.ID)))
            .update(License.ACCESS_COUNT, 1L)
            .update(License.SIGNATURE, signature)
            .update(License.ACTIVATION_CODE, (String)null)
            .update(License.LAST_ACTIVATION_CODE, activationCode)
            .update(License.REPO_ID, repoId)
            .update(License.TIME_STAMP, System.currentTimeMillis())
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
          resp.setStatus(HttpServletResponse.SC_OK);
          for (Glob glob : globList) {
            if (glob != license) {
              // on ne doit avoir qu'un seul enregistrement valide par repo.
              if (Utils.equal(glob.get(License.REPO_ID), repoId)) {
                db.getUpdateBuilder(License.TYPE, Constraints.equal(License.ID, glob.get(License.ID)))
                  .update(License.REPO_ID, ((String)null))
                  .getRequest()
                  .run();
                db.commit();
                logger.info("duplicate line with same repoid => updating to null other repoId");
              }
            }
          }
          return;
        }
      }
      for (Glob license : globList) {
        if (Utils.equal(activationCode, license.get(License.LAST_ACTIVATION_CODE))) {
          String newCode = LicenseGenerator.generateActivationCode();
          logger.info("Mail sent with new code " + newCode);
          db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
            .update(License.ACTIVATION_CODE, newCode)
            .getRequest()
            .run();
          db.commit();
          resp.setHeader(ConfigService.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT, "true");
          if (!mailer.reSendExistingLicenseOnError(lang, newCode, mail)) {
            logger.error("Fail to send mail retrying.");
          }
          resp.setStatus(HttpServletResponse.SC_OK);
          return;
        }
      }
      logger.info("No mail sent (activation failed)");
      resp.setHeader(ConfigService.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT, "false");
    }
    resp.setStatus(HttpServletResponse.SC_OK);
  }
}