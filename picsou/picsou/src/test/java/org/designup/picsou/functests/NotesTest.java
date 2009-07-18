package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class NotesTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    views.selectHome();
    notes.checkText("");

    notes.setText("One note");
    notes.checkText("One note");
  }
}
