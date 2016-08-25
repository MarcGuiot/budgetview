package com.budgetview.server.cloud.functests;

import com.budgetview.server.cloud.functests.checkers.CloudDesktopTestCase;
import com.budgetview.shared.cloud.BudgeaAPI;
import org.junit.Test;

public class CloudImportTest extends CloudDesktopTestCase {

  @Test
  public void testCreateStandardConnection() throws Exception {

    BudgeaAPI api = new BudgeaAPI();
    api.getToken();

    operations.openImportDialog()
      .selectCloud()
      .checkContainsBanks("BNP Paribas", "CIC", "Connecteur de Test Budgea", "Crédit Agricole", "LCL")
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .doImport()
      .close();


  }

}
