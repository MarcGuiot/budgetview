package org.designup.picsou.gui.card;

import org.designup.picsou.bank.importer.SynchronizeAction;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.signpost.guides.ImportSignpost;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class ImportPanel extends View {

  private JButton importLabel;
  private JButton syncLabel;
  private Mode mode;
  private boolean showSignpost;

  public enum Mode {
    STANDARD("/layout/importexport/importPanel_standard.splits"),
    COMPACT("/layout/importexport/importPanel_compact.splits");

    final String filePath;

    private Mode(String filePath) {
      this.filePath = filePath;
    }
  }

  public ImportPanel(Mode mode, boolean showSignpost, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.mode = mode;
    this.showSignpost = showSignpost;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), mode.filePath, repository, directory);

    Action action = ImportFileAction.init(Lang.get("import"), repository, directory, null);
    JButton button = new JButton(action);
    builder.add("importButton", button);

    importLabel = new JButton();
    importLabel.setModel(button.getModel());
    builder.add("importLabel", importLabel);

    Action sync = new SynchronizeAction(repository, directory);
    JButton syncButton = new JButton(sync);
    builder.add("synchroButton", syncButton);

    syncLabel = new JButton();
    syncLabel.setModel(syncButton.getModel());
    builder.add("synchroLabel", syncLabel);

    if (showSignpost) {
      final ImportSignpost importSignpost = new ImportSignpost(repository, directory);
      importSignpost.attach(button);
    }

    parentBuilder.add("importPanel", builder);

    repository.addChangeListener(new TypeChangeSetListener(RealAccount.TYPE) {
      protected void update(GlobRepository repository) {
        updateLabels();
      }
    });
    updateLabels();
  }

  private void updateLabels() {
    GlobList accounts = repository.getAll(RealAccount.TYPE, fieldEquals(RealAccount.FROM_SYNCHRO, Boolean.TRUE));
    if (accounts.isEmpty()) {
      importLabel.setText(Lang.get("importPanel.import.label"));
      syncLabel.setText("");
    }
    else if (accounts.size() == 1) {
      importLabel.setText(Lang.get("importPanel.import.label.other"));
      syncLabel.setText(getSyncLabel(accounts.getFirst()));
    }
    else {
      importLabel.setText(Lang.get("importPanel.import.label.other"));
      syncLabel.setText(Lang.get("importPanel.synchro.label.multi"));
    }
  }

  private String getSyncLabel(Glob realAccount) {
    String realAccountLabel = realAccount.get(RealAccount.BANK_ENTITY_LABEL);
    if (Strings.isNotEmpty(realAccountLabel)) {
      return Lang.get("importPanel.synchro.label.single", realAccountLabel);
    }

    Glob entity = repository.findLinkTarget(realAccount, RealAccount.BANK_ENTITY);
    if (entity != null) {
      String entityLabel = entity.get(BankEntity.LABEL);
      if (Strings.isNotEmpty(entityLabel)) {
        return Lang.get("importPanel.synchro.label.single", entityLabel);
      }
    }

    Glob bank = repository.findLinkTarget(realAccount, RealAccount.BANK);
    if (bank != null) {
      String bankName = bank.get(Bank.NAME);
      if (Strings.isNotEmpty(bankName)) {
        return Lang.get("importPanel.synchro.label.single", bankName);
      }
    }

    return Lang.get("importPanel.synchro.label.noname");
  }
}