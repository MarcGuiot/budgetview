package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.*;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSqlException;
import org.globsframework.utils.directory.Directory;

import java.io.IOException;

public class UserService {

  private GlobsDatabase database;
  private Directory directory;

  public interface DeletionCallback {
    void processOk();
    void processError(String message, Exception e);
  }

  public UserService(Directory directory) {
    this.directory = directory;
    this.database = directory.get(GlobsDatabase.class);
  }

  public boolean deleteUser(Glob user, DeletionCallback callback)  {

    Integer userId = user.get(CloudUser.ID);

    Integer providerUserId = user.get(CloudUser.PROVIDER_USER_ID);
    if (providerUserId != null) {
      try {
        BudgeaAPI budgeaAPI = new BudgeaAPI();
        budgeaAPI.setToken(user.get(CloudUser.PROVIDER_ACCESS_TOKEN));
        budgeaAPI.deleteUser(providerUserId);
      }
      catch (IOException e) {
        callback.processError("Failed to delete Budgea user " + userId + " with provider user ID " + providerUserId, e);
      }
    }

    String stripeCustomerId = user.get(CloudUser.STRIPE_CUSTOMER_ID);
    if (stripeCustomerId != null) {
      String stripeSubscriptionId = user.get(CloudUser.STRIPE_SUBSCRIPTION_ID);
      try {
        directory.get(PaymentService.class).deleteSubscription(stripeCustomerId, stripeSubscriptionId);
      }
      catch (Exception e) {
        callback.processError("Failed to delete Stripe customer " + stripeCustomerId + " with subscription " + stripeSubscriptionId + " (userId: " + userId + ")", e);
      }
    }

    SqlConnection connection = database.connect();
    try {
      connection
        .startDelete(ProviderUpdate.TYPE, Where.fieldEquals(ProviderUpdate.USER, userId))
        .execute();
      connection
        .startDelete(ProviderConnection.TYPE, Where.fieldEquals(ProviderConnection.USER, userId))
        .execute();
      connection
        .startDelete(CloudUserDevice.TYPE, Where.fieldEquals(CloudUserDevice.USER, userId))
        .execute();
      connection
        .startDelete(CloudEmailValidation.TYPE, Where.fieldEquals(CloudEmailValidation.USER, userId))
        .execute();
      connection
        .startDelete(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
        .execute();
    }
    catch (GlobsSqlException e) {
      callback.processError("Failed to delete user " + userId + " with provider user ID " + providerUserId + " from database", e);
      return false;
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (Exception e) {
        callback.processError("Commit failed when deleting user: " + userId, e);
      }
    }

    callback.processOk();
    return true;
  }}
