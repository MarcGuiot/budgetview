package org.designup.picsou.gui.projects.actions;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.globsframework.model.utils.GlobMatchers.*;

public class DeleteProjectAction extends MultiSelectionAction {

  public DeleteProjectAction(Key projectKey, GlobRepository repository, Directory directory) {
    super(projectKey, Project.TYPE, repository, directory);
  }

  public DeleteProjectAction(GlobRepository repository, Directory directory) {
    super(Project.TYPE, repository, directory);
  }

  protected String getLabel(GlobList selection) {
    return Lang.get("projectEdition.delete");
  }

  protected void processSelection(GlobList selection) {
    setEnabled(selection.getAll(Project.TYPE).size() == 1);
  }

  protected void processClick(GlobList selection, GlobRepository repository, Directory directory) {
    final Key currentProjectKey = selection.getFirst().getKey();
    Key seriesKey = repository.get(currentProjectKey).getTargetKey(Project.SERIES);
    final List<Key> transactionKeys =
      repository.getAll(Transaction.TYPE,
                        and(linkedTo(seriesKey, Transaction.SERIES),
                            isFalse(Transaction.PLANNED)))
        .getKeyList();

    if (!transactionKeys.isEmpty()) {
      ConfirmationDialog confirm = new ConfirmationDialog("projectEdition.deleteConfirmation.title",
                                                          Lang.get("projectEdition.deleteConfirmation.message"),
                                                          directory.get(JFrame.class), directory) {

        protected String getOkButtonText() {
          return Lang.get("projectEdition.deleteConfirmation.ok");
        }

        protected void processCustomLink(String href) {
          if (href.equals("seeOperations")) {
            dispose();
            showTransactions(transactionKeys);
          }
        }

        protected void processOk() {
          deleteProject(currentProjectKey, transactionKeys);
        }
      };
      confirm.show();
    }
    else {
      deleteProject(currentProjectKey, Collections.<Key>emptyList());
    }
  }

  private void deleteProject(Key currentProjectKey, List<Key> transactions) {
    repository.delete(currentProjectKey);
    if (!transactions.isEmpty()) {
      showTransactions(transactions);
    }
  }

  private void showTransactions(List<Key> transactionKeys) {
    selectMonthRange(transactionKeys);
    GlobList transactions = new GlobList();
    for (Key transactionKey : transactionKeys) {
      transactions.add(repository.get(transactionKey));
    }
    directory.get(NavigationService.class).gotoCategorization(transactions, false);
  }

  private void selectMonthRange(List<Key> transactionKeys) {
    SelectionService selectionService = directory.get(SelectionService.class);
    SortedSet<Integer> monthIds = new TreeSet<Integer>();
    for (Key transactionKey : transactionKeys) {
      monthIds.add(repository.get(transactionKey).get(Transaction.MONTH));
    }
    monthIds.addAll(selectionService.getSelection(Month.TYPE).getValueSet(Month.ID));

    GlobList months = new GlobList();
    for (Integer monthId : Month.range(monthIds.first(), monthIds.last())) {
      months.add(repository.get(Key.create(Month.TYPE, monthId)));
    }
    selectionService.select(months, Month.TYPE);
  }
}
