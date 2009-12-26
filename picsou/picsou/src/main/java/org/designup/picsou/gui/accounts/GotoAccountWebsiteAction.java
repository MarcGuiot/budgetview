package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class GotoAccountWebsiteAction extends AbstractAction implements ChangeSetListener {
  private String url;
  private GlobRepository repository;
  private Key accountKey;
  private BrowsingService browsingService;

  public GotoAccountWebsiteAction(Glob account, GlobRepository repository, Directory directory) {
    super(Lang.get("accountView.goto.website"));
    this.repository = repository;
    this.browsingService = directory.get(BrowsingService.class);
    this.accountKey = account.getKey();
    repository.addChangeListener(this);
    update();
  }

  private void update() {
    Glob account = repository.find(accountKey);
    if (account == null) {
      url = null;
      repository.removeChangeListener(this);
      setEnabled(false);
      return;
    }

    Glob bank = repository.findLinkTarget(account, Account.BANK);
    if (bank != null) {
      url = bank.get(Bank.DOWNLOAD_URL);
    }
    setEnabled(Strings.isNotEmpty(url));
  }

  public void actionPerformed(ActionEvent e) {
    browsingService.launchBrowser(url);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(accountKey)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Account.TYPE)) {
      update();
    }
  }
}
