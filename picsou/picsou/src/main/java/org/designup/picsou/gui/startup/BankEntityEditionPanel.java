package org.designup.picsou.gui.startup;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class BankEntityEditionPanel {
  private GlobRepository repository;
  private Directory directory;
  private JLabel messageLabel;
  private JPanel panel = new JPanel();
  private GlobList accounts;

  public BankEntityEditionPanel(GlobRepository repository, Directory directory, JLabel messageLabel) {
    this.repository = repository;
    this.directory = directory;
    this.messageLabel = messageLabel;
  }

  public void init(final GlobList accounts) {
    this.accounts = accounts;

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/bankEntityEditionPanel.splits",
                                                      repository, directory);

    builder.add("panel", panel);
    panel.setVisible(!accounts.isEmpty());
    Set<String> valueSet = accounts.getSortedSet(Account.BANK_ENTITY_LABEL);
    builder.addRepeat("repeat",
                      valueSet,
                      new RepeatComponentFactory<String>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, String bankEntityLabel) {
                          GlobList filteredAccount =
                            accounts.filter(GlobMatchers.fieldEquals(Account.BANK_ENTITY_LABEL, bankEntityLabel), repository)
                            .sort(Account.NUMBER);
                          cellBuilder.add("accounts", createTextArea(filteredAccount, bankEntityLabel));
                          cellBuilder.add("banksCombo", createCombo(filteredAccount, bankEntityLabel));
                        }
                      });

    builder.load();
  }

  public boolean check() {
    for (Glob account : accounts) {
      if (account.get(Account.BANK) == null){
        messageLabel.setText(Lang.get("account.error.missing.bank"));
        return false;
      }
    }
    return true;
  }

  public JPanel getPanel() {
    return panel;
  }

  private JTextArea createTextArea(GlobList accounts, String entityId) {
    JTextArea textArea = new JTextArea();
    textArea.setName("accountNames:" + entityId);
    int index = 0;
    for (Glob account : accounts) {
      if (index++ > 0) {
        textArea.append("\n");
      }
      textArea.append(account.get(Account.NUMBER));
    }
    return textArea;
  }

  private JComboBox createCombo(final GlobList accounts, String entityId) {
    return GlobComboView.init(Bank.TYPE, repository, directory)
      .setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
        public void processSelection(Glob bank) {
          Key bankKey = bank != null ? bank.getKey() : null;
          for (Glob account : accounts) {
            repository.setTarget(account.getKey(), Account.BANK, bankKey);
          }
          for (Glob account : BankEntityEditionPanel.this.accounts) {
            if (account.get(Account.BANK) == null){
              return;
            }
          }
          messageLabel.setText("");
        }
      })
      .setName("bankCombo:" + entityId)
      .getComponent();
  }
}
