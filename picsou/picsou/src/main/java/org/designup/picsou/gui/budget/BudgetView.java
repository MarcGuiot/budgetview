package org.designup.picsou.gui.budget;

import com.jidesoft.swing.JideSplitPane;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.PeriodOccasionalSeriesStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.utils.SetFieldValueAction;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import static org.globsframework.model.utils.GlobMatchers.not;
import org.globsframework.model.utils.ReplicationGlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class BudgetView extends View implements GlobSelectionListener, ChangeSetListener {
  private GlobList selectedMonths = GlobList.EMPTY;
  private GlobRepository parentRepository;
  private Directory parentDirectory;
  private JEditorPane helpMessage;

  public BudgetView(GlobRepository repository, Directory parentDirectory) {
    super(new ReplicationGlobRepository(repository,
                                        PeriodSeriesStat.TYPE,
                                        PeriodOccasionalSeriesStat.TYPE),
          createLocalDirectory(parentDirectory));
    this.parentRepository = repository;
    this.parentDirectory = parentDirectory;
    parentDirectory.get(SelectionService.class).addListener(this, Month.TYPE);
    parentRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Series.TYPE)) {
          updateHelpMessage();
        }
      }
    });
  }

  private static DefaultDirectory createLocalDirectory(Directory directory) {
    final DefaultDirectory localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());
    return localDirectory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetView.splits",
                                                      repository, parentDirectory);

    SeriesEditionDialog seriesEditionDialog = new SeriesEditionDialog(directory.get(JFrame.class), repository, directory);

    BudgetLabel budgetLabel = new BudgetLabel(repository, parentDirectory);
    builder.add("budgetLabel", budgetLabel.getLabel());

    addBudgetAreaView("incomeBudgetView", BudgetArea.INCOME, builder, seriesEditionDialog);
    addBudgetAreaView("recurringBudgetView", BudgetArea.RECURRING, builder, seriesEditionDialog);
    addBudgetAreaView("envelopeBudgetView", BudgetArea.ENVELOPES, builder, seriesEditionDialog);
    addBudgetAreaView("occasionalBudgetView", BudgetArea.OCCASIONAL, builder, seriesEditionDialog);
    addBudgetAreaView("projectsBudgetView", BudgetArea.SPECIAL, builder, seriesEditionDialog);
    addBudgetAreaView("savingsBudgetView", BudgetArea.SAVINGS, builder, seriesEditionDialog);

    builder.add("horizontalSplitPane", new JideSplitPane());
    builder.add("firstVerticalSplitPane", new JideSplitPane());
    builder.add("secondVerticalSplitPane", new JideSplitPane());
    builder.add("thirdVerticalSplitPane", new JideSplitPane());

    createHelpMessage();
    builder.add("helpMessage", helpMessage);
    builder.add("hideHelpMessage",
                new SetFieldValueAction(UserPreferences.KEY, UserPreferences.SHOW_BUDGET_VIEW_HELP_MESSAGE,
                                        false, repository));

    builder.add("hyperlinkHandler", new HyperlinkHandler(parentDirectory));

    parentBuilder.add("budgetView", builder);

    repository.addChangeListener(this);
  }

  private void addBudgetAreaView(String name, BudgetArea budgetArea, GlobsPanelBuilder builder, final SeriesEditionDialog seriesEditionDialog) {
    View view;
    if (budgetArea == BudgetArea.OCCASIONAL) {
      view = new OccasionalSeriesView(name, repository, directory);
    }
    else if (budgetArea == BudgetArea.SAVINGS) {
      view = new BudgetAreaSeriesView(name, budgetArea, repository, directory,
                                      seriesEditionDialog);
    }
    else {
      view = new BudgetAreaSeriesView(name, budgetArea, repository, directory,
                                      seriesEditionDialog);
    }
    view.registerComponents(builder);
  }

  private JEditorPane createHelpMessage() {
    helpMessage = new JEditorPane();
    helpMessage.setContentType("text/html");
    PicsouColors.installLinkColor(helpMessage, "mainpanel", "mainpanel.message.link", directory);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.SHOW_BUDGET_VIEW_HELP_MESSAGE)) {
          updateHelpMessage();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        updateHelpMessage();
      }
    });
    updateHelpMessage();
    return helpMessage;
  }

  private void updateHelpMessage() {
    Glob prefs = repository.find(UserPreferences.KEY);
    if (prefs != null) {
      helpMessage.setVisible(Boolean.TRUE.equals(prefs.get(UserPreferences.SHOW_BUDGET_VIEW_HELP_MESSAGE)) &&
                             parentRepository.contains(Series.TYPE,
                                                       not(fieldIn(Series.ID,
                                                                   Series.OCCASIONAL_SERIES_ID,
                                                                   Series.UNCATEGORIZED_SERIES_ID))));
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    selectedMonths = selection.getAll(Month.TYPE);
    updateSelection();
  }

  private void updateSelection() {
    repository.startChangeSet();
    PeriodSeriesStatFunctor seriesStatFunctor = new PeriodSeriesStatFunctor(repository);
    try {
      repository.deleteAll(PeriodSeriesStat.TYPE);
      Set<Integer> monthIds = selectedMonths.getValueSet(Month.ID);

      repository.safeApply(SeriesStat.TYPE, GlobMatchers.fieldContained(SeriesStat.MONTH, monthIds),
                           seriesStatFunctor);
    }
    finally {
      repository.completeChangeSet();
    }

    SelectionService localSelectionService = directory.get(SelectionService.class);
    localSelectionService.select(GlobSelectionBuilder.init()
      .add(selectedMonths, Month.TYPE)
      .add(seriesStatFunctor.getStats(), PeriodSeriesStat.TYPE).get());
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
                        FieldValue.value(PeriodSeriesStat.ABS_SUM_AMOUNT, Math.abs(plannedAmount) + Math.abs(amount)));
      stats.add(stat);
    }

    public GlobList getStats() {
      return new GlobList(stats);
    }
  }
}
