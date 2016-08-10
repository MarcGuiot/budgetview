package com.budgetview.server.license.servlet;

import com.budgetview.server.license.generator.LicenseGenerator;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.RepoInfo;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
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
  private GlobsDatabase db;
  private Mailer mailer;

  public RegisterServlet(Directory directory) {
    db = directory.get(GlobsDatabase.class);
    mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
    String mail = req.getHeader(LicenseConstants.HEADER_MAIL).trim();
    String activationCode = req.getHeader(LicenseConstants.HEADER_CODE).trim();
    String repoId = req.getHeader(LicenseConstants.HEADER_REPO_ID).trim();
    String lang = req.getHeader(MobileConstants.HEADER_LANG).trim();
    logger.info("mail : '" + mail + "' code d'activation :'" + activationCode + "' repoId : '" +
                repoId + "' lang : " + lang);
    SqlConnection connection = db.connect();
    try {
      register(resp, mail, lang, repoId, activationCode, db.connect());
    }
    catch (Exception e) {
      logger.error("RegisterServlet:doPost", e);
      SqlConnection retryConnection = db.connect();
      try {
        register(resp, mail, lang, repoId, activationCode, retryConnection);
      }
      catch (Exception e1) {
        if (retryConnection != null) {
          retryConnection.commitAndClose();
        }
      }
    }
    finally {
      if (connection != null) {
        connection.commitAndClose();
      }
    }
  }

  private void register(HttpServletResponse resp, String mail, String lang, String repoId, String activationCode,
                        SqlConnection db)
    throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    GlobList globList =
      db.selectAll(License.TYPE, Constraints.equal(License.MAIL, mail))
        .sortSelf(COMPARATOR);
    db.commit();
    if (globList.isEmpty()) {
      resp.setHeader(LicenseConstants.HEADER_MAIL_UNKNOWN, "true");
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
          db.startUpdate(License.TYPE, Constraints.equal(License.ID, license.get(License.ID)))
            .set(License.ACCESS_COUNT, 1L)
            .set(License.SIGNATURE, signature)
            .set(License.ACTIVATION_CODE, (String) null)
            .set(License.LAST_ACTIVATION_CODE, activationCode)
            .set(License.REPO_ID, repoId)
            .set(License.TIME_STAMP, System.currentTimeMillis())
            .set(License.KILLED_REPO_ID, license.get(License.REPO_ID))
            .set(License.DATE_KILLED_1, new Date())
            .set(License.DATE_KILLED_2, license.get(License.DATE_KILLED_1))
            .set(License.DATE_KILLED_3, license.get(License.DATE_KILLED_2))
            .set(License.DATE_KILLED_4, license.get(License.DATE_KILLED_3))
            .set(License.KILLED_COUNT, license.get(License.KILLED_COUNT) + 1)
            .run();
          db.startUpdate(RepoInfo.TYPE, Constraints.equal(RepoInfo.REPO_ID, repoId))
            .set(RepoInfo.LICENSE_ID, license.get(License.ID))
            .run();
          db.commit();
          resp.setHeader(LicenseConstants.HEADER_SIGNATURE, Encoder.byteToString(signature));
          resp.setStatus(HttpServletResponse.SC_OK);
          for (Glob glob : globList) {
            if (glob != license) {
              // on ne doit avoir qu'un seul enregistrement valide par repo.
              if (Utils.equal(glob.get(License.REPO_ID), repoId)) {
                db.startUpdate(License.TYPE, Constraints.equal(License.ID, glob.get(License.ID)))
                  .set(License.REPO_ID, ((String) null))
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
          db.startUpdate(License.TYPE, Constraints.equal(License.MAIL, mail))
            .set(License.ACTIVATION_CODE, newCode)
            .run();
          db.commit();
          resp.setHeader(LicenseConstants.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT, "true");
          if (!mailer.reSendExistingLicenseOnError(lang, newCode, mail)) {
            logger.error("Fail to send mail retrying.");
          }
          resp.setStatus(HttpServletResponse.SC_OK);
          return;
        }
      }
      logger.info("No mail sent (activation failed)");
      resp.setHeader(LicenseConstants.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT, "false");
    }
    resp.setStatus(HttpServletResponse.SC_OK);
  }
}