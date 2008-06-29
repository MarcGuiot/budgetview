package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionToCategory;
import org.designup.picsou.utils.Lang;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.KeyBuilder.newKey;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class DeleteCategoryAction extends AbstractCategoryAction {
  protected AllocationLearningService learningService;

  public DeleteCategoryAction(GlobRepository repository, Directory directory) {
    super("-", repository, directory);
    learningService = directory.get(AllocationLearningService.class);
  }

  public boolean appliesFor(GlobList categories) {
    if (categories.size() < 1) {
      return false;
    }
    for (Glob category : categories) {
      if (Category.isMaster(category) || Category.isSystem(category)) {
        return false;
      }
    }
    return true;
  }

  protected void process(GlobList selectedCategories) {
    int confirm = JOptionPane.showConfirmDialog(parent, Lang.get("confirm.delete.subcategory"));
    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    int masterId = getCommonMaster(selectedCategories);

    repository.enterBulkDispatchingMode();
    try {
      for (Glob category : selectedCategories) {
        updateTransactions(category);
        updateTTC(category);
        learningService.deleteCategory(category.get(Category.ID), masterId, repository);
        repository.delete(category.getKey());
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
    selectionService.select(repository.get(newKey(Category.TYPE, masterId)));
  }

  private int getCommonMaster(GlobList categories) {
    Set<Integer> ids = new HashSet<Integer>();
    for (Glob category : categories) {
      ids.add(category.get(Category.MASTER));
    }
    if (ids.size() == 1) {
      return ids.iterator().next();
    }
    return MasterCategory.ALL.getId();

  }

  private void updateTransactions(Glob category) {
    Integer masterId = category.get(Category.MASTER);
    Integer categoryId = category.get(Category.ID);
    for (Glob transaction : repository.getAll(Transaction.TYPE)) {
      if (categoryId.equals(transaction.get(Transaction.CATEGORY))) {
        repository.update(transaction.getKey(), Transaction.CATEGORY, masterId);
      }
    }
  }

  private void updateTTC(Glob category) {
    Integer masterId = category.get(Category.MASTER);
    Integer categoryId = category.get(Category.ID);
    for (Glob ttc : repository.getAll(TransactionToCategory.TYPE)) {
      if (categoryId.equals(ttc.get(TransactionToCategory.CATEGORY))) {
        GlobList existingLink = repository.findByIndex(TransactionToCategory.TRANSACTION_INDEX, TransactionToCategory.TRANSACTION,
                                                       ttc.get(TransactionToCategory.TRANSACTION)).findByIndex(TransactionToCategory.CATEGORY);
        if (existingLink.isEmpty()) {
          repository.create(TransactionToCategory.TYPE,
                            value(TransactionToCategory.TRANSACTION, ttc.get(TransactionToCategory.TRANSACTION)),
                            value(TransactionToCategory.CATEGORY, masterId));
          repository.delete(ttc.getKey());
        }
      }
    }
  }
}
