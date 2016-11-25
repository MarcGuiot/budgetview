package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.utils.CheckFailed;
import com.budgetview.server.cloud.utils.RandomStrings;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.cloud.CloudRequestStatus;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

import javax.mail.MessagingException;
import java.util.Date;

public class EmailValidationService {

  private static Logger logger = Logger.getLogger("EmailValidationService");

  public static final int TEMP_CODE_DURATION_IN_MINUTES = 30;

  private GlobsDatabase database;
  private final ConfigService config;
  private final Mailer mailer;
  private final RandomStrings sessionIds = new RandomStrings();
  public static Date forcedTokenExpirationDate;

  public EmailValidationService(Directory directory) {
    this.config = directory.get(ConfigService.class);
    this.database = directory.get(GlobsDatabase.class);
    this.mailer = directory.get(Mailer.class);
  }

  public void sendTempCode(Integer userId, String email, String lang) throws GlobsSQLException, MessagingException {

    String code = sessionIds.next(8);

    SqlConnection connection = database.connect();
    try {
      connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
        .set(CloudUser.LAST_VALIDATION_CODE, code)
        .set(CloudUser.LAST_VALIDATION_DATE, getTokenExpirationDate())
        .run();
    }
    finally {
      connection.commitAndClose();
    }

    mailer.sendCloudEmailAddressVerification(email, lang, code);
  }

  public void checkTempCode(Integer userId, String validationCode) throws CheckFailed {
    if (Strings.isNullOrEmpty(validationCode)) {
      throw new CheckFailed(CloudRequestStatus.UNKNOWN_CODE);
    }

    SqlConnection connection = database.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId));
      Date referenceCodeDate = user.get(CloudUser.LAST_VALIDATION_DATE);
      String referenceCode = user.get(CloudUser.LAST_VALIDATION_CODE);
      if ((referenceCode == null) || (referenceCodeDate == null)) {
        throw new CheckFailed(CloudRequestStatus.UNKNOWN_CODE);
      }
      if (Dates.minutesBetween(referenceCodeDate, new Date()) > TEMP_CODE_DURATION_IN_MINUTES) {
        logger.info("Temp token expired since " + referenceCodeDate);
        throw new CheckFailed(CloudRequestStatus.TEMP_CODE_EXPIRED);
      }
      if (!Utils.equal(referenceCode, validationCode.trim())) {
        throw new CheckFailed(CloudRequestStatus.UNKNOWN_CODE);
      }
    }
    catch (ItemNotFound itemNotFound) {
      logger.info("No user found with id " + userId);
      throw new CheckFailed(CloudRequestStatus.UNKNOWN_CODE);
    }
    catch (TooManyItems tooManyItems) {
      logger.info("Several users found with id " + userId);
      throw new CheckFailed(CloudRequestStatus.UNKNOWN_CODE);
    }
    finally {
      connection.commitAndClose();
    }
  }

  public static void forceTokenExpirationDate(Date date) {
    forcedTokenExpirationDate = date;
  }

  public Date getTokenExpirationDate() {
    Date result;
    if (forcedTokenExpirationDate != null) {
      result = forcedTokenExpirationDate;
      forcedTokenExpirationDate = null;
    }
    else {
      result = new Date();
    }
    return result;
  }
}
