package org.designup.picsou.gui.startup;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class BankEntityEditionPanel {
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel = new JPanel();

  public BankEntityEditionPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void init(GlobList bankEntities) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/bankEntityEditionPanel.splits",
                                                      repository, directory);

    builder.add("panel", panel);
    panel.setVisible(!bankEntities.isEmpty());

    builder.addRepeat("repeat",
                      bankEntities.sort(BankEntity.ID),
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, Glob entity) {
                          GlobList accounts = repository.findLinkedTo(entity, Account.BANK_ENTITY);
                          accounts.sort(new GlobFieldComparator(Account.NUMBER));
                          Integer entityId = entity.get(BankEntity.ID);
                          cellBuilder.add("accounts", createTextArea(accounts, entityId));
                          cellBuilder.add("banksCombo", createCombo(entity, entityId));
                        }
                      });

    builder.load();
  }

  public boolean check() {
    return true;
  }

  public JPanel getPanel() {
    return panel;
  }

  private JTextArea createTextArea(GlobList accounts, int entityId) {
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

  private JComboBox createCombo(final Glob entity, int entityId) {
    return GlobComboView.init(Bank.TYPE, repository, directory)
      .setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
        public void processSelection(Glob bank) {
          Key bankKey = bank != null ? bank.getKey() : null;
          repository.setTarget(entity.getKey(), BankEntity.BANK, bankKey);
        }
      })
      .setName("bankCombo:" + entityId)
      .getComponent();
  }
}
