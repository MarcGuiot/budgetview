package org.designup.picsou.gui.transactions.details;

import org.designup.picsou.model.Category;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class CategorisationHyperlinkButton extends HyperlinkButton implements GlobSelectionListener, ChangeSetListener {
  private GlobRepository repository;
  private Directory directory;
  private GlobList transactions;

  public CategorisationHyperlinkButton(Action action, GlobRepository repository, Directory directory) {
    super(action);
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    repository.addChangeListener(this);
  }

  public void selectionUpdated(GlobSelection selection) {
    transactions = selection.getAll(Transaction.TYPE);
    updateLabel();
  }

  private void updateLabel() {
    if (transactions == null || transactions.size() == 0) {
      setText("");
      return;
    }
    Integer categoryId = transactions.get(0).get(Transaction.CATEGORY);
    for (Glob transaction : transactions) {
      if (!Utils.equal(categoryId, transaction.get(Transaction.CATEGORY))) {
        setText(Lang.get("transaction.details.multicategories"));
        return;
      }
      categoryId = transaction.get(Transaction.CATEGORY);
    }

    if (categoryId == null) {
      setText(Lang.get("category.assignement.required"));
    }
    else {
      GlobStringifier categoryStringifier = directory.get(DescriptionService.class).getStringifier(Category.TYPE);
      setText(categoryStringifier.toString(repository.get(Key.create(Category.TYPE, categoryId)),
                                           repository));
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      updateLabel();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE)) {
      updateLabel();
    }
  }
}
