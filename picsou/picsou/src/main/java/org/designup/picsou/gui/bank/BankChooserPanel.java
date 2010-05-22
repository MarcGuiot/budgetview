package org.designup.picsou.gui.bank;

import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.GlobListViewFilter;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class BankChooserPanel {
  private JPanel panel;
  private GlobsPanelBuilder builder;

  public BankChooserPanel(GlobRepository repository, Directory directory) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/bank/bankChooserPanel.splits",
                                    repository, directory);

    GlobListView bankListView = builder.addList("bankList", Bank.TYPE);

    builder.add("bankEditor",
                GlobListViewFilter.init(bankListView)
                  .setDefault(Key.create(Bank.TYPE, Bank.GENERIC_BANK_ID)));

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void dispose() {
    builder.dispose();
  }
}
