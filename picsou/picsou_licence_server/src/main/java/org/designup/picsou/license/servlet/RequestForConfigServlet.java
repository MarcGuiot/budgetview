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
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.streams.accessors.utils.ValueDateAccessor;
import org.globsframework.streams.accessors.utils.ValueLongAccessor;
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
  private Mailer mailer;
  private VersionService versionService;
  private SqlConnection db;
  private CreateAnonymousRequest createAnonymousRequest;
  private UpdateNewActivationCodeRequest updateNewActivationCodeRequest;
  private LicenseRequest licenseRequest;
  private UpdateAnonymousAccesCount updateAnonymousAccesCount;
  private UpdateLastAccessRequest updateLastAccessRequest;
  private RepoIdAnonymousRequest repoIdAnonymousRequest;

  public RequestForConfigServlet(Directory directory) {
    sqlService = directory.get(SqlService.class);
    mailer = directory.get(Mailer.class);
    versionService = directory.get(VersionService.class);
    logger.info("RequestForConfigServlet started");
    initDb();
  }

  private void initDb() {
    db = sqlService.getDb();
    createAnonymousRequest = new CreateAnonymousRequest(db);
    licenseRequest = new LicenseRequest(db);
    updateNewActivationCodeRequest = new UpdateNewActivationCodeRequest(db);
    updateLastAccessRequest = new UpdateLastAccessRequest(db);
    updateAnonymousAccesCount = new UpdateAnonymousAccesCount(db);
    repoIdAnonymousRequest = new RepoIdAnonymousRequest(db);
    logger.info("RequestForConfigServlet.init");
  }

  public void destroy() {
    super.destroy();
    logger.info("RequestForConfigServlet.destroy");
    closeDb();
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String id = req.getHeader(ConfigService.HEADER_REPO_ID).trim();
    String mail = req.getHeader(ConfigService.HEADER_MAIL);
    String activationCode = req.getHeader(ConfigService.HEADER_CODE);
    String count = req.getHeader(ConfigService.HEADER_COUNT);
    String lang = req.getHeader(ConfigService.HEADER_LANG);
    String signature = req.getHeader(ConfigService.HEADER_SIGNATURE);
    String applicationVersion = req.getHeader(ConfigService.HEADER_CONFIG_VERSION);
    if (mail != null && activationCode != null) {
      computeLicense(resp, mail, activationCode, Long.parseLong(count), id, lang);
    }
    else {
      computeAnonymous(id, resp);
    }
    resp.setHeader(ConfigService.HEADER_NEW_JAR_VERSION, Long.toString(versionService.getJarVersion()));
    resp.setHeader(ConfigService.HEADER_NEW_CONFIG_VERSION, Long.toString(versionService.getConfigVersion()));
  }

  private void computeAnonymous(String id, HttpServletResponse resp) {
    try {
      computeAnonymous(id);
    }
    catch (Exception e) {
      logger.log(Level.SEVERE, "RequestForConfigServlet : ", e);
      closeDb();
      initDb();
      try {
        computeAnonymous(id);
      }
      catch (Exception e1) {
        logger.log(Level.SEVERE, "RequestForConfigServlet : Retry fail", e);
      }
    }
    finally {
      if (db != null) {
        db.commit();
      }
    }
    resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
  }

  private void closeDb() {
    try {
      licenseRequest.close();
      updateAnonymousAccesCount.close();
      updateLastAccessRequest.close();
      updateNewActivationCodeRequest.close();
      repoIdAnonymousRequest.close();
      db.commitAndClose();
    }
    catch (Exception e1) {
      try {
        db.commitAndClose();
      }
      catch (Exception e2) {
      }
    }
  }

  static class RepoIdAnonymousRequest {
    private SelectQuery repoIdQuery;
    private ValueStringAccessor repoIdAccessor;

    RepoIdAnonymousRequest(SqlConnection db) {
      repoIdAccessor = new ValueStringAccessor();
      repoIdQuery = db.getQueryBuilder(RepoInfo.TYPE, Constraints.equal(RepoInfo.REPO_ID, repoIdAccessor))
        .selectAll()
        .getNotAutoCloseQuery();
    }

    public GlobList execute(String repoId) {
      repoIdAccessor.setValue(repoId);
      return repoIdQuery.executeAsGlobs();
    }

    public void close() {
      try {
        repoIdQuery.close();
      }
      catch (Exception e) {
      }
    }
  }

  static class CreateAnonymousRequest {
    private SqlRequest createAnonymousRequest;
    private ValueStringAccessor repoId;
    private ValueDateAccessor date;

    CreateAnonymousRequest(SqlConnection db) {
      repoId = new ValueStringAccessor();
      date = new ValueDateAccessor();
      createAnonymousRequest = db.getCreateBuilder(RepoInfo.TYPE)
        .set(RepoInfo.REPO_ID, repoId)
        .set(RepoInfo.LAST_ACCESS_DATE, date)
        .set(RepoInfo.COUNT, 1L)
        .getRequest();
    }

    public void execute(String repoId, Date date) {
      this.repoId.setValue(repoId);
      this.date.setValue(date);
      createAnonymousRequest.run();
    }
  }

  static class UpdateAnonymousAccesCount {
    private ValueStringAccessor repoId;
    private ValueDateAccessor date;
    private ValueLongAccessor count;
    private SqlRequest request;

    UpdateAnonymousAccesCount(SqlConnection db) {
      repoId = new ValueStringAccessor();
      date = new ValueDateAccessor();
      count = new ValueLongAccessor();
      request = db.getUpdateBuilder(RepoInfo.TYPE, Constraints.equal(RepoInfo.REPO_ID, repoId))
        .update(RepoInfo.LAST_ACCESS_DATE, date)
        .update(RepoInfo.COUNT, count)
        .getRequest();
    }

    public void execute(String repoId, Date date, long count) {
      this.repoId.setValue(repoId);
      this.date.setValue(date);
      this.count.setValue(count);
      request.run();
    }

    public void close() {
      request.close();
    }
  }

  private void computeAnonymous(String id) {
    logger.info("computeAnonymous " + id);
    GlobList globList = repoIdAnonymousRequest.execute(id);
    db.commit();
    if (globList.size() == 0) {
      createAnonymousRequest.execute(id, new Date());
      db.commit();
    }
    else if (globList.size() > 1) {
      logger.finest("many repo with the same id");
    }
    if (globList.size() >= 1) {
      Long accessCount = globList.get(0).get(RepoInfo.COUNT) + 1;
      logger.info(" accessCount = " + accessCount);
      updateAnonymousAccesCount.execute(id, new Date(), accessCount);
      db.commit();
    }
  }

  private void computeLicense(HttpServletResponse resp, String mail, String activationCode,
                              Long count, String repoId, String lang) {
    logger.info("compute license : mail : '" + mail + "' count :'" + count + "' " +
                "repoId :'" + repoId + "' code :'" + activationCode + "'");
    try {
      computeLicense(resp, mail, activationCode, count, lang);
    }
    catch (Exception e) {
      logger.throwing("RequestForConfigServlet", "computeLicense", e);
      try {
        closeDb();
        initDb();
        computeLicense(resp, mail, activationCode, count, lang);
      }
      catch (Exception ex) {
        logger.throwing("RequestForConfigServlet", "computeLicense retry", e);
      }
      resp.addHeader(ConfigService.HEADER_IS_VALIDE, "true");
    }
  }

  private void computeLicense(HttpServletResponse resp, String mail,
                              String activationCode, Long count, String lang) {
    GlobList globList = licenseRequest.execute(mail);
    db.commit();
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
          String code = LicenseGenerator.generateActivationCode();
          updateNewActivationCodeRequest.execute(mail, code);
          db.commit();
          resp.addHeader(ConfigService.HEADER_MAIL_SENT, "true");
          if (mailer.sendNewLicense(mail, code, lang)) {
            logger.info("Run count decrease send new license to " + mail);
          }
        }
        else {
          logger.info("Run count decrease with different activation code for " + mail);
        }
      }
      else {
        if (Utils.equal(activationCode, license.get(License.LAST_ACTIVATION_CODE))) {
          updateLastAccessRequest.execute(mail, count, new Date());
          db.commit();
          resp.addHeader(ConfigService.HEADER_IS_VALIDE, "true");
          logger.info("ok for " + mail);
        }
        else {
          resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
          resp.addHeader(ConfigService.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_NOT_SENT, "true");
          logger.info("Bad activation code for " + mail);
        }
      }
    }
  }

  static class LicenseRequest {
    private ValueStringAccessor mail;
    private SelectQuery query;

    LicenseRequest(SqlConnection db) {
      mail = new ValueStringAccessor();
      query = db.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
        .selectAll()
        .getNotAutoCloseQuery();
    }

    public GlobList execute(String mail) {
      this.mail.setValue(mail);
      return query.executeAsGlobs();
    }

    public void close() {
      try {
        query.close();
      }
      catch (Exception e) {
      }
    }
  }

  static class UpdateNewActivationCodeRequest {
    private ValueStringAccessor activationCode;
    private ValueStringAccessor mail;
    private SqlRequest request;

    UpdateNewActivationCodeRequest(SqlConnection db) {
      mail = new ValueStringAccessor();
      activationCode = new ValueStringAccessor();
      request = db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
        .update(License.ACTIVATION_CODE, activationCode)
        .getRequest();
    }

    public void execute(String mail, String activationCode) {
      this.mail.setValue(mail);
      this.activationCode.setValue(activationCode);
      request.run();
    }

    public void close() {
      try {
        request.close();
      }
      catch (Exception e) {
      }
    }
  }

  static class UpdateLastAccessRequest {

    private ValueStringAccessor mail;

    private SqlRequest request;
    private ValueLongAccessor count;
    private ValueDateAccessor date;

    UpdateLastAccessRequest(SqlConnection db) {
      mail = new ValueStringAccessor();
      count = new ValueLongAccessor();
      this.date = new ValueDateAccessor();
      request = db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
        .update(License.ACCESS_COUNT, count)
        .update(License.LAST_ACCESS_DATE, this.date)
        .getRequest();
    }

    public void execute(String mail, long count, Date date) {
      this.mail.setValue(mail);
      this.count.setValue(count);
      this.date.setValue(date);
      request.run();
    }

    public void close() {
      try {
        request.close();
      }
      catch (Exception e) {
      }
    }
  }
}