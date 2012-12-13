package org.designup.picsou.license.checkers;

import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.designup.picsou.license.model.RepoInfo;
import org.designup.picsou.license.model.SoftwareInfo;
import org.globsframework.metamodel.Field;
import org.globsframework.model.utils.EmptyGlobList;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.utils.Dates;
import static junit.framework.Assert.*;

import java.util.Date;

public class DbChecker {

  private static final String DATABASE_URL = "jdbc:hsqldb:.";

  private JdbcSqlService sqlService;

  public DbChecker() {
    SqlConnection connection = getConnection();
    connection.createTable(License.TYPE, RepoInfo.TYPE, MailError.TYPE, SoftwareInfo.TYPE);
    connection.emptyTable(License.TYPE, RepoInfo.TYPE, MailError.TYPE, SoftwareInfo.TYPE);
    connection.commitAndClose();
  }

  public SqlConnection getConnection() {
    if (sqlService == null) {
      sqlService = new JdbcSqlService(DATABASE_URL, "sa", "");
    }
    return sqlService.getDb();
  }

  public void start() {
  }

  public void dispose() {
    sqlService = null;
  }

  public void registerMail(String email, String code) {
    SqlConnection connection = getConnection();
    connection.getCreateBuilder(License.TYPE)
      .set(License.EMAIL, email)
      .set(License.ACTIVATION_CODE, code)
      .getRequest()
      .run();
    connection.commit();
  }

  private Glob getGlob(Field field, Object expected,
                       Constraint constraint) throws InterruptedException {
    long end = System.currentTimeMillis() + 3000;
    GlobList glob = new EmptyGlobList();
    SqlConnection connection = getConnection();
    while (end > System.currentTimeMillis()) {
      glob = connection.getQueryBuilder(field.getGlobType(), constraint)
        .selectAll()
        .getQuery().executeAsGlobs();
      connection.commit();
      if (glob.size() == 1) {
        Object actual = glob.get(0).getValue(field);
        if (actual != null && (expected == null || actual.equals(expected))) {
          break;
        }
      }
      Thread.sleep(50);
    }
    assertEquals(1, glob.size());
    return glob.get(0);
  }

  public Glob getLicense(String email, Field field, Object expected) throws InterruptedException {
    return getGlob(field, expected, Constraints.equal(License.EMAIL, email));
  }

  public String checkRepoIdIsUpdated(long repoCount, Constraint constraint) throws InterruptedException {
    Glob repoInfo = getGlob(RepoInfo.COUNT, repoCount, constraint);
    Date target = repoInfo.get(RepoInfo.LAST_ACCESS_DATE);
    assertTrue(Dates.isNear(new Date(), target, 10000));
    assertEquals(repoCount, repoInfo.get(RepoInfo.COUNT).longValue());
    return repoInfo.get(RepoInfo.REPO_ID);
  }

  public void checkLicenseCount(String email, long count) throws InterruptedException {
    Glob license = getLicense(email, License.ACCESS_COUNT, count);
    assertEquals(count, license.get(License.ACCESS_COUNT).longValue());
    assertTrue(license.get(License.SIGNATURE).length > 1);
  }

  public String getUrl() {
    return DATABASE_URL;
  }

}
