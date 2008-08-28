package org.designup.picsou.gui.categorization;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.AbstractGlobSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import java.awt.event.ActionEvent;

public abstract class CategorizationAction extends AbstractGlobSelectionAction {
  private GlobRepository repository;

  public CategorizationAction(GlobRepository repository, Directory directory) {
    super(Lang.get("categorization.button"), Transaction.TYPE, repository, directory);
    this.repository = repository;
    setMatcher(getMatcher());
  }

  public static GlobMatcher getMatcher() {
    return not(fieldEquals(Transaction.TRANSACTION_TYPE, TransactionType.PLANNED.getId()));
  }

  public void actionPerformed(ActionEvent e) {
    CategorizationDialog dialog = directory.get(CategorizationDialog.class);
    dialog.show(getTransactionList(), true, false);
  }

  private GlobList getTransactionList() {
    if (lastSelection.size() > 1) {
      return lastSelection;
    }
    Glob transaction = lastSelection.get(0);
    if ((!Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) ||
        (Strings.isNullOrEmpty(transaction.get(Transaction.LABEL_FOR_CATEGORISATION)))) {
      return new GlobList(transaction);
    }
    else {
      return getAllGlobs()
        .filter(and(fieldEquals(Transaction.LABEL_FOR_CATEGORISATION,
                                transaction.get(Transaction.LABEL_FOR_CATEGORISATION)),
                    fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID)),
                repository);
    }
  }

  protected abstract GlobList getAllGlobs();
}
