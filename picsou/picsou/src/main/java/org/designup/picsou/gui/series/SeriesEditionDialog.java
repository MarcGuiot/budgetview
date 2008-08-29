package org.designup.picsou.gui.series;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.categories.CategoryChooserCallback;
import org.designup.picsou.gui.categories.CategoryChooserDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.CellPainter;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

public class SeriesEditionDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private SelectionService selectionService;
  private BudgetArea budgetArea;
  private GlobStringifier stringifier;
  private GlobRepository repository;
  private Glob currentSeries;

  public SeriesEditionDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    stringifier = descriptionService.getStringifier(Series.TYPE);
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Category.TYPE, BudgetArea.TYPE, Month.TYPE)
      .get();

    localRepository.addTrigger(new SeriesBudgetTrigger());
    selectionService = new SelectionService();
    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);

    dialog = PicsouDialog.create(parent, Lang.get("seriesEdition.title"));
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/seriesEditionDialog.splits",
                                                      localRepository, localDirectory);

    builder.addEditor("nameField", Series.LABEL);

    builder.addLabel("singleCategoryLabel", Series.DEFAULT_CATEGORY);
    builder.add("assignCategoryAction", new AssignCategoryAction(dialog));

    builder.addEditor("beginSeriesDate", Series.FIRST_MONTH);
    builder.addEditor("endSeriesDate", Series.LAST_MONTH);

    builder.addEditor("amountEditor", SeriesBudget.AMOUNT);

    builder.addRepeat("monthRepeat", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                      new RepeatComponentFactory<Integer>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, final Integer item) {
                          cellBuilder.add("monthLabel", new JLabel(Month.getMediumSizeLetterLabelFromMonth(item)));
                          final JCheckBox checkbox = new JCheckBox();
                          selectionService.addListener(new GlobSelectionListener() {
                            public void selectionUpdated(GlobSelection selection) {
                              GlobList series = selection.getAll(Series.TYPE);
                              if (series.size() != 1) {
                                return;
                              }
                              checkbox.setSelected(series.get(0).get(Series.getField(item)));
                            }
                          }, Series.TYPE);
                          cellBuilder.add("monthSelector", checkbox);
                          checkbox.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                              localRepository.update(currentSeries.getKey(), Series.getField(item), e.getStateChange() == ItemEvent.SELECTED);
                            }
                          });
                        }
                      });

    builder.addTable("seriesBudget", SeriesBudget.TYPE, new GlobFieldComparator(SeriesBudget.MONTH))
      .setFilter(GlobMatchers.fieldEquals(SeriesBudget.ACTIVE, true))
      .setDefaultBackgroundPainter(new TableBackgroundPainter())
      .addColumn(Lang.get("seriesEdition.year"), new YearStringifier())
      .addColumn(Lang.get("seriesEdition.month"), new MonthStringifier())
      .addColumn(SeriesBudget.AMOUNT);

    JPanel panel = builder.load();
    dialog.addInPanelWithButton(panel, new ValidateAction(), new CancelAction());
  }

  public void show(Glob series, Set<Integer> monthIds) {
    try {
      localRepository.enterBulkDispatchingMode();
      localRepository.rollback();
      GlobList globs = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                              series.get(Series.ID)).getGlobs();
      globs.add(series);
      localRepository.reset(globs, SeriesBudget.TYPE, Series.TYPE);
      this.currentSeries = localRepository.get(series.getKey());
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }
    doShow(monthIds);
  }

  public void showInit(Glob series, GlobList transactions) {
    try {
      localRepository.enterBulkDispatchingMode();
      localRepository.rollback();
      GlobList globs = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                              series.get(Series.ID)).getGlobs();
      globs.add(series);
      localRepository.reset(globs, SeriesBudget.TYPE, Series.TYPE);
      this.currentSeries = localRepository.get(series.getKey());
      Double min = computeMinAmountPerMonth(transactions);
      SortedSet<Integer> days = transactions.getSortedSet(Transaction.DAY);
      budgetArea = BudgetArea.get(this.currentSeries.get(Series.BUDGET_AREA));
      String name = stringifier.toString(series, localRepository);
      localRepository.update(series.getKey(),
                             value(Series.INITIAL_AMOUNT, min),
                             value(Series.DAY, days.last()),
                             value(Series.LABEL, name),
                             value(Series.JANUARY, true),
                             value(Series.FEBRUARY, true),
                             value(Series.MARCH, true),
                             value(Series.APRIL, true),
                             value(Series.MAY, true),
                             value(Series.JUNE, true),
                             value(Series.JULY, true),
                             value(Series.AUGUST, true),
                             value(Series.SEPTEMBER, true),
                             value(Series.OCTOBER, true),
                             value(Series.NOVEMBER, true),
                             value(Series.DECEMBER, true));
      this.currentSeries = localRepository.get(series.getKey());
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }
    doShow(getCurrentMonthId());
  }

  public void showNewSeries(GlobList transactions, BudgetArea budget) {
    try {
      localRepository.enterBulkDispatchingMode();
      localRepository.rollback();
      budgetArea = BudgetArea.get(budget.getId());
      Double min = computeMinAmountPerMonth(transactions);
      SortedSet<Integer> days = transactions.getSortedSet(Transaction.DAY);
      Glob firstTransaction = transactions.get(0);
      String label = AllocationLearningService.anonymise(firstTransaction.get(Transaction.LABEL));
      currentSeries = localRepository.create(Series.TYPE,
                                             value(Series.BUDGET_AREA, budgetArea.getId()),
                                             value(Series.INITIAL_AMOUNT, min),
                                             value(Series.LABEL, label),
                                             value(Series.DAY, days.last()),
                                             value(Series.JANUARY, true),
                                             value(Series.FEBRUARY, true),
                                             value(Series.MARCH, true),
                                             value(Series.APRIL, true),
                                             value(Series.MAY, true),
                                             value(Series.JUNE, true),
                                             value(Series.JULY, true),
                                             value(Series.AUGUST, true),
                                             value(Series.SEPTEMBER, true),
                                             value(Series.OCTOBER, true),
                                             value(Series.NOVEMBER, true),
                                             value(Series.DECEMBER, true));
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }
    doShow(getCurrentMonthId());
  }

  private Set<Integer> getCurrentMonthId() {
    return Collections.singleton(localDirectory.get(TimeService.class).getCurrentMonthId());
  }

  private void doShow(Set<Integer> monthIds) {
    selectionService.select(currentSeries);
    selectionService.select(localRepository.getAll(SeriesBudget.TYPE,
                                                   fieldIn(SeriesBudget.MONTH, monthIds)), SeriesBudget.TYPE);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private Double computeMinAmountPerMonth(GlobList transactions) {
    Map<Integer, Double> amounts = new HashMap<Integer, Double>();
    Double min = 0.0;
    for (Glob transaction : transactions) {
      Integer month = transaction.get(Transaction.MONTH);
      Double value = amounts.get(month);
      Double amount = transaction.get(Transaction.AMOUNT);
      min = (value == null ? 0.0 : value) + (amount == null ? 0.0 : amount);
      amounts.put(month, min);
    }
    for (Double value : amounts.values()) {
      min = Math.min(min, value);
    }
    return min;
  }

  private class AssignCategoryAction extends AbstractAction {
    private Dialog parent;

    private AssignCategoryAction(Dialog parent) {
      this.parent = parent;
    }

    public void actionPerformed(ActionEvent e) {
      CategoryChooserDialog chooser =
        new CategoryChooserDialog(new SeriesCategoryChooserCallback(), parent, true,
                                  new TransactionRendererColors(localDirectory),
                                  localRepository, localDirectory);

      chooser.show();
    }

    private class SeriesCategoryChooserCallback implements CategoryChooserCallback {
      public void processSelection(GlobList categories) {
        for (Glob category : categories) {
          localRepository.setTarget(currentSeries.getKey(), Series.DEFAULT_CATEGORY, category.getKey());
          if (budgetArea == BudgetArea.EXPENSES_ENVELOPE) {
            localRepository.delete(localRepository.getAll(SeriesToCategory.TYPE,
                                                          GlobMatchers.linkedTo(currentSeries, SeriesToCategory.SERIES)));
            localRepository.create(SeriesToCategory.TYPE,
                                   value(SeriesToCategory.SERIES, currentSeries.get(Series.ID)),
                                   value(SeriesToCategory.CATEGORY, category.get(Category.ID)));
          }
        }
      }

      public Set<Integer> getPreselectedCategoryIds() {
        return Collections.emptySet();
      }
    }
  }

  private class YearStringifier extends AbstractGlobStringifier {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      return Integer.toString(Month.toYear(seriesBudget.get(SeriesBudget.MONTH)));
    }
  }

  private class MonthStringifier extends AbstractGlobStringifier {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      return Month.getMonthLabel(seriesBudget.get(SeriesBudget.MONTH));
    }
  }

  private class TableBackgroundPainter implements CellPainter {
    private TimeService timeService;

    private TableBackgroundPainter() {
      timeService = localDirectory.get(TimeService.class);
    }

    public void paint(Graphics g, Glob seriesBudget, int row, int column,
                      boolean isSelected, boolean hasFocus, int width, int height) {
      Color color;
      if (isSelected) {
        color = Gui.getDefaultTableSelectionBackground();
      }
      else {
        Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
        if (monthId == timeService.getCurrentMonthId()) {
          color = Colors.toColor("EEEEFF");
        }
        else {
          boolean even = Month.toYear(monthId) % 2 == 0;
          color = even ? Color.WHITE : Colors.toColor("EEEEEE");
        }
      }
      g.setColor(color);
      g.fillRect(0, 0, width, height);
    }
  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}
