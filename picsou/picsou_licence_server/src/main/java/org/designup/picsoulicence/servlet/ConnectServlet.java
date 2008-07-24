package org.designup.picsoulicence.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsoulicence.model.Licence;
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
import java.util.logging.Logger;

public class ConnectServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("connect");
  private SqlService sqlService;

  public ConnectServlet(Directory directory) {
    sqlService = directory.get(SqlService.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mail = req.getHeader(ConfigService.HEADER_MAIL).trim();
    String activationCode = req.getHeader(ConfigService.HEADER_CODE).trim();
    Long count = Long.parseLong(req.getHeader(ConfigService.HEADER_COUNT).trim());
    logger.info("mail : '" + mail + "' code d'activation :'" + activationCode + "' count :'" + count + "'");
    try {
      SqlConnection db = sqlService.getDb();
      SelectQuery query = db.getQueryBuilder(Licence.TYPE, Constraints.equal(Licence.MAIL, mail))
        .selectAll()
        .getQuery();
      GlobList globList = query.executeAsGlobs();
      if (globList.isEmpty()) {
        resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
      }
      else {
        if (count < globList.get(0).get(Licence.LAST_COUNT)) {
          resp.addHeader(ConfigService.HEADER_IS_VALIDE, "false");
          resp.addHeader(ConfigService.HEADER_MAIL_SENT, "true");
        }
        else {
          resp.addHeader(ConfigService.HEADER_IS_VALIDE, "true");
          db.getUpdateBuilder(Licence.TYPE, Constraints.equal(Licence.MAIL, mail))
            .update(Licence.LAST_COUNT, count)
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