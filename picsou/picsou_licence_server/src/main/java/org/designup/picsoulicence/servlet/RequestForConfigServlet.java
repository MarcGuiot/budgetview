package org.designup.picsoulicence.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsoulicence.LicenceGenerator;
import org.designup.picsoulicence.mail.Mailler;
import org.designup.picsoulicence.model.License;
import org.designup.picsoulicence.model.RepoInfo;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

public class RequestForConfigServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("requestForConfig");
  private SqlService sqlService;
  private Mailler mailler;
  private int currentVersion;

  public RequestForConfigServlet(Directory directory) {
    sqlService = directory.get(SqlService.class);
    mailler = directory.get(Mailler.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String id = req.getHeader(ConfigService.HEADER_REPO_ID).trim();
    String mail = req.getHeader(ConfigService.HEADER_MAIL);
    String activationCode = req.getHeader(ConfigService.HEADER_CODE);
    String signature = req.getHeader(ConfigService.HEADER_SIGNATURE);
    String count = req.getHeader(ConfigService.HEADER_COUNT);
    if (mail != null && activationCode != null) {
      computeLicense(resp, mail, activationCode, Long.parseLong(count));
    }
    else {
      SqlConnection db = sqlService.getDb();
      db.getCreateBuilder(RepoInfo.TYPE)
        .set(RepoInfo.REPO_ID, id)
        .getRequest()
        .run();
      db.commit();
    }
    String s = req.getHeader(ConfigService.HEADER_CONFIG_VERSION);
    if (s != null) {
      int remoteVersion = Integer.parseInt(s);
      if (remoteVersion < currentVersion) {

      }
    }
  }

  private void computeLicense(HttpServletResponse resp, String mail, String activationCode, Long count) {
    logger.info("mail : '" + mail + "' count :'" + count + "'");
    try {
      SqlConnection db = sqlService.getDb();
      SelectQuery query = db.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
        .selectAll()
        .getQuery();
      db.commit();
      GlobList globList = query.executeAsGlobs();
      if (globList.isEmpty()) {
        resp.addHeader(ConfigService.HEADER_MAIL_UNKNOWN, "true");
      }
      else {
        Glob license = globList.get(0);
        if (count < license.get(License.LAST_COUNT)) {
          resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
          resp.addHeader(ConfigService.HEADER_MAIL_SENT, "true");
          String code = LicenceGenerator.generateActivationCode();
          db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
            .update(License.ACTIVATION_CODE, code)
            .update(License.LAST_DATE_KILLED_1, new Date())
            .update(License.LAST_DATE_KILLED_2, license.get(License.LAST_DATE_KILLED_1))
            .update(License.LAST_DATE_KILLED_3, license.get(License.LAST_DATE_KILLED_2))
            .update(License.LAST_DATE_KILLED_4, license.get(License.LAST_DATE_KILLED_3))
            .update(License.KILLED_COUNT, license.get(License.KILLED_COUNT) + 1)
            .getRequest()
            .run();
          db.commit();
          mailler.sendNewLicense(mail, code);
        }
        else {
          if (activationCode.equalsIgnoreCase(license.get(License.LAST_ACTIVATION_CODE))) {
            resp.addHeader(ConfigService.HEADER_IS_VALIDE, "true");
          }
          db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
            .update(License.LAST_COUNT, count)
            .update(License.LAST_ACCESS_DATE, new Date())
            .getRequest()
            .run();
        }
      }
    }
    catch (Exception e) {
      logger.throwing("AskForMailServlet", "doPost", e);
      resp.addHeader(ConfigService.HEADER_IS_VALIDE, "true");
    }
  }
}