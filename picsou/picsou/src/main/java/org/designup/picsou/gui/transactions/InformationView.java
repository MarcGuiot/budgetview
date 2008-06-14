package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionToCategory;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ForegroundColorUpdater;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InformationView extends View implements GlobSelectionListener, ChangeSetListener {
  private boolean displayWarning = false;
  private JPanel panel = new JPanel();
  private JLabel label = new JLabel();
  private IconLocator iconLocator;
  private TransactionSelection transactionSelection;

  public InformationView(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    this.transactionSelection.addListener(this);

    this.iconLocator = directory.get(IconLocator.class);

    repository.addChangeListener(this);

    panel.setLayout(new BorderLayout());
    label.setHorizontalAlignment(JLabel.LEFT);
    panel.add(label, BorderLayout.WEST);
    hideWarning();

    directory.get(ColorService.class).install(PicsouColors.INFOLABEL_FG.toString(),
                                              new ForegroundColorUpdater(label));
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("informationPanel", panel);
  }

  public JPanel getPanel() {
    return panel;
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
    update();
  }

  private void update() {
    for (Glob transaction : repository.getAll(Transaction.TYPE, transactionSelection.getCurrentMatcher())) {
      if (TransactionToCategory.hasCategories(transaction, repository)) {
        if (!displayWarning) {
          showWarning();
        }
        return;
      }
    }
    if (displayWarning) {
      hideWarning();
    }
  }

  private void hideWarning() {
    label.setText(" ");
    label.setIcon(null);
    displayWarning = false;
  }

  private void showWarning() {
    label.setText(Lang.get("transaction.allocation.warning"));
    label.setIcon(iconLocator.get("exclamation.png"));
    displayWarning = true;
  }
}
