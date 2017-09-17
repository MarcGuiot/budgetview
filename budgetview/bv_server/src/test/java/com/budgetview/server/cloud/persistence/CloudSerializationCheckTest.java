package com.budgetview.server.cloud.persistence;

import com.budgetview.server.cloud.functests.checkers.BudgeaChecker;
import com.budgetview.server.cloud.functests.checkers.CloudChecker;
import com.budgetview.server.cloud.functests.checkers.PaymentChecker;
import com.budgetview.server.cloud.services.CloudSerializationBuilder;
import junit.framework.TestCase;
import org.globsframework.utils.exceptions.InvalidConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class CloudSerializationCheckTest extends TestCase {

  @Test
  public void test() throws Exception {

    PaymentChecker payments = new PaymentChecker();

    BudgeaChecker budgea = new BudgeaChecker();
    budgea.startServer();

    // -- Initial case --

    System.setProperty(CloudSerializationBuilder.DB_ENCRYPTION_PHRASE_PROPERTY, "a random phrase");
    System.setProperty(CloudSerializationBuilder.DB_ENCRYPTION_PASSWORD_PROPERTY, "a first password");

    CloudChecker cloud = new CloudChecker(payments);
    cloud.startServer();

    // -- Wrong password & good phrase --

    System.setProperty(CloudSerializationBuilder.DB_ENCRYPTION_PASSWORD_PROPERTY, "another first password");

    try {
      cloud.restartServerWithSameDatabase();
      Assert.fail("Exception not raised");
    }
    catch (InvalidConfiguration e) {
      Assert.assertEquals("Phrase 'budgetview.db.encryption.pwd.phrase' and password are different from previous ones - aborting",
                          e.getMessage());
    }

    // -- Good password & wrong phrase --

    System.setProperty(CloudSerializationBuilder.DB_ENCRYPTION_PASSWORD_PROPERTY, "a first password");
    System.setProperty(CloudSerializationBuilder.DB_ENCRYPTION_PHRASE_PROPERTY, "a different phrase");

    try {
      cloud.restartServerWithSameDatabase();
      Assert.fail("Exception not raised");
    }
    catch (InvalidConfiguration e) {
      Assert.assertEquals("Phrase 'budgetview.db.encryption.pwd.phrase' is different from previous one - aborting",
                          e.getMessage());
    }

    // -- Good password & good phrase --

    System.setProperty(CloudSerializationBuilder.DB_ENCRYPTION_PHRASE_PROPERTY, "a random phrase");
    System.setProperty(CloudSerializationBuilder.DB_ENCRYPTION_PASSWORD_PROPERTY, "a first password");

    cloud.restartServerWithSameDatabase();

    // No error

    cloud.stopServer();
    budgea.stopServer();
  }
}
