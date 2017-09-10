package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderConnection;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.GlobsException;

import javax.mail.MessagingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;

public class WebhookNotificationService {

  private static Logger logger = Logger.getLogger("WebhookNotificationService");

  private GlobsDatabase database;
  private final Mailer mailer;

  public WebhookNotificationService(Directory directory) {
    this.database = directory.get(GlobsDatabase.class);
    this.mailer = directory.get(Mailer.class);
  }

  public Notifications start() {
    return new Notifications();
  }

  public class Notifications {
    private List<ConnectionInfo> infoList = new ArrayList<ConnectionInfo>();

    public void addConnection(int providerConnectionId, String bankName, boolean containsAccounts, boolean passwordError, boolean actionNeeded) {
      infoList.add(new ConnectionInfo(providerConnectionId, bankName, containsAccounts, passwordError, actionNeeded));
    }

    public void send(Glob user) {

      if (user == null) {
        logger.error("Cannot send notification for null user");
        throw new InvalidParameterException("User is null");
      }

      SqlConnection sqlConnection = database.connect();
      boolean newlyInitializedConnections = false;
      try {
        for (ConnectionInfo connectionInfo : infoList) {
          GlobList items =
            sqlConnection.startSelect(ProviderConnection.TYPE,
                                      Where.and(fieldEquals(ProviderConnection.USER, user.get(CloudUser.ID)),
                                                fieldEquals(ProviderConnection.PROVIDER, Provider.BUDGEA.getId()),
                                                fieldEquals(ProviderConnection.PROVIDER_CONNECTION, connectionInfo.providerConnectionId)))
              .selectAll()
              .getList();

          if (!items.isEmpty()) {
            for (Glob item : items) {
              if (!item.isTrue(ProviderConnection.INITIALIZED) && connectionInfo.containsAccounts) {
                newlyInitializedConnections = true;
                sqlConnection.startUpdate(ProviderConnection.TYPE, Where.globEquals(item))
                  .set(ProviderConnection.INITIALIZED, true)
                  .run();
              }
              sqlConnection.startUpdate(ProviderConnection.TYPE, Where.globEquals(item))
                .set(ProviderConnection.PASSWORD_ERROR, connectionInfo.passwordError)
                .set(ProviderConnection.ACTION_NEEDED, connectionInfo.actionNeeded)
                .run();
            }
          }
          else {
            newlyInitializedConnections |= connectionInfo.containsAccounts;
            sqlConnection.startCreate(ProviderConnection.TYPE)
              .set(ProviderConnection.USER, user.get(CloudUser.ID))
              .set(ProviderConnection.PROVIDER_CONNECTION, connectionInfo.providerConnectionId)
              .set(ProviderConnection.INITIALIZED, connectionInfo.containsAccounts)
              .set(ProviderConnection.PASSWORD_ERROR, connectionInfo.passwordError)
              .set(ProviderConnection.ACTION_NEEDED, connectionInfo.actionNeeded)
              .run();
          }
        }
        sqlConnection.commitAndClose();

        if (newlyInitializedConnections) {
          boolean sent = mailer.sendCloudWebhookNotification(user.get(CloudUser.EMAIL), user.get(CloudUser.LANG));
          logger.debug("Webhook notification email " + (sent ? "sent" : "planned for sending") + " to: " + user.get(CloudUser.EMAIL));
        }
        for (ConnectionInfo connectionInfo : infoList) {
          if (connectionInfo.passwordError) {
            boolean sent = mailer.sendCloudBankPasswordError(user.get(CloudUser.EMAIL), user.get(CloudUser.LANG), connectionInfo.bankName);
            logger.debug("Webhook password error email " + (sent ? "sent" : "planned for sending") + " to: " + user.get(CloudUser.EMAIL) + " for bank: " + connectionInfo.bankName);
          }
          if (connectionInfo.actionNeeded) {
            boolean sent = mailer.sendCloudActionNeeded(user.get(CloudUser.EMAIL), user.get(CloudUser.LANG), connectionInfo.bankName);
            logger.debug("Webhook action needd email " + (sent ? "sent" : "planned for sending") + " to: " + user.get(CloudUser.EMAIL) + " for bank: " + connectionInfo.bankName);
          }
        }
      }
      catch (GlobsException e) {
        logger.error("Database error raised while processing webhook for user " + user.get(CloudUser.ID) + " with email: " + user.get(CloudUser.EMAIL));
        sqlConnection.rollbackAndClose();
      }
      catch (MessagingException e) {
        logger.error("Could not send notification to user " + user.get(CloudUser.ID) + " with email: " + user.get(CloudUser.EMAIL));
        sqlConnection.rollbackAndClose();
      }
      catch (Exception e) {
        logger.error("Could not send notification to user " + user.get(CloudUser.ID) + " with email: " + user.get(CloudUser.EMAIL));
        sqlConnection.rollbackAndClose();
      }
    }
  }

  private static class ConnectionInfo {
    public final int providerConnectionId;
    public final String bankName;
    public final boolean containsAccounts;
    public final boolean passwordError;
    public final boolean actionNeeded;

    public ConnectionInfo(int providerConnectionId, String bankName, boolean containsAccounts, boolean passwordError, boolean actionNeeded) {
      this.providerConnectionId = providerConnectionId;
      this.bankName = bankName;
      this.containsAccounts = containsAccounts;
      this.passwordError = passwordError;
      this.actionNeeded = actionNeeded;
    }
  }
}
