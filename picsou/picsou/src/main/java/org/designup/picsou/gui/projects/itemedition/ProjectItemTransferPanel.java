package org.designup.picsou.gui.projects.itemedition;

import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.ProjectTransfer;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ProjectItemTransferPanel extends ProjectItemEditionPanel {

  private JLabel savingsMessage;
  private GlobLinkComboEditor fromAccountCombo;
  private GlobLinkComboEditor toAccountCombo;

  public ProjectItemTransferPanel(Glob item, GlobRepository parentRepository, Directory directory) {
    super(item, parentRepository, directory);
  }

  protected JPanel createEditionPanel() {
    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectItemTransferEditionPanel.splits",
                                                            localRepository, directory);

    Key projectTransferKey = Key.create(ProjectTransfer.TYPE, itemKey.get(ProjectItem.ID));

    addCommonComponents(builder);

    GlobNumericEditor amountEditor = GlobNumericEditor.init(ProjectItem.PLANNED_AMOUNT, localRepository, directory)
      .setValidationAction(validate)
      .setPositiveNumbersOnly(true)
      .forceSelection(itemKey);
    amountEditorField = amountEditor.getComponent();
    builder.add("amountEditor", amountEditorField);
    disposables.add(amountEditor);

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
        !Account.isSavings(localRepository.get(Key.create(Account.TYPE, fromAccount)))) {
      ErrorTip.showLeft(fromAccountCombo.getComponent(),
                        Lang.get("projectEdition.error.noSavingsAccount"),
                        directory);
      return false;
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

    protected void update() {
      Glob projectTransfer = localRepository.get(Key.create(ProjectTransfer.TYPE, itemKey.get(ProjectItem.ID)));
      boolean isSavings = ProjectTransfer.isSavings(projectTransfer, ProjectItemTransferPanel.this.localRepository);
      savingsMessage.setVisible(isSavings);
    }

    public void dispose() {
      localRepository.removeChangeListener(this);
    }
  }
}
