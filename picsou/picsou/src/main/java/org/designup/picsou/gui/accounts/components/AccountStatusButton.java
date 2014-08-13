package org.designup.picsou.gui.accounts.components;

import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.gui.model.PeriodAccountStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AccountStatusButton implements Disposable {

  private GlobRepository repository;
  private final Directory directory;
  private final Key periodStatKey;
  private KeyChangeListener updater;
  private SplitsNode<JButton> statusNode;

  public static AccountStatusButton create(Key accountKey, PanelBuilder builder, String componentName, GlobRepository repository, Directory directory) {
    AccountStatusButton button = new AccountStatusButton(accountKey, repository, directory);
    button.register(builder, componentName);
    builder.addDisposable(button);
    return button;
  }

  private AccountStatusButton(Key accountKey, final GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.periodStatKey = Key.create(PeriodAccountStat.TYPE, accountKey.get(Account.ID));
    this.updater = new KeyChangeListener(periodStatKey) {
      public void update() {
        Glob stat = repository.find(periodStatKey);
        boolean isOk = stat != null && stat.isTrue(PeriodAccountStat.OK);
        if (isOk) {
          statusNode.applyStyle("account:OK");
          statusNode.getComponent().setToolTipText(Lang.get("accountView.status.ok.tooltip"));
        }
        else {
          statusNode.applyStyle("account:NOK");
          statusNode.getComponent().setToolTipText(Lang.get("accountView.status.nok.tooltip"));
        }
      }
    };
  }

  private void register(PanelBuilder builder, String componentName) {
    statusNode = builder.add(componentName, new JButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        JButton button = statusNode.getComponent();
        DetailsTip tip = new DetailsTip(button, button.getToolTipText(), directory);
        tip.show();
      }
    }));
    repository.addChangeListener(updater);
    updater.update();
  }

  public void dispose() {
    repository.removeChangeListener(updater);
    updater = null;
    repository = null;
  }
}
