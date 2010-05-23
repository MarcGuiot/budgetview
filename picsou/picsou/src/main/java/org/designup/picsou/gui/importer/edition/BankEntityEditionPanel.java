package org.designup.picsou.gui.importer.edition;

import org.designup.picsou.gui.bank.BankChooserDialog;
import org.designup.picsou.model.Account;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class BankEntityEditionPanel {
  private Window container;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel = new JPanel();
  private GlobsPanelBuilder builder;

  public BankEntityEditionPanel(Window container, GlobRepository repository, Directory directory) {
    this.container = container;
    this.repository = repository;
    this.directory = directory;
  }

  public void init(final GlobList accounts) {

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/bankEntityEditionPanel.splits",
                                    repository, directory);

    builder.add("panel", panel);
    Set<String> valueSet = accounts.getSortedSet(Account.BANK_ENTITY_LABEL);
    builder.addRepeat("repeat",
                      valueSet,
                      new RepeatComponentFactory<String>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, String bankEntityLabel) {
                          GlobList filteredAccount =
                            accounts.filter(GlobMatchers.fieldEquals(Account.BANK_ENTITY_LABEL, bankEntityLabel), repository)
                              .sort(Account.NUMBER);
                          cellBuilder.add("accounts", createTextArea(filteredAccount, bankEntityLabel));
                          AccountBankAction action = new AccountBankAction(filteredAccount, container);
                          JButton button = new JButton(action);
                          cellBuilder.add("banksChooser", button);
                          button.setName("bankChooser:" + bankEntityLabel);
                        }
                      });

    builder.load();
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

  private class AccountBankAction extends AbstractAction {
    private GlobList accounts;
    private Window dialog;

    public AccountBankAction(GlobList accounts, Window dialog) {
      this.accounts = accounts;
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      BankChooserDialog bankChooserDialog = new BankChooserDialog(dialog, repository, directory);
      Integer bankId = bankChooserDialog.show();
      if (bankId != null) {
        for (Glob account : accounts) {
          repository.update(account.getKey(), Account.BANK, bankId);
        }
      }
    }

  }

  public void dispose() {
    builder.dispose();
  }
}
