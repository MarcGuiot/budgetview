package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class GotoAccountWebsiteAction extends AbstractAction implements ChangeSetListener, Disposable {
  private String url;
  private GlobRepository repository;
  private Key accountKey;
  private BrowsingService browsingService;
  private final DescriptionService descriptionService;

  public GotoAccountWebsiteAction(Glob account, GlobRepository repository, Directory directory) {
    super(getName(account, repository, directory.get(DescriptionService.class)));
    putValue(Action.SHORT_DESCRIPTION, Lang.get("accountView.goto.website.tooltip"));
    this.repository = repository;
    this.browsingService = directory.get(BrowsingService.class);
    descriptionService = directory.get(DescriptionService.class);
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
      url = bank.get(Bank.URL);
    }
    setEnabled(Strings.isNotEmpty(url));
    putValue(Action.NAME, bank != null ? getName(account, repository, descriptionService) : "");
  }

  public void actionPerformed(ActionEvent e) {
    browsingService.launchBrowser(url);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(accountKey) || changeSet.containsChanges(Bank.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Account.TYPE) || changedTypes.contains(Bank.TYPE)) {
      update();
    }
  }

  private static String getName(Glob account, GlobRepository repository, DescriptionService descriptionService) {
    Glob bank = Account.findBank(account, repository);
    if ((bank == null) || Strings.isNullOrEmpty(bank.get(Bank.URL))) {
      return Lang.get("accountView.goto.website.disabled");
    }
    String bankName = bank.get(Bank.SHORT_NAME);
    if (Strings.isNullOrEmpty(bankName)){
      bankName = descriptionService.getStringifier(Bank.TYPE).toString(bank, repository);
    }
    return Lang.get("accountView.goto.website", bankName);
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }
}
