package org.designup.picsou.gui.projects.itemedition;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class ProjectItemTransferPanel extends ProjectItemEditionPanel {

  private JLabel savingsMessage;
  private GlobLinkComboEditor fromAccountCombo;
  private GlobLinkComboEditor toAccountCombo;

  public ProjectItemTransferPanel(Glob item, JScrollPane scrollPane, GlobRepository parentRepository, Directory directory) {
    super(item, scrollPane, parentRepository, directory);
  }

  protected JPanel createEditionPanel() {
    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectItemTransferEditionPanel.splits",
                                                            localRepository, directory);

    Key projectTransferKey = Key.create(ProjectTransfer.TYPE, itemKey.get(ProjectItem.ID));

    addCommonComponents(builder, true);

    GlobMatcher accountFilter = SeriesEditionDialog.createAccountFilter();

    fromAccountCombo = builder.addComboEditor("fromAccount", ProjectTransfer.FROM_ACCOUNT)
      .setEmptyOptionLabel(Lang.get("projectView.item.transfer.noFromAccount"))
      .setFilter(accountFilter)
      .forceSelection(projectTransferKey);
    disposables.add(fromAccountCombo);

    toAccountCombo = builder.addComboEditor("toAccount", ProjectTransfer.TO_ACCOUNT)
      .setEmptyOptionLabel(Lang.get("projectView.item.transfer.noToAccount"))
      .setFilter(accountFilter)
      .forceSelection(projectTransferKey);
    disposables.add(toAccountCombo);

    savingsMessage = new JLabel(Lang.get("projectView.item.transfer.savingsMessage"));
    builder.add("savingsMessage", savingsMessage);
    SavingsMessageUpdater savingsMessageUpdater = new SavingsMessageUpdater(projectTransferKey);
    savingsMessageUpdater.install();
    savingsMessageUpdater.update();
    disposables.add(savingsMessageUpdater);
    builder.add();

    builder.add("validate", validate);
    builder.add("cancel", new CancelAction());

    builder.add("handler", new HyperlinkHandler(directory));

    return builder.load();
  }

  protected boolean isNewItem(Glob item) {
    Key projectTransferKey = Key.create(ProjectTransfer.TYPE, item.get(ProjectItem.ID));
    Glob projectTransfer = parentRepository.get(projectTransferKey);
    return projectTransfer.get(ProjectTransfer.FROM_ACCOUNT) == null ||
           projectTransfer.get(ProjectTransfer.TO_ACCOUNT) == null;
  }

  protected boolean usesImages() {
    return false;
  }

  protected boolean check() {
    if (!super.check()) {
      return false;
    }

    Key projectTransferKey = Key.create(ProjectTransfer.TYPE, itemKey.get(ProjectItem.ID));
    Glob projectTransfer = localRepository.get(projectTransferKey);
    Integer fromAccount = projectTransfer.get(ProjectTransfer.FROM_ACCOUNT);
    if (fromAccount == null) {
      ErrorTip.showLeft(fromAccountCombo.getComponent(),
                        Lang.get("projectEdition.error.noFromAccount"),
                        directory);
      return false;
    }
    Integer toAccount = projectTransfer.get(ProjectTransfer.TO_ACCOUNT);
    if (toAccount == null) {
      ErrorTip.showLeft(toAccountCombo.getComponent(),
                        Lang.get("projectEdition.error.noToAccount"),
                        directory);
      return false;
    }
    if (Utils.equal(fromAccount, toAccount)) {
      ErrorTip.showLeft(fromAccountCombo.getComponent(),
                        Lang.get("projectEdition.error.sameFromAndToAccounts"),
                        directory);
      return false;
    }
    if (!Account.isSavings(localRepository.get(Key.create(Account.TYPE, fromAccount))) &&
        !Account.isSavings(localRepository.get(Key.create(Account.TYPE, toAccount)))) {
      ErrorTip.showLeft(fromAccountCombo.getComponent(),
                        Lang.get("projectEdition.error.noSavingsAccount"),
                        directory);
      return false;
    }

    Set<Integer> discardedAccountIds = new HashSet<Integer>();
    ChangeSet changeSet = localRepository.getCurrentChanges();
    Integer previousFromAccountId;
    if (changeSet.containsChanges(projectTransferKey, ProjectTransfer.FROM_ACCOUNT)) {
      previousFromAccountId = changeSet.getPreviousValues(projectTransferKey).get(ProjectTransfer.FROM_ACCOUNT);
      if ((previousFromAccountId != null) && Account.isUserCreatedAccount(previousFromAccountId)) {
        discardedAccountIds.add(previousFromAccountId);
      }
    }
    else {
      previousFromAccountId = projectTransfer.get(ProjectTransfer.FROM_ACCOUNT);
    }
    Integer previousToAccountId;
    if (changeSet.containsChanges(projectTransferKey, ProjectTransfer.TO_ACCOUNT)) {
      previousToAccountId = changeSet.getPreviousValues(projectTransferKey).get(ProjectTransfer.TO_ACCOUNT);
      if ((previousToAccountId != null) && Account.isUserCreatedAccount(previousToAccountId)) {
        discardedAccountIds.add(previousToAccountId);
      }
    }
    else {
      previousToAccountId = projectTransfer.get(ProjectTransfer.TO_ACCOUNT);
    }
    discardedAccountIds.remove(projectTransfer.get(ProjectTransfer.FROM_ACCOUNT));
    discardedAccountIds.remove(projectTransfer.get(ProjectTransfer.TO_ACCOUNT));
    if (!discardedAccountIds.isEmpty()) {
      ConfirmUncategorizeDialog confirmation =
        new ConfirmUncategorizeDialog(projectTransferKey, previousFromAccountId, previousToAccountId);
      confirmation.show();
      if (confirmation.cancelled) {
        return false;
      }
    }

    return true;
  }

  private class SavingsMessageUpdater extends KeyChangeListener implements Disposable {

    public SavingsMessageUpdater(Key projectTransferKey) {
      super(projectTransferKey);
    }

    public void install() {
      localRepository.addChangeListener(this);
    }

    public void update() {
      Glob projectTransfer = localRepository.get(Key.create(ProjectTransfer.TYPE, itemKey.get(ProjectItem.ID)));
      boolean isSavings = ProjectTransfer.usesSavingsAccounts(projectTransfer, ProjectItemTransferPanel.this.localRepository);
      savingsMessage.setVisible(isSavings);
    }

    public void dispose() {
      localRepository.removeChangeListener(this);
    }
  }

  private class ConfirmUncategorizeDialog extends ConfirmationDialog {

    private Key projectTransferKey;
    private final Integer previousFromAccountId;
    private final Integer previousToAccountId;
    public boolean cancelled = false;

    public ConfirmUncategorizeDialog(Key projectTransferKey, Integer previousFromAccountId, Integer previousToAccountId) {
      super("projectEdition.changeAccount.confirmUncategorize.title",
            Lang.get("projectEdition.changeAccount.confirmUncategorize.message"),
            directory.get(JFrame.class), directory);
      this.projectTransferKey = projectTransferKey;
      this.previousFromAccountId = previousFromAccountId;
      this.previousToAccountId = previousToAccountId;
    }

    protected void processCustomLink(String href) {
      GlobList transactions = getTransactions();
      dispose();
      processCancel();
      directory.get(NavigationService.class).gotoData(transactions);
    }

    private GlobList getTransactions() {
      Glob transfer = localRepository.get(projectTransferKey);
      Glob item = ProjectTransfer.getItemFromTransfer(transfer, localRepository);
      return ProjectItemTransferPanel.this.getAssignedTransactions(item, ProjectItemTransferPanel.this.parentRepository);
    }

    protected void processOk() {
      onCommitFunctors.add(new Functor() {
        public void run() throws Exception {
          GlobList transactions = getTransactions();
          for (Glob transaction : transactions) {
            parentRepository.update(transaction.getKey(),
                                    value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                                    value(Transaction.SUB_SERIES, null));
          }
        }
      });
    }

    protected void processCancel() {
      cancelled = true;
    }
  }

  protected GlobList getAssignedTransactions(Glob item, GlobRepository repository) {
    Integer seriesId = item.get(ProjectItem.SERIES);
    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    Set<Integer> seriesIds = new HashSet<Integer>();
    seriesIds.add(seriesId);
    Integer mirror = series.get(Series.MIRROR_SERIES);
    if (mirror != null) {
      seriesIds.add(mirror);
    }
    return repository.getAll(Transaction.TYPE,
                             and(fieldIn(Transaction.SERIES, seriesIds),
                                 isFalse(Transaction.PLANNED)));
  }
}
