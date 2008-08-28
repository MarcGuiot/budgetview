package org.designup.picsou.functests.upgrade;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

public class UpgradeV1Test extends LoggedInFunctionalTestCase {


  public void testReadXmlV1() throws Exception {
    createRepo();
  }

  public void createRepo() {
    OfxBuilder
      .init(this)
//      .addCategory(MasterCategory.FOOD, "courant")
      .addCategory(MasterCategory.FOOD, "resto")
      .addTransaction("2006/01/10", -1.2, "Menu K", "courant", "resto")
      .addTransaction("2006/01/11", -1.2, "Menu I", MasterCategory.FOOD)
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.openSplitDialog(0)
      .enterAmount("-1")
      .enterNote("COCA")
      .selectEnvelope(MasterCategory.FOOD, true)
      .add()
      .ok();

    String fileName = TestUtils.getFileName(this, ".ofx");
    operations.exportFile(fileName);
    String exportedContent = Files.loadFileToString(fileName);
    System.out.println(exportedContent);
  }
}
