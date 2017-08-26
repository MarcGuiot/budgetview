package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.services.CloudSerializationService;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.SqlSelectBuilder;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.GlobAccessor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;
import static org.globsframework.sqlstreams.constraints.Where.fieldStrictlyGreaterThan;

public class StatementServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("StatementServlet");
  private Pattern pattern = Pattern.compile("/([0-9]+)");

  private final CloudSerializationService serializer;

  public StatementServlet(Directory directory) throws Exception {
    super(directory);
    serializer = directory.get(CloudSerializationService.class);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("GET");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        Integer lastUpdate = null;
        String pathInfo = request.getPathInfo();
        if (Strings.isNotEmpty(pathInfo)) {
          Matcher matcher = pattern.matcher(pathInfo);
          if (matcher.matches()) {
            lastUpdate = Integer.parseInt(matcher.group(1));
            logger.debug("Retrieving updates > " + lastUpdate);
          }
        }

        Integer userId = user.get(CloudUser.ID);
        SqlConnection connection = database.connect();
        Constraint where =
          lastUpdate == null ?
            Where.fieldEquals(ProviderUpdate.USER, userId) :
            Where.and(fieldEquals(ProviderUpdate.USER, userId),
                      fieldStrictlyGreaterThan(ProviderUpdate.ID, lastUpdate));

        SqlSelectBuilder selectUpdates =
          connection.startSelect(ProviderUpdate.TYPE, where)
            .orderBy(ProviderUpdate.DATE);
        GlobAccessor accessor = selectUpdates.retrieveAll();
        SqlSelect query = selectUpdates.getQuery();
        GlobStream stream = query.getStream();

        Integer maxId = 0;
        GlobRepository repository = GlobRepositoryBuilder.createEmpty();
        while (stream.next()) {
          try {
            int updateId = accessor.get(ProviderUpdate.ID);
            if (updateId > maxId) {
              maxId = updateId;
            }
            byte[] bytes = accessor.get(ProviderUpdate.DATA);
            serializer.readBlob(bytes, repository);
          }
          catch (Exception e) {
            logger.error("Failed to deserialize statement for user with email: " + user.get(CloudUser.EMAIL), e);
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
          }
        }

        writer.object();
        writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
        writer.key(CloudConstants.STATUS).value("ok");
        writer.key("last_update").value(maxId);

        writer.key("accounts");
        writer.array();

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

        writer.endArray(); // accounts
        writer.endObject();

        stream.close();
        query.close();
        connection.commitAndClose();

        return HttpServletResponse.SC_OK;
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
        writer.field(ProviderAccount.DELETED, "deleted");
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
        writer.field(ProviderTransaction.DEFAULT_SERIES_ID, "default_series_id");
        writer.field(ProviderTransaction.PROVIDER_CATEGORY_NAME, "provider_category_name");
        writer.field(ProviderTransaction.DELETED, "deleted");
        writer.endObject();
      }
    };
    command.run();
  }
}
