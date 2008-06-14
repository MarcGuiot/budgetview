package org.designup.picsou.gui.transactions;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class CategoryChooserAction extends AbstractAction implements GlobSelectionListener {

  private GlobList selectedTransactions;
  private TransactionRendererColors colors;
  private GlobRepository repository;
  private Directory directory;
  private AllocationLearningService learningService;
  private CategoryChooserDialog dialog;

  public CategoryChooserAction(TransactionRendererColors colors, GlobRepository repository, Directory directory) {
    super(Lang.get("choose.category"));
    this.colors = colors;
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    this.learningService = directory.get(AllocationLearningService.class);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!selection.isRelevantForType(Transaction.TYPE)) {
      return;
    }
    selectedTransactions = selection.getAll(Transaction.TYPE);
    setEnabled(!selectedTransactions.isEmpty());
  }

  public void actionPerformed(final ActionEvent e) {
    if (dialog == null) {
      dialog = new CategoryChooserDialog(new CategoryChooserCallback() {
        public void categorySelected(final Glob category) {
          boolean displayPropagationDialog = false;
          Set<Glob> allTransactionsToBeLearned = new HashSet<Glob>();
          for (Glob transaction : selectedTransactions) {
            GlobList toBeLearned =
              learningService.getTransactionsToBeLearned(transaction, category.get(Category.ID), repository);
            allTransactionsToBeLearned.addAll(toBeLearned);
            if (toBeLearned.size() > 1) {
              displayPropagationDialog = true;
            }
          }

          // disable the feature for now
          displayPropagationDialog = false;
          if (displayPropagationDialog) {
            CategoryPropagationDialog dialog =
              new CategoryPropagationDialog(new CategoryPropagationCallback() {
                public void propagate() {
                  categorise(category, selectedTransactions);
                }
              }, new GlobList(allTransactionsToBeLearned), colors, repository, directory);
            dialog.show();
          }
          else {
            categorise(category, selectedTransactions);
          }
        }
      }, colors, repository, directory);
    }
    dialog.show(selectedTransactions);
  }

  private void categorise(Glob category, GlobList transactions) {
    repository.enterBulkDispatchingMode();
    try {
      for (Glob transaction : transactions) {
        learningService.learn(transaction, category.get(Category.ID), repository);
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }
}