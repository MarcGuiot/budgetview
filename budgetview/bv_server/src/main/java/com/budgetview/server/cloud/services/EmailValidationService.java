package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudEmailValidation;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.utils.RandomStrings;
import com.budgetview.server.cloud.utils.ValidationFailed;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.CloudValidationStatus;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TimeExpired;
import org.globsframework.utils.exceptions.TooManyItems;

import javax.mail.MessagingException;
import java.util.Date;

import static org.globsframework.utils.Dates.hoursLater;
import static org.globsframework.utils.Dates.now;

public class EmailValidationService {

  private static Logger logger = Logger.getLogger("EmailValidationService");

  private final GlobsDatabase database;
  private final Mailer mailer;
  private final RandomStrings sessionIds = new RandomStrings();

  public static Date forcedTokenExpirationDate;

  public EmailValidationService(Directory directory) {
    this.database = directory.get(GlobsDatabase.class);
    this.mailer = directory.get(Mailer.class);
  }

  public boolean sendDeviceValidationTempCode(Integer userId, String email, String lang) throws GlobsSQLException, MessagingException {
    return mailer.sendCloudDeviceVerificationEmail(email, lang, createCode(userId, email, 8));
  }

  public boolean sendSubscriptionEmailValidationLink(Integer userId, String email, String lang) throws GlobsSQLException, MessagingException {
    String code = createCode(userId, email, 24);
    String url = CloudConstants.getSubscriptionValidationUrl(code);
    return mailer.sendSubscriptionEmailValidationLink(email, lang, url);
  }

  public String createCode(Integer userId, String email, int length) {
    String validationCode = sessionIds.next(length);
    SqlConnection connection = database.connect();
    try {
      connection
        .startDelete(CloudEmailValidation.TYPE, Where.fieldEquals(CloudEmailValidation.CODE, validationCode))
        .execute();

      connection
        .startCreate(CloudEmailValidation.TYPE)
        .set(CloudEmailValidation.CODE, validationCode)
        .set(CloudEmailValidation.EMAIL, email)
        .set(CloudEmailValidation.USER, userId)
        .set(CloudEmailValidation.CREATION_DATE, now())
        .set(CloudEmailValidation.EXPIRATION_DATE, getTokenExpirationDate())
        .run();
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when sending temp code for user: " + userId + " with email: " + email, e);
      }
    }

    logger.info("[REMOVE ME!!!] Temp code is " + validationCode);
    return validationCode;
  }

  public void checkTempCode(Integer userId, String validationCode) throws ValidationFailed {
    if (Strings.isNullOrEmpty(validationCode)) {
      throw new ValidationFailed(CloudValidationStatus.UNKNOWN_VALIDATION_CODE);
    }

    SqlConnection connection = database.connect();
    try {
      GlobList entries = connection.selectAll(CloudEmailValidation.TYPE, Where.fieldEquals(CloudEmailValidation.CODE, validationCode));
      if (entries.isEmpty()) {
        throw new ValidationFailed(CloudValidationStatus.UNKNOWN_VALIDATION_CODE);
      }
      Glob entry = entries.getFirst();
      Date referenceCodeDate = entry.get(CloudEmailValidation.EXPIRATION_DATE);
      if (referenceCodeDate == null || now().after(referenceCodeDate)) {
        logger.info("Temp token expired since " + referenceCodeDate);
        throw new ValidationFailed(CloudValidationStatus.TEMP_VALIDATION_CODE_EXPIRED);
      }
    }
    catch (ItemNotFound itemNotFound) {
      logger.info("No user found with id " + userId);
      throw new ValidationFailed(CloudValidationStatus.UNKNOWN_VALIDATION_CODE);
    }
    catch (TooManyItems tooManyItems) {
      logger.info("Several users found with id " + userId);
      throw new ValidationFailed(CloudValidationStatus.UNKNOWN_VALIDATION_CODE);
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when checking temp code for: " + userId, e);
      }
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
      result = hoursLater(1);
    }
    logger.info("Created expiration date: " + result);
    return result;
  }

  public Glob getUser(String code) throws ItemNotFound, TimeExpired {
    SqlConnection connection = database.connect();
    try {
      GlobList items =
        connection.selectAll(CloudEmailValidation.TYPE, Where.fieldEquals(CloudEmailValidation.CODE, code));
      if (items.isEmpty()) {
        throw new ItemNotFound();
      }
      else {
        Glob item = items.getFirst();
        Date expirationDate = item.get(CloudEmailValidation.EXPIRATION_DATE);
        if (now().after(expirationDate)) {
          throw new TimeExpired();
        }

        Integer userId = item.get(CloudEmailValidation.USER);
        return connection.selectUnique(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId));
      }
    }
    finally {
      try {
        connection.commitAndClose();
      }
      catch (GlobsSQLException e) {
        logger.error("Commit failed when finding user id for code: " + code, e);
      }
    }
  }
}
