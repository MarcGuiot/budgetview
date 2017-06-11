package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.model.CloudInvoiceLog;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.CloudUserDevice;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Args;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobTreePrinter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Where;

import java.io.IOException;

public class ShowUser {
  public static void main(String... args) throws Exception {
    System.out.println(dump(args));
  }

  public static String dump(String... args) throws Exception {
    String configFile = Args.toString(args, 0);
    String email = Args.toEmail(args, 1);
    if (configFile == null || email == null) {
      return "Usage: script <config_file> <email>";
    }
    return print(configFile, Where.fieldEquals(CloudUser.EMAIL, email));
  }

  static String print(String configFile, Constraint where) throws IOException {
    ConfigService config = new ConfigService(configFile);
    GlobsDatabase database = CloudDb.create(config);
    SqlConnection connection = database.connect();
    GlobList users = connection.selectAll(CloudUser.TYPE, where);
    GlobTreePrinter writer = new GlobTreePrinter();
    for (Glob user : users) {
      writer.writeIndented("User",
                           user,
                           CloudUser.EMAIL,
                           CloudUser.ID,
                           CloudUser.EMAIL_VERIFIED,
                           CloudUser.LANG,
                           CloudUser.CREATION_DATE,
                           CloudUser.PROVIDER,
                           CloudUser.PROVIDER_USER_ID,
                           CloudUser.STRIPE_CUSTOMER_ID,
                           CloudUser.STRIPE_SUBSCRIPTION_ID,
                           CloudUser.LAST_STRIPE_INVOICE_EVENT_ID,
                           CloudUser.SUBSCRIPTION_END_DATE);

      writer.enter();
      Integer userId = user.get(CloudUser.ID);

      GlobList updates =
        connection.selectAll(ProviderUpdate.TYPE, Where.fieldEquals(ProviderUpdate.USER, userId))
          .sort(ProviderUpdate.ID);
      for (Glob update : updates) {
        writer.writeFlat("Update", update,
                         ProviderUpdate.ID,
                         ProviderUpdate.DATE,
                         ProviderUpdate.PROVIDER,
                         ProviderUpdate.PROVIDER_CONNECTION);
      }

      GlobList devices =
        connection.selectAll(CloudUserDevice.TYPE, Where.fieldEquals(CloudUserDevice.USER, userId))
          .sort(CloudUserDevice.LAST_UPDATE);
      for (Glob device: devices) {
        writer.writeFlat("Device", device,
                         CloudUserDevice.ID,
                         CloudUserDevice.LAST_UPDATE);
      }

      GlobList invoices =
        connection.selectAll(CloudInvoiceLog.TYPE, Where.fieldEquals(CloudInvoiceLog.USER, userId))
          .sort(CloudInvoiceLog.DATE);
      for (Glob invoice : invoices) {
        writer.writeFlat("Invoice", invoice,
                         CloudInvoiceLog.DATE,
                         CloudInvoiceLog.EMAIL,
                         CloudInvoiceLog.AMOUNT,
                         CloudInvoiceLog.RECEIPT_NUMBER,
                         CloudInvoiceLog.EMAIL_SENT);
      }

      writer.leave();
    }
    return writer.toString();
  }

}
