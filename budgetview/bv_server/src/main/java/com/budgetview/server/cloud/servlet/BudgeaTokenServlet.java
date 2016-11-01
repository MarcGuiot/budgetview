package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.persistence.CloudSerializer;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;
import static org.globsframework.sqlstreams.constraints.Where.fieldStrictlyGreaterThan;

public class BudgeaTokenServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("/budgea/token");

  private final GlobsDatabase database;
  private final AuthenticationService authentication;
  private final CloudSerializer serializer;

  public BudgeaTokenServlet(Directory directory) throws Exception {
    this.database = directory.get(GlobsDatabase.class);
    this.authentication = directory.get(AuthenticationService.class);
    this.serializer = new CloudSerializer(directory);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    logger.info("GET");

    String email = request.getHeader(CloudConstants.EMAIL);
    String token = request.getHeader(CloudConstants.BV_TOKEN);
    if (Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(token)) {
      logger.info("Missing info " + email + " / " + token);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Integer userId = authentication.checkUserToken(email, token);
    if (userId == null) {
      logger.error("Could not identify user with email:" + email);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }


    SqlConnection connection = database.connect();
    Glob user = connection.selectUnique(CloudUser.TYPE, fieldEquals(CloudUser.ID, userId));
    String permanentBudgeaToken = user.get(CloudUser.PROVIDER_ACCESS_TOKEN);

    String temporaryToken;
    if (Strings.isNotEmpty(permanentBudgeaToken)) {
      temporaryToken = BudgeaAPI.requestTemporaryToken(permanentBudgeaToken);
    }
    else {
      temporaryToken = BudgeaAPI.requestFirstTemporaryToken();
    }

    JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
    writer.object();
    writer.key(CloudConstants.BUDGEA_TOKEN);
    writer.value(temporaryToken);
    writer.key(CloudConstants.BUDGEA_TOKEN_REGISTERED);
    writer.value(Strings.isNotEmpty(permanentBudgeaToken));
    writer.endObject();

    response.setStatus(HttpServletResponse.SC_OK);

  }
}
