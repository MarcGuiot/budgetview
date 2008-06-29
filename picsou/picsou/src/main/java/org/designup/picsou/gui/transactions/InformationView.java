package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.TransactionSelection;
import org.designup.picsou.gui.View;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.List;

public class InformationView extends View implements GlobSelectionListener, ChangeSetListener {
  private JLabel label = new JLabel();
  private TransactionSelection transactionSelection;

  public InformationView(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    this.transactionSelection.addListener(this);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("uncategorizedLabel", label);
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
    GlobList uncategorizedTransactions =
      repository.getAll(Transaction.TYPE,
                        GlobMatchers.and(transactionSelection.getCurrentMatcher(),
                                         GlobMatchers.isNull(Transaction.CATEGORY)));
    label.setVisible(uncategorizedTransactions.size() > 0);
  }

}
