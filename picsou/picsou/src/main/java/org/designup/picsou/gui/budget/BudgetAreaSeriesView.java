package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

public class BudgetAreaSeriesView extends View {
  private String name;
  private BudgetArea budgetArea;
  private GlobMatcher totalMatcher;
  private SeriesEditionDialog seriesEditionDialog;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private PicsouMatchers.SeriesFirstEndDateFilter seriesDateFilter;
  private GlobMatcher seriesFilter;
  private GlobRepeat seriesRepeat;

  protected BudgetAreaSeriesView(String name, final BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;
    this.totalMatcher = GlobMatchers.linkTargetFieldEquals(SeriesStat.SERIES, Series.BUDGET_AREA, budgetArea.getId());
    seriesEditionDialog = new SeriesEditionDialog(directory.get(JFrame.class), repository, directory);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        seriesDateFilter.filterDates(selectedMonthIds);
        seriesRepeat.setFilter(seriesFilter);
      }
    }, Month.TYPE);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SeriesStat.TYPE)) {
          seriesRepeat.setFilter(seriesFilter);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetAreaSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(stringify(budgetArea)));
    addTotalLabel("totalObservedAmount", SeriesStat.AMOUNT, builder);
    addTotalLabel("totalPlannedAmount", SeriesStat.PLANNED_AMOUNT, builder);

    final GlobGaugeView gaugeView = new GlobGaugeView(SeriesStat.TYPE, budgetArea, SeriesStat.AMOUNT, SeriesStat.PLANNED_AMOUNT,
                                                      totalMatcher, repository, directory);
    builder.add("totalGauge", gaugeView.getComponent());

    seriesRepeat =
      builder.addRepeat("seriesRepeat",
                        Series.TYPE,
                        GlobMatchers.fieldEquals(Series.BUDGET_AREA, budgetArea.getId()),
                        new RepeatComponentFactory<Glob>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, Glob series) {
                            final GlobButtonView globButtonView = GlobButtonView.init(Series.TYPE, repository, directory, new EditSeriesFunctor())
                              .forceSelection(series);
                            cellBuilder.add("seriesName", globButtonView.getComponent());
                            addAmountLabel("observedSeriesAmount", SeriesStat.AMOUNT, series, cellBuilder);
                            addAmountLabel("plannedSeriesAmount", SeriesStat.PLANNED_AMOUNT, series, cellBuilder);

                            final GlobGaugeView gaugeView =
                              new GlobGaugeView(SeriesStat.TYPE, budgetArea, SeriesStat.AMOUNT, SeriesStat.PLANNED_AMOUNT,
                                                GlobMatchers.fieldEquals(SeriesStat.SERIES, series.get(Series.ID)),
                                                repository, directory);
                            cellBuilder.add("gauge", gaugeView.getComponent());
                            cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
                              public void dispose() {
                                gaugeView.dispose();
                                globButtonView.dispose();
                              }
                            });
                          }
                        });

    builder.add("createSeries", new CreateSeriesAction());

    builder.add("editAllSeries",
                new EditSeriesAction(repository, directory, seriesEditionDialog, budgetArea));

    parentBuilder.add(name, builder);
    seriesDateFilter = PicsouMatchers.seriesDateFilter(budgetArea.getId(), false);
    seriesFilter = new GlobMatcher() {
      public boolean matches(Glob series, GlobRepository repository) {
        if (!seriesDateFilter.matches(series, repository)) {
          return false;
        }
        for (Integer id : selectedMonthIds) {
          Glob seriesStat = repository.find(Key.create(SeriesStat.SERIES, series.get(Series.ID), SeriesStat.MONTH, id));
          if (seriesStat != null) {
            if (seriesStat.get(SeriesStat.AMOUNT) != 0.0 || seriesStat.get(SeriesStat.PLANNED_AMOUNT) != 0.0) {
              return true;
            }
          }
        }
        return false;
      }
    };
    seriesRepeat.setFilter(seriesFilter);
  }

  private String stringify(BudgetArea budgetArea) {
    return descriptionService.getStringifier(BudgetArea.TYPE).toString(budgetArea.getGlob(), repository);
  }

  private void addTotalLabel(String name, DoubleField field, GlobsPanelBuilder builder) {
    builder.addLabel(name, SeriesStat.TYPE, getStringifier(field))
      .setFilter(totalMatcher);
  }

  private void addAmountLabel(String name, DoubleField field, Glob series, RepeatCellBuilder cellBuilder) {
    final GlobLabelView globLabelView = GlobLabelView.init(SeriesStat.TYPE, repository, directory, getStringifier(field))
      .setFilter(GlobMatchers.linkedTo(series, SeriesStat.SERIES));
    cellBuilder.add(name, globLabelView.getComponent());
    cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
      public void dispose() {
        globLabelView.dispose();
      }
    });
  }

  private GlobListStringifier getStringifier(final DoubleField field) {
    return GlobListStringifiers.sum(field, decimalFormat, !budgetArea.isIncome());
  }

  private class EditSeriesFunctor implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      seriesEditionDialog.show(list.getFirst(), selectedMonthIds);
    }
  }

  private class CreateSeriesAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      seriesEditionDialog.showNewSeries(GlobList.EMPTY, budgetArea);
    }
  }


}
