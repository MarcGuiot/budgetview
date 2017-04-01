package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.CloudUserDevice;
import com.budgetview.server.cloud.utils.RandomStrings;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import org.apache.log4j.Logger;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.*;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Strings;
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

  public Glob findUser(String email) throws GlobsSQLException {
    String lowerCaseEmail = email.toLowerCase();
    SqlConnection connection = database.connect();
    try {
      return connection.selectUnique(CloudUser.TYPE, fieldEquals(CloudUser.EMAIL, lowerCaseEmail));
    }
    catch (ItemNotFound itemNotFound) {
      return null;
    }
    catch (TooManyItems tooManyItems) {
      logger.error("Several entries found for user: " + lowerCaseEmail);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when trying to find user: " + email, e);
      }
    }
    return null;
  }

  public Integer createUser(String email, String lang) throws GlobsSQLException {
    String lowerCaseEmail = email.toLowerCase();
    SqlConnection connection = database.connect();
    try {
      SqlCreateRequest request = connection.startCreate(CloudUser.TYPE)
        .set(CloudUser.EMAIL, lowerCaseEmail)
        .set(CloudUser.LANG, lang)
        .set(CloudUser.CREATION_DATE, new Date())
        .getRequest();
      request.execute();
      return request.getLastGeneratedIds().get(CloudUser.ID);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when looking for user: " + email, e);
      }
    }
  }

  public Glob findOrCreateUser(String email, String lang, FieldValue... values) {
    String lowerCaseEmail = email.toLowerCase();
    SqlConnection connection = database.connect();
    try {
      GlobList users = connection.selectAll(CloudUser.TYPE, fieldEquals(CloudUser.EMAIL, lowerCaseEmail));
      if (users.isEmpty()) {
        SqlCreateBuilder builder = connection.startCreate(CloudUser.TYPE)
          .set(CloudUser.EMAIL, lowerCaseEmail)
          .set(CloudUser.LANG, lang)
          .set(CloudUser.CREATION_DATE, new Date());
        for (FieldValue value : values) {
          builder.setValue(value.getField(), value.getValue());
        }
        SqlCreateRequest request = builder.getRequest();
        request.execute();
        Integer userId = request.getLastGeneratedIds().get(CloudUser.ID);
        return connection.selectUnique(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId));
      }
      else if (users.size() >= 1) {
        if (values.length > 0) {
          SqlUpdateBuilder builder =
            connection.startUpdate(CloudUser.TYPE, fieldEquals(CloudUser.EMAIL, lowerCaseEmail));
          for (FieldValue value : values) {
            builder.setValue(value.getField(), value.getValue());
          }
          builder.getRequest().execute();
        }
        return users.getFirst();
      }
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when trying to find user: " + email, e);
      }
    }
    return null;
  }


  public String registerUserDevice(int userId) {
    String newToken = randomStrings.next(16);

    SqlConnection connection = database.connect();
    try {
      SqlCreateRequest request = connection.startCreate(CloudUserDevice.TYPE)
        .set(CloudUserDevice.USER, userId)
        .set(CloudUserDevice.TOKEN, newToken)
        .set(CloudUserDevice.LAST_UPDATE, now())
        .getRequest();
      request.execute();
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when registering device for user: " + userId, e);
      }
    }

    return newToken;
  }

  public Glob findUserAndToken(String email, String token) {
    SqlConnection connection = database.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, fieldEquals(CloudUser.EMAIL, email.toLowerCase()));
      if (user == null) {
        return null;
      }

      Integer userId = user.get(CloudUser.ID);
      GlobList usersWithToken = connection.selectAll(CloudUserDevice.TYPE,
                                                     Where.and(fieldEquals(CloudUserDevice.USER, userId),
                                                               fieldEquals(CloudUserDevice.TOKEN, token)));
      if (usersWithToken.size() != 1) {
        return null;
      }

      return user;
    }
    catch (ItemNotFound e) {
      return null;
    }
    catch (TooManyItems e) {
      logger.error("Too many user token entries for: " + email);
      return null;
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when trying to find user and token for: " + email, e);
      }
    }
  }

  public Glob checkUserToken(String email, String token) throws SubscriptionCheckFailed {
    SqlConnection connection = database.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, fieldEquals(CloudUser.EMAIL, email.toLowerCase()));
      if (user == null) {
        return null;
      }

      Integer userId = user.get(CloudUser.ID);
      GlobList usersWithToken = connection.selectAll(CloudUserDevice.TYPE,
                                                     Where.and(fieldEquals(CloudUserDevice.USER, userId),
                                                               fieldEquals(CloudUserDevice.TOKEN, token)));
      if (usersWithToken.size() != 1) {
        return null;
      }

      connection.startUpdate(CloudUserDevice.TYPE, Where.globEquals(usersWithToken.getFirst()))
        .set(CloudUserDevice.LAST_UPDATE, new Date())
        .getRequest()
        .execute();

      checkSubscriptionEndDate(user, userId, email, connection);

      return user;
    }
    catch (ItemNotFound e) {
      throw new SubscriptionCheckFailed(CloudSubscriptionStatus.NO_SUBSCRIPTION);
    }
    catch (TooManyItems e) {
      logger.error("Too many user token entries for: " + email);
      throw new SubscriptionCheckFailed(CloudSubscriptionStatus.NO_SUBSCRIPTION);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when looking for user: " + email, e);
      }
    }
  }

  public boolean isSubscriptionValid(Glob user) {
    Date subscriptionEndDate = user.get(CloudUser.SUBSCRIPTION_END_DATE);
    return Strings.isNotEmpty(user.get(CloudUser.STRIPE_CUSTOMER_ID)) &&
           subscriptionEndDate != null &&
           now().after(subscriptionEndDate);
  }

  public void checkSubscriptionIsValid(Integer userId) throws SubscriptionCheckFailed {
    SqlConnection connection = database.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, fieldEquals(CloudUser.ID, userId));
      if (user == null) {
        throw new SubscriptionCheckFailed(CloudSubscriptionStatus.NO_SUBSCRIPTION);
      }
      checkSubscriptionEndDate(user, userId, user.get(CloudUser.EMAIL), connection);
    }
    catch (ItemNotFound e) {
      throw new SubscriptionCheckFailed(CloudSubscriptionStatus.NO_SUBSCRIPTION);
    }
    catch (TooManyItems e) {
      logger.error("Too many user token entries for user: " + userId);
      throw new SubscriptionCheckFailed(CloudSubscriptionStatus.NO_SUBSCRIPTION);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when looking for user: " + userId, e);
      }
    }
  }

  private void checkSubscriptionEndDate(Glob user, Integer userId, String email, SqlConnection connection) throws SubscriptionCheckFailed {
    Date endDate = user.get(CloudUser.SUBSCRIPTION_END_DATE);
    if (endDate == null) {
      throw new SubscriptionCheckFailed(CloudSubscriptionStatus.NO_SUBSCRIPTION);
    }
    if (now().after(endDate)) {
      throw new SubscriptionCheckFailed(CloudSubscriptionStatus.EXPIRED);
    }
  }
}
