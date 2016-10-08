package com.budgetview.server.license.servlet;

import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.RepoInfo;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.streams.accessors.utils.ValueDateAccessor;
import org.globsframework.streams.accessors.utils.ValueIntegerAccessor;
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

public class RequestForConfigServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("requestForConfig");
  private GlobsDatabase db;
  private Mailer mailer;
  private VersionService versionService;
  private SqlConnection connection;
  private CreateAnonymousRequest createAnonymousRequest;
  private UpdateNewActivationCodeRequest updateNewActivationCodeRequest;
  private LicenseRequest licenseRequest;
  private UpdateAnonymousAccessCount updateAnonymousAccessCount;
  private UpdateLastAccessRequest updateLastAccessRequest;
  private RepoIdAnonymousRequest repoIdAnonymousRequest;

  public RequestForConfigServlet(Directory directory) {
    db = directory.get(GlobsDatabase.class);
    mailer = directory.get(Mailer.class);
    versionService = directory.get(VersionService.class);
    logInfo("RequestForConfigServlet started");
    initDb();
  }

  private void initDb() {
    connection = db.connect();
    createAnonymousRequest = new CreateAnonymousRequest(connection);
    licenseRequest = new LicenseRequest(connection);
    updateNewActivationCodeRequest = new UpdateNewActivationCodeRequest(connection);
    updateLastAccessRequest = new UpdateLastAccessRequest(connection);
    updateAnonymousAccessCount = new UpdateAnonymousAccessCount(connection);
    repoIdAnonymousRequest = new RepoIdAnonymousRequest(connection);
    logInfo("RequestForConfigServlet.init");
  }

  public void destroy() {
    super.destroy();
    logInfo("RequestForConfigServlet.destroy");
    closeDb();
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
    String ip = req.getRemoteAddr();
    String repoId = req.getHeader(LicenseConstants.HEADER_REPO_ID).trim();
    String mail = req.getHeader(LicenseConstants.HEADER_MAIL_FROM);
    String activationCode = req.getHeader(LicenseConstants.HEADER_CODE);
    String count = req.getHeader(LicenseConstants.HEADER_COUNT);
    String lang = req.getHeader(MobileConstants.HEADER_LANG);
    String signature = req.getHeader(LicenseConstants.HEADER_SIGNATURE);
    String configVersion = req.getHeader(LicenseConstants.HEADER_CONFIG_VERSION);
    String jarVersion = req.getHeader(LicenseConstants.HEADER_JAR_VERSION);
    VersionService.JarInfo info = null;
    try {
      info = new VersionService.JarInfo(Long.parseLong(jarVersion),
                                        Long.parseLong(configVersion));
    }
    catch (Exception e) {
      logger.info("No version info.");
    }
    Integer group = 0;
    if (mail != null && activationCode != null) {
      if (count == null || repoId == null || lang == null) {
        logInfo("For " + mail + " ip = " + ip + ", one element is missing count : " + count + ", id :" + repoId + ", lang : " + lang);
        resp.setHeader(LicenseConstants.HEADER_IS_VALID, "false");
        resp.setHeader(LicenseConstants.HEADER_MAIL_UNKNOWN, "true");
      }
      else {
        group = computeLicenseWithRetry(resp, mail, activationCode, Long.parseLong(count), repoId, lang, ip, info);
      }
    }
    else {
      computeAnonymous(repoId, resp, ip, info);
    }
    ValueLongAccessor jarVersionAccessor = new ValueLongAccessor();
    ValueLongAccessor configVersionAccessor = new ValueLongAccessor();
    versionService.getVersion(mail, group, jarVersionAccessor, configVersionAccessor);
    long newJarVersion = jarVersionAccessor.getValue();
    resp.setHeader(LicenseConstants.HEADER_NEW_JAR_VERSION, Long.toString(newJarVersion));
    resp.setHeader(LicenseConstants.HEADER_NEW_CONFIG_VERSION, Long.toString(configVersionAccessor.getValue()));
    resp.setStatus(HttpServletResponse.SC_OK);
  }

  private void computeAnonymous(String id, HttpServletResponse resp, String ip, VersionService.JarInfo info) {
    try {
      computeAnonymous(id, ip, info);
    }
    catch (Exception e) {
      logger.error("RequestForConfigServlet : ", e);
      closeDb();
      initDb();
      try {
        computeAnonymous(id, ip, info);
      }
      catch (Exception e1) {
        logger.error("RequestForConfigServlet : Retry fail", e);
      }
    }
    finally {
      if (connection != null) {
        connection.commit();
      }
    }
    resp.setHeader(LicenseConstants.HEADER_IS_VALID, "false");
  }

  private void closeDb() {
    try {
      licenseRequest.close();
      updateAnonymousAccessCount.close();
      updateLastAccessRequest.close();
      updateNewActivationCodeRequest.close();
      repoIdAnonymousRequest.close();
      connection.commitAndClose();
    }
    catch (Exception e1) {
      try {
        connection.commitAndClose();
      }
      catch (Exception e2) {
      }
    }
  }

  static class RepoIdAnonymousRequest {
    private SqlSelect repoIdQuery;
    private ValueStringAccessor repoIdAccessor;

    RepoIdAnonymousRequest(SqlConnection db) {
      repoIdAccessor = new ValueStringAccessor();
      repoIdQuery = db.startSelect(RepoInfo.TYPE, Where.fieldEquals(RepoInfo.REPO_ID, repoIdAccessor))
        .selectAll()
        .getNotAutoCloseQuery();
    }

    public GlobList execute(String repoId) {
      repoIdAccessor.setValue(repoId);
      return repoIdQuery.getList();
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
    private final ValueLongAccessor jarVersion;
    private SqlRequest createAnonymousRequest;
    private ValueStringAccessor repoId;
    private ValueDateAccessor date;

    CreateAnonymousRequest(SqlConnection db) {
      repoId = new ValueStringAccessor();
      date = new ValueDateAccessor();
      jarVersion  = new ValueLongAccessor();
      createAnonymousRequest = db.startCreate(RepoInfo.TYPE)
        .set(RepoInfo.REPO_ID, repoId)
        .set(RepoInfo.LAST_ACCESS_DATE, date)
        .set(RepoInfo.COUNT, 1L)
        .set(RepoInfo.JAR_VERSION, jarVersion)
        .getRequest();
    }

    public void execute(String repoId, Date date, VersionService.JarInfo version) {
      this.repoId.setValue(repoId);
      this.date.setValue(date);
      if (version != null){
        this.jarVersion.setValue(version.getJarVersion());
      }
      else {
        this.jarVersion.setValue(0l);
      }
      createAnonymousRequest.execute();
    }
  }

  static class UpdateAnonymousAccessCount {
    private final ValueLongAccessor jarVersion;
    private ValueStringAccessor repoId;
    private ValueDateAccessor date;
    private ValueLongAccessor count;
    private SqlRequest request;

    UpdateAnonymousAccessCount(SqlConnection db) {
      repoId = new ValueStringAccessor();
      date = new ValueDateAccessor();
      count = new ValueLongAccessor();
      jarVersion = new ValueLongAccessor();
      request = db.startUpdate(RepoInfo.TYPE, Where.fieldEquals(RepoInfo.REPO_ID, repoId))
        .set(RepoInfo.LAST_ACCESS_DATE, date)
        .set(RepoInfo.COUNT, count)
        .set(RepoInfo.JAR_VERSION, jarVersion)
        .getRequest();
    }

    public void execute(String repoId, Date date, long count, VersionService.JarInfo info) {
      this.repoId.setValue(repoId);
      this.date.setValue(date);
      this.count.setValue(count);
      if (info != null){
        this.jarVersion.setValue(info.getJarVersion());
      }
      else {
        this.jarVersion.setValue(0l);
      }
      request.execute();
    }

    public void close() {
      request.close();
    }
  }

  private void computeAnonymous(String id, String ip, VersionService.JarInfo info) {
    GlobList globList = repoIdAnonymousRequest.execute(id);
    connection.commit();
    if (globList.size() == 0) {
      logInfo("new_anonymous ip = " + ip + " id =" + id + " on jar version = " + info);
      createAnonymousRequest.execute(id, new Date(), info);
      connection.commit();
    }
    else if (globList.size() > 1) {
      logger.error("compute_anonymous ip = " + ip + " id = " + id + " many repo with the same id");
    }
    if (globList.size() >= 1) {
      Long accessCount = globList.get(0).get(RepoInfo.COUNT) + 1;
      logInfo("known_anonymous ip = " + ip + " id =" + id + " access_count = " + accessCount);
      updateAnonymousAccessCount.execute(id, new Date(), accessCount, info);
      connection.commit();
    }
  }

  private Integer computeLicenseWithRetry(HttpServletResponse resp, String mail, String activationCode,
                                          Long count, String repoId, String lang, String ip, VersionService.JarInfo info) {
    logInfo("compute_license ip = " + ip + " mail = " + mail + " count = " + count +
            " id = " + repoId + " code = " + activationCode + " jar version = " + info);
    Integer group = null;
    try {
      group = computeLicense(resp, mail, activationCode, count, lang, ip, repoId, info);
    }
    catch (Exception e) {
      logger.error("RequestForConfigServlet:computeLicense", e);
      try {
        closeDb();
        initDb();
        group = computeLicense(resp, mail, activationCode, count, lang, ip, repoId, info);
      }
      catch (Exception ex) {
        logger.error("RequestForConfigServlet:computeLicense retry", e);
      }
      resp.setHeader(LicenseConstants.HEADER_IS_VALID, "true");
    }
    return group;
  }

  private void logInfo(String message) {
    logger.info("thread " + Thread.currentThread().getId() + " msg : " + message);
  }

  private Integer computeLicense(HttpServletResponse resp, String mail,
                                 String activationCode, Long count, String lang,
                                 String ip, String repoId, VersionService.JarInfo info) {
    GlobList globList = licenseRequest.execute(mail);
    connection.commit();
    if (globList.isEmpty()) {
      resp.setHeader(LicenseConstants.HEADER_IS_VALID, "false");
      resp.setHeader(LicenseConstants.HEADER_MAIL_UNKNOWN, "true");
      logInfo("unknown_mail mail = " + mail);
    }
    else {
      resp.setHeader(LicenseConstants.HEADER_IS_VALID, "true");
      for (Glob license : globList) {
        if (license.get(License.REPO_ID) != null && license.get(License.REPO_ID).equals(repoId)) {
          if (count < license.get(License.ACCESS_COUNT)) {
//            resp.setHeader(HttpBudgetViewConstants.HEADER_IS_VALIDE, "false");
            if (Utils.equal(activationCode, license.get(License.LAST_ACTIVATION_CODE))) {
//              String code = LicenseGenerator.generateActivationCode();
//              updateNewActivationCodeRequest.execute(mail, code);
//              db.commit();
//              resp.setHeader(HttpBudgetViewConstants.HEADER_MAIL_SENT, "true");
//              mailer.reSendExistingLicenseOnError(lang, code, mail);
              logInfo("Run_count_decrease_send_new_license_to mail = " + mail);
            }
            else {
              logInfo("Run_count_decrease_with_different_activation_code_for mail = " + mail);
            }
            return null;
          }
          else {
            if (Utils.equal(activationCode, license.get(License.LAST_ACTIVATION_CODE))) {
              updateLastAccessRequest.execute(license.get(License.ID), count, new Date(), info);
              connection.commit();
              logInfo("ok_for mail = " + mail + " count = " + count);
              return license.get(License.GROUP_ID);
            }
            else {
//              resp.setHeader(HttpBudgetViewConstants.HEADER_IS_VALIDE, "false");
//              resp.setHeader(HttpBudgetViewConstants.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_NOT_SENT, "true");
              logInfo("Different_code_for mail = " + mail);
              return null;
            }
          }
        }
      }
      logger.warn("For " + mail + " repo id not found ");
    }
    return null;
  }

  static class LicenseRequest {
    private ValueStringAccessor mail;
    private SqlSelect query;

    LicenseRequest(SqlConnection db) {
      mail = new ValueStringAccessor();
      query = db.startSelect(License.TYPE, Where.fieldEquals(License.MAIL, mail))
        .select(License.ACCESS_COUNT)
        .select(License.ACTIVATION_CODE)
        .select(License.REPO_ID)
        .select(License.LAST_ACCESS_DATE)
        .select(License.LAST_ACTIVATION_CODE)
        .select(License.GROUP_ID)
        .getNotAutoCloseQuery();
    }

    public GlobList execute(String mail) {
      this.mail.setValue(mail);
      return query.getList();
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
      request = db.startUpdate(License.TYPE, Where.fieldEquals(License.MAIL, mail))
        .set(License.ACTIVATION_CODE, activationCode)
        .getRequest();
    }

    public void execute(String mail, String activationCode) {
      this.mail.setValue(mail);
      this.activationCode.setValue(activationCode);
      request.execute();
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

    private ValueIntegerAccessor id;
    private SqlRequest request;
    private ValueLongAccessor count;

    private ValueDateAccessor date;
    private ValueLongAccessor timestamp;
    private final ValueLongAccessor jarVersion;

    UpdateLastAccessRequest(SqlConnection db) {
      id = new ValueIntegerAccessor();
      count = new ValueLongAccessor();
      this.date = new ValueDateAccessor();
      timestamp = new ValueLongAccessor();
      jarVersion = new ValueLongAccessor();
      request = db.startUpdate(License.TYPE, Where.fieldEquals(License.ID, id))
        .set(License.ACCESS_COUNT, count)
        .set(License.LAST_ACCESS_DATE, this.date)
        .set(License.TIME_STAMP, timestamp)
        .set(License.JAR_VERSION, jarVersion)
        .getRequest();
    }

    public void execute(int id, long count, Date date, VersionService.JarInfo jarInfo) {
      this.id.setValue(id);
      this.count.setValue(count);
      this.date.setValue(date);
      timestamp.setValue(System.currentTimeMillis());
      if (jarInfo != null){
        jarVersion.setValue(jarInfo.getJarVersion());
      }
      else {
        jarVersion.setValue(0l);
      }
      request.execute();
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