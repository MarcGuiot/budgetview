package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.SqlSelectBuilder;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.GlobAccessor;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StatementServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("/statement");

  private final GlobsDatabase database;
  private final AuthenticationService authentication;

  public StatementServlet(Directory directory) {
    this.database = directory.get(GlobsDatabase.class);
    this.authentication = directory.get(AuthenticationService.class);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    Integer userId = authentication.findUser(email);
    if (userId == null) {
      logger.error("Could not identify user with email:" + email);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
    writer.object();
    writer.key("accounts");
    writer.array();

    SqlConnection connection = database.connect();
    SqlSelectBuilder selectAccounts =
      connection.startSelect(ProviderAccount.TYPE, Where.fieldEquals(ProviderAccount.USER, userId));
    GlobAccessor account = selectAccounts.retrieveAll();
    SqlSelect accountQuery = selectAccounts.getQuery();
    GlobStream accountStream = accountQuery.getStream();
    while (accountStream.next()) {
       writeAccount(account, writer);
    }
    accountStream.close();
    accountQuery.close();

    writer.endArray();
    writer.endObject();
  }

  private void writeAccount(GlobAccessor account, JsonGlobWriter writer) {
    writer.object();
    writer.setCurrentValues(account);
    writer.field(ProviderAccount.ID, "id");
    writer.field(ProviderAccount.ACCOUNT_TYPE, "type");
    writer.field(ProviderAccount.PROVIDER, "provider");
    writer.field(ProviderAccount.PROVIDER_BANK_ID, "provider_bank_id");
    writer.field(ProviderAccount.PROVIDER_BANK_NAME, "provider_bank_name");
    writer.field(ProviderAccount.POSITION_MONTH, "position_month");
    writer.field(ProviderAccount.POSITION_DAY, "position_day");
    writer.field(ProviderAccount.NAME, "name");
    writer.field(ProviderAccount.NUMBER, "number");
    writer.endObject();
  }
}
