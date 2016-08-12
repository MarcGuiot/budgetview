package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.model.CloudUser;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
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
      logger.error("No user found with email: " + lowerCaseEmail);
    }
    catch (TooManyItems tooManyItems) {
      logger.error("Several entries found for user: " + lowerCaseEmail);
    }
    finally {
      connection.commitAndClose();
    }
    return userId;
  }
}
