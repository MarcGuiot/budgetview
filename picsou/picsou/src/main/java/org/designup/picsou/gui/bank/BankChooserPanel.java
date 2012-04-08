package org.designup.picsou.gui.bank;

import org.designup.picsou.gui.bank.actions.AddBankAction;
import org.designup.picsou.gui.bank.actions.DeleteBankAction;
import org.designup.picsou.gui.bank.actions.EditBankAction;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.GlobListViewFilter;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class BankChooserPanel {

  private JPanel panel;
  private GlobListViewFilter filter;
  private DeleteBankAction deleteBankAction;

  public BankChooserPanel(GlobRepository repository,
                          Directory directory,
                          Action validateAction,
                          GlobMatcher matcher,
                          Window owner) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(),
                                                      "/layout/bank/bankChooserPanel.splits",
                                                      repository, directory);

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

    JPopupMenu menu = new JPopupMenu();
    menu.add(new EditBankAction(owner, repository, directory));
    deleteBankAction = new DeleteBankAction(owner, repository, directory);
    menu.add(deleteBankAction);
    builder.add("bankActions", new JPopupButton(Lang.get("budgetView.actions"), menu));

    builder.add("addBank", new AddBankAction(owner, repository, builder.getDirectory()));

    panel = builder.load();
  }

  public void setExcludedAccounts(Set<Integer> excludedAccountIds) {
    deleteBankAction.setExcludedAccounts(excludedAccountIds);
  }

  public void requestFocus() {
    filter.getComponent().requestFocus();
  }

  public JPanel getPanel() {
    return panel;
  }
}
