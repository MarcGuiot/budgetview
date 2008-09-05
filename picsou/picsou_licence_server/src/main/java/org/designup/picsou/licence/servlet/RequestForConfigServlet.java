package org.designup.picsou.licence.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.licence.mail.Mailler;
import org.designup.picsou.licence.model.License;
import org.designup.picsou.licence.model.RepoInfo;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.streams.accessors.utils.ValueStringAccessor;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestForConfigServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("requestForConfig");
  private SqlService sqlService;
  private Mailler mailler;
  private VersionService versionService;
  private SelectQuery repoIdQuery;
  private ValueStringAccessor repoIdAccessor;
  private SqlConnection db;

  public RequestForConfigServlet(Directory directory) {
    sqlService = directory.get(SqlService.class);
    mailler = directory.get(Mailler.class);
    versionService = directory.get(VersionService.class);
    db = sqlService.getDb();
    repoIdAccessor = new ValueStringAccessor();
    repoIdQuery = db.getQueryBuilder(RepoInfo.TYPE, Constraints.equal(RepoInfo.REPO_ID, repoIdAccessor))
      .selectAll()
      .getNotAutoCloseQuery();
    logger.info("RequestForConfigServlet started");
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String id = req.getHeader(ConfigService.HEADER_REPO_ID).trim();
    String mail = req.getHeader(ConfigService.HEADER_MAIL);
    String activationCode = req.getHeader(ConfigService.HEADER_CODE);
    String count = req.getHeader(ConfigService.HEADER_COUNT);
    String signature = req.getHeader(ConfigService.HEADER_SIGNATURE);
    String applicationVersion = req.getHeader(ConfigService.HEADER_CONFIG_VERSION);
    if (mail != null && activationCode != null) {
      computeLicense(resp, mail, activationCode, Long.parseLong(count), id);
    }
    else {
      computeAnonymous(id, resp);
    }
    resp.setHeader(ConfigService.HEADER_NEW_JAR_VERSION, Long.toString(versionService.getJarVersion()));
    resp.setHeader(ConfigService.HEADER_NEW_CONFIG_VERSION, Long.toString(versionService.getConfigVersion()));
  }

  private void computeAnonymous(String id, HttpServletResponse resp) {
    try {
      logger.info("computeAnonymous " + id);
      repoIdAccessor.setValue(id);
      GlobList globList = repoIdQuery.executeAsGlobs();
      db.commit();
      if (globList.size() == 0) {
        db.getCreateBuilder(RepoInfo.TYPE)
          .set(RepoInfo.REPO_ID, id)
          .set(RepoInfo.LAST_ACCESS_DATE, new Date())
          .set(RepoInfo.COUNT, 1L)
          .getRequest()
          .run();
        db.commit();
      }
      else if (globList.size() > 1) {
        logger.finest("many repo with the same id");
      }
      if (globList.size() >= 1) {
        Long accessCount = globList.get(0).get(RepoInfo.COUNT) + 1;
        logger.info(" accessCount = " + accessCount);
        db.getUpdateBuilder(RepoInfo.TYPE, Constraints.equal(RepoInfo.REPO_ID, id))
          .update(RepoInfo.LAST_ACCESS_DATE, new Date())
          .update(RepoInfo.COUNT, accessCount)
          .getRequest().run();
        db.commit();
      }
    }
    catch (Exception e) {
      logger.log(Level.SEVERE, "RequestForConfigServlet : ", e);
    }
    finally {
      if (db != null) {
        db.commit();
      }
    }
    resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
  }

  private void computeLicense(HttpServletResponse resp, String mail, String activationCode,
                              Long count, String repoId) {
    logger.info("compute licence : mail : '" + mail + "' count :'" + count + "' " + "repoId :'" + repoId + "'");
    SqlConnection db = null;
    try {
      db = sqlService.getDb();
      SelectQuery query = db.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
        .selectAll()
        .getQuery();
      db.commit();
      GlobList globList = query.executeAsGlobs();
      if (globList.isEmpty()) {
        resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
        resp.addHeader(ConfigService.HEADER_MAIL_UNKNOWN, "true");
        logger.info("unknown mail : " + mail);
      }
      else {
        Glob license = globList.get(0);
        if (count < license.get(License.ACCESS_COUNT)) {
          resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
          if (Utils.equal(activationCode, license.get(License.LAST_ACTIVATION_CODE))) {
            resp.addHeader(ConfigService.HEADER_MAIL_SENT, "true");
            String code = LicenceGenerator.generateActivationCode();
            db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
              .update(License.ACTIVATION_CODE, code)
              .getRequest()
              .run();
            db.commit();
            mailler.sendNewLicense(mail, code);
            logger.info("send new license to " + mail);
          }
        }
        else {
          if (Utils.equal(activationCode, license.get(License.LAST_ACTIVATION_CODE))) {
            resp.addHeader(ConfigService.HEADER_IS_VALIDE, "true");
            db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
              .update(License.ACCESS_COUNT, count)
              .update(License.LAST_ACCESS_DATE, new Date())
              .getRequest()
              .run();
            db.commit();
          }
          else {
            resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
            resp.addHeader(ConfigService.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_NOT_SENT, "true");
            logger.info("Bad activation code for " + mail);
          }
        }
      }
    }
    catch (Exception e) {
      logger.throwing("RequestForConfigServlet", "computeLicense", e);
      resp.addHeader(ConfigService.HEADER_IS_VALIDE, "true");
    }
    finally {
      if (db != null) {
        db.commit();
      }
    }
  }

  public void destroy() {
    super.destroy();
    repoIdQuery.close();
    db.commitAndClose();
  }
}