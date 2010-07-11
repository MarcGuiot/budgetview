package org.designup.picsou.gui.bank;

import org.designup.picsou.model.Bank;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.GlobListViewFilter;
import org.globsframework.model.Key;

import javax.swing.*;

public class BankChooserPanel {

  public static void registerComponents(GlobsPanelBuilder builder, Action validateAction) {
    GlobListView bankListView = builder.addList("bankList", Bank.TYPE)
      .addDoubleClickAction(validateAction);

    builder.add("bankEditor",
                GlobListViewFilter.init(bankListView)
                  .setDefault(Key.create(Bank.TYPE, Bank.GENERIC_BANK_ID)));
  }
}
