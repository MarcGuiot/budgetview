package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.TransactionSelection;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categorization.CategorizationDialog;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class UncategorizedMessageView extends View implements GlobSelectionListener, ChangeSetListener {
  private JTextArea textArea = new JTextArea();
  private TransactionSelection transactionSelection;
  private GlobList uncategorizedTransactions = GlobList.EMPTY;

  public UncategorizedMessageView(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    this.transactionSelection.addListener(this);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("uncategorizedMessage", textArea);
    builder.add("categorizeRemainingTransactions", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        final CategorizationDialog dialog = directory.get(CategorizationDialog.class);
        dialog.show(uncategorizedTransactions);
      }
    });
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
    uncategorizedTransactions =
      repository.getAll(Transaction.TYPE,
                        and(transactionSelection.getCurrentMatcher(),
                            isNull(Transaction.CATEGORY)));
    textArea.setVisible(uncategorizedTransactions.size() > 0);
  }

}
