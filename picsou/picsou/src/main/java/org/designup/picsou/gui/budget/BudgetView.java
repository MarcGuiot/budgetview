package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class BudgetView extends View implements GlobSelectionListener, ChangeSetListener {
  private GlobList currentSelectedMonth = GlobList.EMPTY;

  public BudgetView(GlobRepository repository, Directory parentDirectory) {
    super(repository, createLocalDirectory(parentDirectory));
    parentDirectory.get(SelectionService.class).addListener(this, Month.TYPE);
  }

  private static DefaultDirectory createLocalDirectory(Directory directory) {
    final DefaultDirectory localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());
    return localDirectory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetView.splits",
                                                      repository, directory);

    addBudgetAreaView("incomeBudgetView", BudgetArea.INCOME, builder);

    addBudgetAreaView("recurringBudgetView", BudgetArea.RECURRING_EXPENSES, builder);

    addBudgetAreaView("envelopeBudgetView", BudgetArea.EXPENSES_ENVELOPE, builder);

    addBudgetAreaView("occasionalBudgetView", BudgetArea.OCCASIONAL_EXPENSES, builder);

    parentBuilder.add("budgetView", builder);

    repository.addChangeListener(this);
  }

  private void addBudgetAreaView(String name, BudgetArea budgetArea, GlobsPanelBuilder builder) {
    View view;
    if (budgetArea == BudgetArea.OCCASIONAL_EXPENSES) {
      view = new OccasionalSeriesView(name, repository, directory);
    }
    else {
      view = new BudgetAreaSeriesView(name, budgetArea, repository, directory);
    }
    view.registerComponents(builder);
  }

  public void selectionUpdated(GlobSelection selection) {
    currentSelectedMonth = selection.getAll(Month.TYPE);
    updateSelection();
  }

  private void updateSelection() {
    Set<Integer> monthIds = currentSelectedMonth.getValueSet(Month.ID);
    GlobList seriesStats = repository.getAll(SeriesStat.TYPE, GlobMatchers.fieldContained(SeriesStat.MONTH, monthIds));

    GlobList localSelection = new GlobList();
    localSelection.addAll(currentSelectedMonth);
    localSelection.addAll(seriesStats);
    SelectionService localSelectionService = directory.get(SelectionService.class);
    localSelectionService.select(localSelection, SeriesStat.TYPE, Month.TYPE);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsCreationsOrDeletions(SeriesStat.TYPE)) {
      if (changeSet.containsCreationsOrDeletions(Month.TYPE)) {
        currentSelectedMonth.removeAll(changeSet.getDeleted(Month.TYPE));
      }
      updateSelection();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    currentSelectedMonth = GlobList.EMPTY;
  }
}
