package org.designup.picsou.gui;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class BankEntityEditionPanel {
  private GlobRepository repository;
  private Directory directory;
  private JLabel messageLabel;
  private JPanel panel = new JPanel();
  private GlobList bankEntities;

  public BankEntityEditionPanel(GlobRepository repository, Directory directory, JLabel messageLabel) {
    this.repository = repository;
    this.directory = directory;
    this.messageLabel = messageLabel;
  }

  public void init(GlobList bankEntities) {
    this.bankEntities = bankEntities;
    panel.setVisible(!bankEntities.isEmpty());
    panel.removeAll();
    if (bankEntities.isEmpty()) {
      return;
    }

    GridBagBuilder builder = GridBagBuilder.init(panel);
    int row = 0;

    for (Glob entity : bankEntities.sort(BankEntity.ID)) {
      GlobList accounts = repository.findLinkedTo(entity, Account.BANK_ENTITY);
      accounts.sort(new GlobFieldComparator(Account.NUMBER));

      builder.add(createTextArea(accounts, row), 0, row, 1, 1, new Insets(5, 0, 5, 10));
      builder.add(createCombo(entity, row), 1, row, 1, 1, new Insets(5, 10, 5, 0));
      row++;
    }
  }

  public boolean check() {
    for (Glob entity : bankEntities) {
      if (entity.get(BankEntity.BANK) == null) {
        messageLabel.setText(Lang.get("import.select.bank"));
        return false;
      }
    }
    return true;
  }

  public JPanel getPanel() {
    return panel;
  }

  private JTextArea createTextArea(GlobList accounts, int row) {
    JTextArea textArea = new JTextArea();
    textArea.setName("accountNames" + row);
    int index = 0;
    for (Glob account : accounts) {
      if (index++ > 0) {
        textArea.append("\n");
      }
      textArea.append(account.get(Account.NUMBER));
    }
    return textArea;
  }

  private JComboBox createCombo(final Glob entity, int row) {
    return GlobComboView.init(Bank.TYPE, repository, directory)
      .setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
        public void processSelection(Glob bank) {
          Key bankKey = bank != null ? bank.getKey() : null;
          repository.setTarget(entity.getKey(), BankEntity.BANK, bankKey);
        }
      })
      .setName("bankCombo" + row)
      .getComponent();
  }
}
