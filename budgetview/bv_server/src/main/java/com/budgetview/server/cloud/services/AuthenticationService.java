package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudUser;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlCreateRequest;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.DbConstraintViolation;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.sqlstreams.exceptions.RollbackFailed;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

public class AuthenticationService {

  private static Logger logger = Logger.getLogger("AuthenticationService");

  private GlobsDatabase database;

  public AuthenticationService(Directory directory) {
    this.database = directory.get(GlobsDatabase.class);
  }

  public Integer findUser(String email) throws GlobsSQLException {
    String lowerCaseEmail = email.toLowerCase();
    SqlConnection connection = database.connect();
    Integer userId = null;
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, Where.fieldEquals(CloudUser.EMAIL, lowerCaseEmail));
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

  public Integer createUser(String email) throws GlobsSQLException  {
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

  public boolean isUserValidated(int userId) throws GlobsSQLException {
    SqlConnection connection = database.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId));
      return user.get(CloudUser.EMAIL_VERIFIED);
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
    return false;
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
