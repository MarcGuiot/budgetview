package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderConnection;
import com.budgetview.server.license.mail.Mailer;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.directory.Directory;

import javax.mail.MessagingException;
import java.util.Set;

import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;

public class WebhookNotificationService {

  private static Logger logger = Logger.getLogger("WebhookNotificationService");

  private GlobsDatabase database;
  private final Mailer mailer;

  public WebhookNotificationService(Directory directory) {
    this.database = directory.get(GlobsDatabase.class);
    this.mailer = directory.get(Mailer.class);
  }

  public void send(Glob user, Set<Integer> connectionIds) {

    SqlConnection sqlConnection = database.connect();
    boolean notificationNeeded = false;
    try {
      for (Integer connectionId : connectionIds) {
        GlobList items =
          sqlConnection.startSelect(ProviderConnection.TYPE,
                                    Where.and(fieldEquals(ProviderConnection.USER, user.get(CloudUser.ID)),
                                              fieldEquals(ProviderConnection.PROVIDER_CONNECTION_ID, connectionId)))
            .selectAll()
            .getList();

        if (!items.isEmpty()) {
          for (Glob item : items) {
            if (!item.isTrue(ProviderConnection.INITIALIZED)) {
              notificationNeeded = true;
              sqlConnection.startUpdate(ProviderConnection.TYPE,
                                        Where.fieldEquals(ProviderConnection.ID, item.get(ProviderConnection.ID)))
                .set(ProviderConnection.INITIALIZED, true)
                .run();
            }
          }
        }
        else {
          notificationNeeded = true;
          sqlConnection.startCreate(ProviderConnection.TYPE)
            .set(ProviderConnection.USER, user.get(CloudUser.ID))
            .set(ProviderConnection.PROVIDER_CONNECTION_ID, connectionId)
            .set(ProviderConnection.INITIALIZED, true)
            .run();
        }
      }
      sqlConnection.commitAndClose();

      database.connect();

      if (notificationNeeded) {
        mailer.sendCloudWebhookNotification(user.get(CloudUser.EMAIL), user.get(CloudUser.LANG));
        logger.info("Webhook notification email sent to: " + user.get(CloudUser.EMAIL));
      }
    }
    catch (MessagingException e) {
      logger.error("Could not send notification to user " + user.get(CloudUser.ID) + " with email: " + user.get(CloudUser.EMAIL));
      sqlConnection.rollbackAndClose();
    }
  }
}
