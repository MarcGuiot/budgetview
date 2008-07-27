package org.designup.picsoulicence.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsoulicence.LicenceGenerator;
import org.designup.picsoulicence.mail.Mailler;
import org.designup.picsoulicence.model.License;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.Encoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class RegisterServlet extends HttpServlet {
  static Logger logger = Logger.getLogger(ConfigService.REGISTER_SERVLET);
  private SqlService sqlService;
  private Mailler mailler;

  public RegisterServlet(Directory directory) {
    sqlService = directory.get(SqlService.class);
    mailler = directory.get(Mailler.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mail = req.getHeader(ConfigService.HEADER_MAIL).trim();
    String activationCode = req.getHeader(ConfigService.HEADER_CODE).trim();
    logger.info("mail : '" + mail + "' code d'activation :'" + activationCode + "'");
    SqlConnection db = null;
    try {
      db = sqlService.getDb();
      SelectQuery query = db.getQueryBuilder(License.TYPE,
                                             Constraints.equal(License.MAIL, mail))
        .selectAll()
        .getQuery();
      GlobList globList = query.executeAsGlobs();
      db.commit();
      if (globList.isEmpty()) {
        resp.addHeader(ConfigService.HEADER_MAIL_UNKNOWN, "true");
      }
      else {
        Glob license = globList.get(0);
        if (activationCode.equals(license.get(License.ACTIVATION_CODE))) {
          byte[] signature = LicenceGenerator.generateSignature(mail);
          db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
            .update(License.LAST_COUNT, 1L)
            .update(License.SIGNATURE, signature)
            .update(License.LAST_ACTIVATION_CODE, activationCode)
            .getRequest()
            .run();
          db.commit();
          resp.addHeader(ConfigService.HEADER_SIGNATURE, Encoder.b64Decode(signature));
        }
        else {
          resp.addHeader(ConfigService.HEADER_ACTIVATION_CODE_NOT_VALIDE, "true");
        }
      }
    }
    catch (Exception e) {
      logger.throwing("AskForMailServlet", "doPost", e);
      resp.addHeader(ConfigService.HEADER_IS_VALIDE, "true");
    }
    finally {
      if (db != null) {
        db.commitAndClose();
      }

    }
  }
}