package org.designup.picsou.gui.budget;

import com.jidesoft.swing.JideSplitPane;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
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
  private JideSplitPane horizontalSplitPane;
  private JideSplitPane firstVerticalSplitPane;
  private JideSplitPane secondVerticalSplitPane;
  private JideSplitPane thirdVerticalSplitPane;

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

    horizontalSplitPane = new JideSplitPane();
    builder.add("horizontalSplitPane", horizontalSplitPane);
    firstVerticalSplitPane = new JideSplitPane();
    builder.add("firstVerticalSplitPane", firstVerticalSplitPane);
    secondVerticalSplitPane = new JideSplitPane();
    builder.add("secondVerticalSplitPane", secondVerticalSplitPane);
    thirdVerticalSplitPane = new JideSplitPane();
    builder.add("thirdVerticalSplitPane", thirdVerticalSplitPane);
    addBudgetAreaView("incomeBudgetView", BudgetArea.INCOME, builder);

    addBudgetAreaView("recurringBudgetView", BudgetArea.RECURRING, builder);

    addBudgetAreaView("envelopeBudgetView", BudgetArea.ENVELOPES, builder);

    addBudgetAreaView("occasionalBudgetView", BudgetArea.OCCASIONAL, builder);

    addBudgetAreaView("projectsBudgetView", BudgetArea.SPECIAL, builder);

    addBudgetAreaView("savingsBudgetView", BudgetArea.SAVINGS, builder);

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
    currentSelectedMonth = selection.getAll(Month.TYPE);
    updateSelection();
  }

  private void updateSelection() {
    Set<Integer> monthIds = currentSelectedMonth.getValueSet(Month.ID);
    GlobList seriesStats = repository.getAll(SeriesStat.TYPE, GlobMatchers.fieldContained(SeriesStat.MONTH, monthIds));

    SelectionService localSelectionService = directory.get(SelectionService.class);
    localSelectionService.select(GlobSelectionBuilder.init()
      .add(currentSelectedMonth, Month.TYPE)
      .add(seriesStats, SeriesStat.TYPE).get());
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsCreationsOrDeletions(Month.TYPE)) {
      currentSelectedMonth.removeAll(changeSet.getDeleted(Month.TYPE));
      updateSelection();
    }
    else if (changeSet.containsCreationsOrDeletions(SeriesStat.TYPE)) {
      updateSelection();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    currentSelectedMonth = GlobList.EMPTY;
  }
}
