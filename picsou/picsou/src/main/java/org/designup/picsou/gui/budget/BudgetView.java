package org.designup.picsou.gui.budget;

import com.jidesoft.swing.JideSplitPane;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.PeriodOccasionalSeriesStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.impl.ReplicationGlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class BudgetView extends View implements GlobSelectionListener, ChangeSetListener {
  private GlobList selectedMonths = GlobList.EMPTY;
  private JideSplitPane horizontalSplitPane;
  private JideSplitPane firstVerticalSplitPane;
  private JideSplitPane secondVerticalSplitPane;
  private JideSplitPane thirdVerticalSplitPane;
  private JEditorPane multiSelectionWarning;

  public BudgetView(GlobRepository repository, Directory parentDirectory) {
    super(new ReplicationGlobRepository(repository, PeriodSeriesStat.TYPE, PeriodOccasionalSeriesStat.TYPE),
          createLocalDirectory(parentDirectory));
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
    addBudgetAreaView("recurringBudgetView", BudgetArea.RECURRING, builder);
    addBudgetAreaView("envelopeBudgetView", BudgetArea.ENVELOPES, builder);
    addBudgetAreaView("occasionalBudgetView", BudgetArea.OCCASIONAL, builder);
    addBudgetAreaView("projectsBudgetView", BudgetArea.SPECIAL, builder);
    addBudgetAreaView("savingsBudgetView", BudgetArea.SAVINGS, builder);

    horizontalSplitPane = builder.add("horizontalSplitPane", new JideSplitPane());
    firstVerticalSplitPane = builder.add("firstVerticalSplitPane", new JideSplitPane());
    secondVerticalSplitPane = builder.add("secondVerticalSplitPane", new JideSplitPane());
    thirdVerticalSplitPane = builder.add("thirdVerticalSplitPane", new JideSplitPane());

    multiSelectionWarning = builder.add("multiSelectionWarning", new JEditorPane());
    multiSelectionWarning.setVisible(false);

    parentBuilder.add("budgetView", builder);

    repository.addChangeListener(this);
  }

  private void addBudgetAreaView(String name, BudgetArea budgetArea, GlobsPanelBuilder builder) {
    View view;
    if (budgetArea == BudgetArea.OCCASIONAL) {
      view = new OccasionalSeriesView(name, repository, directory);
    }
    else {
      view = new BudgetAreaSeriesView(name, budgetArea, repository, directory);
    }
    view.registerComponents(builder);
  }

  public void selectionUpdated(GlobSelection selection) {
    selectedMonths = selection.getAll(Month.TYPE);
    updateSelection();
    updateWarningMessage();
  }

  private void updateSelection() {
    repository.enterBulkDispatchingMode();
    PeriodSeriesStatFunctor seriesStatFunctor = new PeriodSeriesStatFunctor(repository);
    try {
      repository.deleteAll(PeriodSeriesStat.TYPE);
      Set<Integer> monthIds = selectedMonths.getValueSet(Month.ID);

      repository.safeApply(SeriesStat.TYPE, GlobMatchers.fieldContained(SeriesStat.MONTH, monthIds),
                           seriesStatFunctor);
    }
    finally {
      repository.completeBulkDispatchingMode();
    }

    SelectionService localSelectionService = directory.get(SelectionService.class);
    localSelectionService.select(GlobSelectionBuilder.init()
      .add(selectedMonths, Month.TYPE)
      .add(seriesStatFunctor.getStats(), PeriodSeriesStat.TYPE).get());
  }

  private void updateWarningMessage() {
    multiSelectionWarning.setVisible(selectedMonths.size() > 1);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsCreationsOrDeletions(Month.TYPE)) {
      selectedMonths.removeAll(changeSet.getDeleted(Month.TYPE));
      updateSelection();
    }
    else if (changeSet.containsChanges(SeriesStat.TYPE)) {
      updateSelection();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    selectedMonths = GlobList.EMPTY;
  }

  private static class PeriodSeriesStatFunctor implements GlobFunctor {
    private Set<Glob> stats = new HashSet<Glob>();
    private GlobRepository repository;

    public PeriodSeriesStatFunctor(GlobRepository repository) {
      this.repository = repository;
    }

    public void run(Glob glob, GlobRepository remote) throws Exception {
      Glob stat =
        repository.findOrCreate(Key.create(PeriodSeriesStat.TYPE, glob.get(SeriesStat.SERIES)));
      double amount = stat.get(PeriodSeriesStat.AMOUNT) + glob.get(SeriesStat.AMOUNT);
      double plannedAmount = stat.get(PeriodSeriesStat.PLANNED_AMOUNT) + glob.get(SeriesStat.PLANNED_AMOUNT);
      repository.update(stat.getKey(),
                        FieldValue.value(PeriodSeriesStat.AMOUNT, amount),
                        FieldValue.value(PeriodSeriesStat.PLANNED_AMOUNT, plannedAmount),
                        FieldValue.value(PeriodSeriesStat.ABS_SUM_AMOUNT, Math.abs(plannedAmount) + Math.abs(amount))
      );
      stats.add(stat);
    }

    public GlobList getStats() {
      return new GlobList(stats);
    }
  }
}
