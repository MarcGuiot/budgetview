package org.designup.picsou.gui.bank;

import org.designup.picsou.model.Bank;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.GlobListViewFilter;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;

import javax.swing.*;

public class BankChooserPanel {

  private GlobListViewFilter filter;

  public static BankChooserPanel registerComponents(GlobsPanelBuilder builder, Action validateAction, GlobMatcher matcher) {
    return new BankChooserPanel(builder, validateAction, matcher);
  }

  private BankChooserPanel(GlobsPanelBuilder builder, Action validateAction, GlobMatcher matcher) {
    GlobListView bankListView = builder.addList("bankList", Bank.TYPE)
      .addDoubleClickAction(validateAction);
    filter = GlobListViewFilter.init(bankListView);
    if (matcher == null) {
      filter.setDefaultValue(Key.create(Bank.TYPE, Bank.GENERIC_BANK_ID));
    }
    else {
      filter.setDefaultMatcher(matcher);
    }
    builder.add("bankEditor", filter);
  }

  public void requestFocus() {
    filter.getComponent().requestFocus();
  }
}
