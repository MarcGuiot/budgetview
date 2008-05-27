package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.ChangeSetListener;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ForegroundColorUpdater;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionToCategory;
import org.designup.picsou.utils.Lang;

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

  public void registerComponents(SplitsBuilder builder) {
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
