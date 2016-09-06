package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.persistence.CloudSerializer;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
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
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;

public class StatementServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("/statement");
  private Pattern pattern = Pattern.compile("/([0-9]+)/([0-9]+)");

  private final GlobsDatabase database;
  private final AuthenticationService authentication;
  private final CloudSerializer serializer;

  public StatementServlet(Directory directory) throws Exception {
    this.database = directory.get(GlobsDatabase.class);
    this.authentication = directory.get(AuthenticationService.class);
    this.serializer = new CloudSerializer(directory);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    logger.info("GET");

    String pathInfo = request.getPathInfo();
    Matcher matcher = pattern.matcher(pathInfo);
    if (!matcher.matches()) {
      logger.error("Missing or invalid parameters ==> " + pathInfo);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    int provider = Integer.parseInt(matcher.group(1));
    if (provider != Provider.BUDGEA.getId()) {
      logger.error("Unexpected provider: " + provider);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    int providerBankId = Integer.parseInt(matcher.group(2));

    String email = request.getHeader(CloudConstants.EMAIL);
    Integer userId = authentication.findUser(email);
    if (userId == null) {
      logger.error("Could not identify user with email:" + email);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }


    SqlConnection connection = database.connect();

    SqlSelectBuilder selectUpdates =
      connection.startSelect(ProviderUpdate.TYPE, Where.fieldEquals(ProviderUpdate.USER, userId));
    GlobAccessor accessor = selectUpdates.retrieveAll();
    SqlSelect query = selectUpdates.getQuery();
    GlobStream stream = query.getStream();

    JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
    writer.object();
    writer.key("accounts");
    writer.array();

    while (stream.next()) {

      GlobRepository repository = GlobRepositoryBuilder.createEmpty();
      try {
        byte[] bytes = accessor.get(ProviderUpdate.DATA);
        serializer.readBlob(bytes, repository);
      }
      catch (GeneralSecurityException e) {
        logger.error("Could not identify user with email: " + email);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }

      for (Glob account : repository.getAll(ProviderAccount.TYPE)) {
        writer.object();
        writeAccount(account, writer);

        writer.key("transactions");
        writer.array();
        for (Glob transaction : repository.findLinkedTo(account, ProviderTransaction.ACCOUNT)) {
          writeTransaction(transaction, writer);
        }
        writer.endArray(); // transactions

        writer.endObject(); // account
      }
    }

    writer.endArray(); // accounts
    writer.endObject();

    stream.close();
    query.close();

  }

  private void writeAccount(FieldValues account, JsonGlobWriter writer) {
    writer.setCurrentValues(account);
    writer.field(ProviderAccount.ID, "id");
    writer.field(ProviderAccount.ACCOUNT_TYPE, "type");
    writer.value(Provider.BUDGEA.getId(), "provider");
    writer.field(ProviderAccount.PROVIDER_BANK_ID, "provider_bank_id");
    writer.field(ProviderAccount.ID, "provider_account_id");
    writer.field(ProviderAccount.PROVIDER_BANK_NAME, "provider_bank_name");
    writer.field(ProviderAccount.POSITION, "position");
    writer.field(ProviderAccount.POSITION_MONTH, "position_month");
    writer.field(ProviderAccount.POSITION_DAY, "position_day");
    writer.field(ProviderAccount.NAME, "name");
    writer.field(ProviderAccount.NUMBER, "number");
  }

  private void writeTransaction(FieldValues transaction, JsonGlobWriter writer) {
    writer.object();
    writer.setCurrentValues(transaction);
    writer.field(ProviderTransaction.ID, "id");
    writer.value(Provider.BUDGEA.getId(), "provider");
    writer.field(ProviderTransaction.ID, "provider_id");
    writer.field(ProviderTransaction.LABEL, "label");
    writer.field(ProviderTransaction.ORIGINAL_LABEL, "original_label");
    writer.field(ProviderTransaction.AMOUNT, "amount");
    writer.field(ProviderTransaction.OPERATION_DATE, "operation_date");
    writer.field(ProviderTransaction.BANK_DATE, "bank_date");
    writer.field(ProviderTransaction.PROVIDER_CATEGORY_ID, "provider_category_id");
    writer.field(ProviderTransaction.PROVIDER_CATEGORY_NAME, "provider_category_name");
    writer.field(ProviderTransaction.DELETED, "deleted");
    writer.endObject();
  }
}
