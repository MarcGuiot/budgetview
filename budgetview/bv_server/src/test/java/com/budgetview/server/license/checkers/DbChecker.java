package com.budgetview.server.license.checkers;

import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.MailError;
import com.budgetview.server.license.model.RepoInfo;
import com.budgetview.server.license.model.SoftwareInfo;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.EmptyGlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;
import org.globsframework.utils.Dates;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class DbChecker {

  public static final String DATABASE_URL = "jdbc:hsqldb:.";
  public static final String DATABASE_USER = "sa";
  public static final String DATABASE_PASSWORD = "";

  private JdbcGlobsDatabase globsDatabase;

  public DbChecker() {
    SqlConnection connection = getConnection();
    connection.createTables(License.TYPE, RepoInfo.TYPE, MailError.TYPE, SoftwareInfo.TYPE);
    connection.emptyTable(License.TYPE, RepoInfo.TYPE, MailError.TYPE, SoftwareInfo.TYPE);
    connection.commitAndClose();
  }

  public SqlConnection getConnection() {
    if (globsDatabase == null) {
      globsDatabase = new JdbcGlobsDatabase(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    }
    return globsDatabase.connect();
  }

  public void start() {
  }

  public void dispose() {
    globsDatabase = null;
  }

  public void registerMail(String email, String code) {
    SqlConnection connection = getConnection();
    connection.startCreate(License.TYPE)
      .set(License.MAIL, email)
      .set(License.ACTIVATION_CODE, code)
      .run();
    connection.commit();
  }

  private Glob getGlob(Field field, Object expected, Constraint constraint) throws InterruptedException {
    long end = System.currentTimeMillis() + 3000;
    GlobList globsList = new EmptyGlobList();
    SqlConnection connection = getConnection();
    while (end > System.currentTimeMillis()) {
      globsList = connection.selectAll(field.getGlobType(), constraint);
      connection.commit();
      if (globsList.size() == 1) {
        Object actual = globsList.get(0).getValue(field);
        if (actual != null && (expected == null || actual.equals(expected))) {
          break;
        }
      }
      Thread.sleep(50);
    }
    assertEquals(1, globsList.size());
    return globsList.get(0);
  }

  public Glob getLicense(String email, Field field, Object expected) throws InterruptedException {
    return getGlob(field, expected, Where.fieldEquals(License.MAIL, email));
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
}
