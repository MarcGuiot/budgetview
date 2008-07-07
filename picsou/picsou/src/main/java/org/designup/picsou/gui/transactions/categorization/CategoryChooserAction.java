package org.designup.picsou.gui.transactions.categorization;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
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

public class CategoryChooserAction extends AbstractAction implements GlobSelectionListener {

  private GlobList selectedTransactions;
  private TransactionRendererColors colors;
  protected GlobRepository repository;
  protected Directory directory;
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
      dialog = new CategoryChooserDialog(new MyCategoryChooserCallback(),
                                         colors, repository, directory, directory.get(JFrame.class));
    }
    dialog.show();
  }

  private class MyCategoryChooserCallback extends TransactionCategoryChooserCallback {
    public void categorySelected(final Glob category) {
      repository.enterBulkDispatchingMode();
      try {
        for (Glob transaction : selectedTransactions) {
          learningService.learn(transaction, category.get(Category.ID), repository);
        }
      }
      finally {
        repository.completeBulkDispatchingMode();
      }
    }

    protected GlobRepository getRepository() {
      return repository;
    }

    protected GlobList getReferenceTransactions() {
      return selectedTransactions;
    }
  }
}