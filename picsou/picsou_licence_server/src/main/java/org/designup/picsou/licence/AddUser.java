package org.designup.picsou.licence;

import org.designup.picsou.licence.model.License;
import org.designup.picsou.licence.servlet.LicenceGenerator;
import org.designup.picsou.licence.servlet.LicenceServer;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;

public class AddUser {
  public static void main(String[] args) {
    String databaseUrl = System.getProperty(LicenceServer.DATABASE_URL);
    String user = System.getProperty(LicenceServer.DATABASE_USER);
    if (user == null) {
      user = "sa";
    }
    String passwd = System.getProperty(LicenceServer.DATABASE_PASSWD);
    if (passwd == null) {
      passwd = "";
    }
    SqlService sqlService = new JdbcSqlService(databaseUrl, user, passwd);
    SqlConnection db = sqlService.getDb();
    String code = LicenceGenerator.generateActivationCode();
    db.getCreateBuilder(License.TYPE)
      .set(License.MAIL, args[0])
      .set(License.ACTIVATION_CODE, code)
      .getRequest()
      .run();
    db.commitAndClose();
    System.out.println("code d'activation : " + code);
  }
}
