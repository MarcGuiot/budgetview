package org.designup.picsou.gui.bank;

import org.designup.picsou.model.Bank;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
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

  public BankChooserPanel(GlobRepository repository, Directory directory, PicsouDialog parentDialog) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/bank/bankChooserPanel.splits",
                                    repository, directory);

    registerComponents(builder);

    panel = builder.load();
  }

  public static void registerComponents(GlobsPanelBuilder builder) {
    GlobListView bankListView = builder.addList("bankList", Bank.TYPE);

    builder.add("bankEditor",
                GlobListViewFilter.init(bankListView)
                  .setDefault(Key.create(Bank.TYPE, Bank.GENERIC_BANK_ID)));
  }

  public JPanel getPanel() {
    return panel;
  }

  public void dispose() {
    builder.dispose();
  }
}
