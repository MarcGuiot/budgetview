package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.utils.RandomStrings;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.mail.Mailer;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

import javax.mail.MessagingException;
import java.util.Date;

public class EmailValidationService {

  private static Logger logger = Logger.getLogger("EmailValidationService");

  private GlobsDatabase database;
  private final ConfigService config;
  private final Mailer mailer;
  private final RandomStrings sessionIds = new RandomStrings();

  public EmailValidationService(Directory directory) {
    this.config = directory.get(ConfigService.class);
    this.database = directory.get(GlobsDatabase.class);
    this.mailer = directory.get(Mailer.class);
  }

  public void send(Integer userId, String email, String lang) throws GlobsSQLException, MessagingException {

    String code = sessionIds.next(8);

    SqlConnection connection = database.connect();
    try {
      connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
        .set(CloudUser.LAST_VALIDATION_CODE, code)
        .set(CloudUser.LAST_VALIDATION_DATE, new Date())
        .run();
    }
    finally {
      connection.commitAndClose();
    }

    mailer.sendCloudEmailAddressVerification(email, lang, code);
  }

  public boolean check(Integer userId, String code) {
    SqlConnection connection = database.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId));
      return Utils.equal(user.get(CloudUser.LAST_VALIDATION_CODE), code.trim());
    }
    catch (ItemNotFound itemNotFound) {
      logger.info("No user found with id " + userId);
      return false;
    }
    catch (TooManyItems tooManyItems) {
      logger.info("Several users found with id " + userId);
      return false;
    }
    finally {
      connection.commitAndClose();
    }
  }
}
