package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class SignpostSequenceTest extends LoggedInFunctionalTestCase {
  public void testImport() throws Exception {

    System.out.println("SignpostSequenceTest.testImport: start");

    views.selectHome();

    actions.checkImportSignpostDisplayed("Click here to import your operations");

    System.out.println("SignpostSequenceTest.testImport: ");

    actions.openImport().close();

    actions.checkImportSignpostHidden();
  }
}
