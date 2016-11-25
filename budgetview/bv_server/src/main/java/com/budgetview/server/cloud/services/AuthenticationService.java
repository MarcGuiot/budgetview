package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.CloudUserDevice;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.server.cloud.utils.RandomStrings;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.shared.license.LicenseAPI;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlCreateRequest;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.DbConstraintViolation;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.sqlstreams.exceptions.RollbackFailed;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

import java.util.Date;

import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;
import static org.globsframework.utils.Dates.now;

public class AuthenticationService {

  private static Logger logger = Logger.getLogger("AuthenticationService");

  private GlobsDatabase database;
  private RandomStrings randomStrings = new RandomStrings();

  public AuthenticationService(Directory directory) {
    this.database = directory.get(GlobsDatabase.class);
  }

  public Integer findUser(String email) throws GlobsSQLException {
    String lowerCaseEmail = email.toLowerCase();
    SqlConnection connection = database.connect();
    Integer userId = null;
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, fieldEquals(CloudUser.EMAIL, lowerCaseEmail));
      userId = user.get(CloudUser.ID);
    }
    catch (ItemNotFound itemNotFound) {
      userId = null;
    }
    catch (TooManyItems tooManyItems) {
      logger.error("Several entries found for user: " + lowerCaseEmail);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (RollbackFailed rollbackFailed) {
        logger.error("Commit failed when looking for user: " + email, rollbackFailed);
      }
      catch (DbConstraintViolation constraintViolation) {
        logger.error("Commit failed when looking for user: " + email, constraintViolation);
      }
    }
    return userId;
  }

  public Integer createUser(String email) throws GlobsSQLException {
    String lowerCaseEmail = email.toLowerCase();
    SqlConnection connection = database.connect();
    try {
      SqlCreateRequest request = connection.startCreate(CloudUser.TYPE)
        .set(CloudUser.EMAIL, lowerCaseEmail)
        .set(CloudUser.EMAIL_VERIFIED, false)
        .getRequest();
      request.execute();
      return request.getLastGeneratedIds().get(CloudUser.ID);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (RollbackFailed rollbackFailed) {
        logger.error("Commit failed when looking for user: " + email, rollbackFailed);
      }
      catch (DbConstraintViolation constraintViolation) {
        logger.error("Commit failed when looking for user: " + email, constraintViolation);
      }
    }
  }

  public String registerUserDevice(int userId) {
    String newToken = randomStrings.next(16);

    SqlConnection connection = database.connect();
    try {
      SqlCreateRequest request = connection.startCreate(CloudUserDevice.TYPE)
        .set(CloudUserDevice.USER_ID, userId)
        .set(CloudUserDevice.TOKEN, newToken)
        .set(CloudUserDevice.LAST_UPDATE, now())
        .getRequest();
      request.execute();
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (RollbackFailed rollbackFailed) {
        logger.error("Commit failed when registering device for user: " + userId, rollbackFailed);
      }
      catch (DbConstraintViolation constraintViolation) {
        logger.error("Commit failed when registering device for user: " + userId, constraintViolation);
      }
    }

    return newToken;
  }

  public Integer checkUserToken(String email, String token) throws SubscriptionCheckFailed {
    SqlConnection connection = database.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, fieldEquals(CloudUser.EMAIL, email.toLowerCase()));
      if (user == null) {
        return null;
      }

      Integer userId = user.get(CloudUser.ID);
      GlobList usersWithToken = connection.selectAll(CloudUser.TYPE, Where.and(fieldEquals(CloudUserDevice.USER_ID, userId),
                                                                               fieldEquals(CloudUserDevice.TOKEN, token)));
      if (usersWithToken.size() != 1) {
        return null;
      }

      doCheckSubscription(user, userId, email, connection);

      return userId;
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (RollbackFailed rollbackFailed) {
        logger.error("Commit failed when looking for user: " + email, rollbackFailed);
      }
      catch (DbConstraintViolation constraintViolation) {
        logger.error("Commit failed when looking for user: " + email, constraintViolation);
      }
    }
  }

  public void checkSubscriptionIsValid(Integer userId) throws SubscriptionCheckFailed {
    SqlConnection connection = database.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, fieldEquals(CloudUser.ID, userId));
      if (user == null) {
        throw new SubscriptionCheckFailed(CloudSubscriptionStatus.UNKNOWN);
      }
      doCheckSubscription(user, userId, user.get(CloudUser.EMAIL), connection);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (RollbackFailed rollbackFailed) {
        logger.error("Commit failed when looking for user: " + userId, rollbackFailed);
      }
      catch (DbConstraintViolation constraintViolation) {
        logger.error("Commit failed when looking for user: " + userId, constraintViolation);
      }
    }
  }

  private void doCheckSubscription(Glob user, Integer userId, String email, SqlConnection connection) throws SubscriptionCheckFailed {
    Date endDate = user.get(CloudUser.SUBSCRIPTION_END_DATE);
    if (endDate == null || now().after(endDate)) {
      try {
        endDate = LicenseAPI.getCloudSubscriptionEndDate(email);
      }
      catch (Exception e) {
        logger.error("Failed to retrieve subscription end date", e);
      }
      if (endDate != null) {
        connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
          .set(CloudUser.SUBSCRIPTION_END_DATE, endDate)
          .run();
      }
    }

    logger.info("Subscription end date is " + Dates.toString(endDate) + " for " + email);

    if (endDate == null) {
      throw new SubscriptionCheckFailed(CloudSubscriptionStatus.UNKNOWN);
    }
    if (now().after(endDate)) {
      throw new SubscriptionCheckFailed(CloudSubscriptionStatus.EXPIRED);
    }
  }

  public void invalidateUserEmail(int userId) throws GlobsSQLException {
    SqlConnection connection = database.connect();
    try {
      connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
        .set(CloudUser.EMAIL_VERIFIED, false)
        .run();
    }
    catch (ItemNotFound itemNotFound) {
      logger.error("User not found: " + userId);
    }
    catch (TooManyItems tooManyItems) {
      logger.error("Several entries found for user: " + userId);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (RollbackFailed rollbackFailed) {
        logger.error("Commit failed when looking for user: " + userId, rollbackFailed);
      }
      catch (DbConstraintViolation constraintViolation) {
        logger.error("Commit failed when looking for user: " + userId, constraintViolation);
      }
    }
  }
}
