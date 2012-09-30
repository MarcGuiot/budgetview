package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;

import java.util.Set;
import java.util.Collections;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class BudgetAreaHeader {

  private BudgetArea budgetArea;
  private BudgetAreaHeaderUpdater headerUpdater;
  private GlobRepository repository;

  private Set<Integer> selectedMonthIds = Collections.emptySet();

  public static BudgetAreaHeader init(BudgetArea budgetArea,
                                      BudgetAreaHeaderUpdater headerUpdater,
                                      GlobRepository repository,
                                      Directory directory) {
    return new BudgetAreaHeader(budgetArea, headerUpdater, repository, directory);
  }

  private BudgetAreaHeader(BudgetArea budgetArea,
                           BudgetAreaHeaderUpdater headerUpdater,
                           GlobRepository repository,
                           Directory directory) {
    this.budgetArea = budgetArea;
    this.headerUpdater = headerUpdater;
    this.repository = repository;

    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        updateHeader();
      }
    }, Month.TYPE);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BudgetStat.TYPE)) {
          updateHeader();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(BudgetStat.TYPE)) {
          updateHeader();
        }
      }
    });

    updateHeader();
  }

  private void updateHeader() {
    GlobList budgetStat = new GlobList();
    budgetStat.addAll(repository.getAll(BudgetStat.TYPE, fieldIn(BudgetStat.MONTH, selectedMonthIds)));
    headerUpdater.update(budgetStat, budgetArea);
  }

}
