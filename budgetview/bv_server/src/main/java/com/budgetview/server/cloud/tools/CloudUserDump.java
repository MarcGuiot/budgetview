package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.model.CloudInvoiceLog;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.CloudUserDevice;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.server.config.ConfigService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobTreePrinter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Where;

public class CloudUserDump {

  public static CloudUserDump get(String configFile, Constraint where) throws Exception {
    return new CloudUserDump(configFile, where);
  }

  private boolean userFound = false;
  private String content;

  private CloudUserDump(String configFile, Constraint where) throws Exception {
    ConfigService config = new ConfigService(configFile);
    GlobsDatabase database = CloudDb.create(config);
    SqlConnection connection = database.connect();
    GlobList users = connection.selectAll(CloudUser.TYPE, where);
    if (users.isEmpty()) {
      content = "User not found";
      return;
    }

    userFound = true;
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
          .sort(CloudUserDevice.LAST_SEEN);
      for (Glob device: devices) {
        writer.writeFlat("Device", device,
                         CloudUserDevice.ID,
                         CloudUserDevice.LAST_SEEN);
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
    content = writer.toString();
  }

  public boolean userFound() {
    return userFound;
  }

  public String toString() {
    return content;
  }
}
