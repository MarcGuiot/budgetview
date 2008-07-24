package org.designup.picsoulicence.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsoulicence.LicenceGenerator;
import org.designup.picsoulicence.model.Licence;
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
  static Logger logger = Logger.getLogger("register");
  private SqlService sqlService;

  public RegisterServlet(Directory directory) {
    sqlService = directory.get(SqlService.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mail = req.getHeader(ConfigService.HEADER_MAIL).trim();
    String activationCode = req.getHeader(ConfigService.HEADER_CODE).trim();
    logger.info("mail : '" + mail + "' code d'activation :'" + activationCode + "'");
    try {
      SqlConnection db = sqlService.getDb();
      SelectQuery query = db.getQueryBuilder(Licence.TYPE,
                                             Constraints.equal(Licence.MAIL, mail))
        .selectAll()
        .getQuery();
      GlobList globList = query.executeAsGlobs();
      db.commit();
      if (globList.isEmpty()) {
        resp.addHeader(ConfigService.HEADER_MAIL_UNKNOWN, "true");
      }
      else {
        Glob licence = globList.get(0);
        if (activationCode.equals(licence.get(Licence.ACTIVATION_CODE))) {
          byte[] signature = LicenceGenerator.generateSignature(mail);
          db.getUpdateBuilder(Licence.TYPE, Constraints.equal(Licence.MAIL, mail))
            .update(Licence.LAST_COUNT, 0L)
            .update(Licence.SIGNATURE, signature)
            .update(Licence.LAST_ACTIVATION_CODE, activationCode)
            .update(Licence.ACTIVATION_CODE, "")
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
  }
}