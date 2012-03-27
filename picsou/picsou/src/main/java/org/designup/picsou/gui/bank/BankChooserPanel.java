package org.designup.picsou.gui.bank;

import org.designup.picsou.gui.bank.actions.AddBankAction;
import org.designup.picsou.model.Bank;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.GlobListViewFilter;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;

import javax.swing.*;
import java.awt.*;

public class BankChooserPanel {

  private GlobListViewFilter filter;

  public static BankChooserPanel registerComponents(GlobsPanelBuilder builder,
                                                    GlobRepository repository,
                                                    Action validateAction,
                                                    GlobMatcher matcher,
                                                    Window owner) {
    return new BankChooserPanel(builder, repository, validateAction, matcher, owner);
  }

  private BankChooserPanel(GlobsPanelBuilder builder, GlobRepository repository, Action validateAction, GlobMatcher matcher, Window owner) {
    GlobListView bankListView = builder.addList("bankList", Bank.TYPE)
      .addDoubleClickAction(validateAction);
    filter = GlobListViewFilter.init(bankListView).setIgnoreAccents(true);
    if (matcher == null) {
      filter.setDefaultValue(Key.create(Bank.TYPE, Bank.GENERIC_BANK_ID));
    }
    else {
      filter.setDefaultMatcher(matcher);
    }
    builder.add("bankEditor", filter);

    builder.add("addBank", new AddBankAction(owner, repository, builder.getDirectory()));
  }

  public void requestFocus() {
    filter.getComponent().requestFocus();
  }
}
