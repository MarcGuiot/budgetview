package com.budgetview.server.cloud.services;

import com.budgetview.server.cloud.model.CloudConfig;
import com.budgetview.server.config.ConfigService;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidConfiguration;

import javax.crypto.BadPaddingException;

public class CloudSerializationBuilder {

  private static Logger logger = Logger.getLogger("CloudSerializationBuilder");

  public static final String DB_ENCRYPTION_PHRASE_PROPERTY = "budgetview.db.encryption.pwd.phrase";
  public static final String DB_ENCRYPTION_PASSWORD_PROPERTY = "budgetview.db.encryption.password";

  public static CloudSerializationService create(ConfigService configService, Directory directory) throws Exception {
    String encryptionPhrase = configService.get(DB_ENCRYPTION_PHRASE_PROPERTY);
    String encryptionPassword = configService.get(DB_ENCRYPTION_PASSWORD_PROPERTY);

    CloudSerializationService serializationService = new CloudSerializationService(encryptionPassword, directory);

    GlobsDatabase db = directory.get(GlobsDatabase.class);
    SqlConnection connection = null;
    try {
      connection = db.connect();
      GlobList configs =
        connection.startSelect(CloudConfig.TYPE, Where.fieldEquals(CloudConfig.ID, 0))
          .selectAll()
          .getList();
      if (configs.isEmpty()) {
        connection.startCreate(CloudConfig.TYPE)
          .set(CloudConfig.ID, 0)
          .set(CloudConfig.SAMPLE, serializationService.encode(encryptionPhrase.getBytes("UTF-8")))
          .run();
        logger.info("Sample initialized");
      }
      else {
        Glob config = configs.getFirst();
        byte[] decoded = serializationService.decode(config.get(CloudConfig.SAMPLE));
        String previousPhrase = new String(decoded, "UTF-8");
        if (!Utils.equal(previousPhrase, encryptionPhrase)) {
          throw new InvalidConfiguration("Phrase '" + DB_ENCRYPTION_PHRASE_PROPERTY + "' is different from previous one - aborting");
        }
      }
    }
    catch (BadPaddingException e) {
      throw new InvalidConfiguration("Phrase '" + DB_ENCRYPTION_PHRASE_PROPERTY + "' and password are different from previous ones - aborting");
    }
    catch (Exception e) {
      logger.info("error", e);
      if (connection != null) {
        connection.rollbackAndClose();
        connection = null;
      }
      throw e;
    }
    finally {
      if (connection != null) {
        connection.commitAndClose();
      }
    }

    return serializationService;
  }
}
