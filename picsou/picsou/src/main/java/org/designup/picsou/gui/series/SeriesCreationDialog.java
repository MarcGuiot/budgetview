package org.designup.picsou.gui.series;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.transactions.categorization.CategoryChooserCallback;
import org.designup.picsou.gui.transactions.categorization.CategoryChooserDialog;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobLabelView;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
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

public class SeriesCreationDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private Glob series;
  private SelectionService selectionService;
  private BudgetArea budgetArea;
  private GlobStringifier stringifier;
  private GlobRepository repository;

  public SeriesCreationDialog(Window parent, final GlobRepository repository, Directory directory) {
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

    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesCreationDialog.class,
                                                      "/layout/seriesCreationDialog.splits",
                                                      localRepository, localDirectory);

    builder.addEditor("nameField", Series.LABEL);

    builder.add("assignCategoryAction", new AssignCategoryAction());

    builder.addEditor("beginSeriesDate", Series.FIRST_MONTH);
    builder.addEditor("endSeriesDate", Series.LAST_MONTH);

    builder.addEditor("amountEditor", Series.AMOUNT);

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
                          cellBuilder.add("monthSelected", checkbox);
                          checkbox.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                              localRepository.update(series.getKey(), Series.getField(item), e.getStateChange() == ItemEvent.SELECTED);
                            }
                          });
                        }
                      });

    builder.addRepeat("budgetSeries", SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.ACTIVE, true),
                      new GlobFieldComparator(SeriesBudget.MONTH), new RepeatComponentFactory<Glob>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, Glob item) {
        cellBuilder.add("budgetMonth",
                        GlobLabelView.init(SeriesBudget.MONTH, localRepository, localDirectory)
                          .forceSelection(item)
                          .getComponent());
        cellBuilder.add("budgetAmount",
                        GlobNumericEditor.init(SeriesBudget.AMOUNT, localRepository, localDirectory)
                          .forceSelection(item)
                          .getComponent());
      }
    });

    builder.add("ok", new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        localRepository.commitChanges(false);
        dialog.setVisible(false);
      }
    });

    builder.add("cancel", new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });

    JPanel panel = builder.load();
    dialog = PicsouDialog.create(parent, Lang.get("seriesCreation.title"));
    dialog.setContentPane(panel);
  }

  public void show(Glob series, GlobList transactions) {
    localRepository.rollback();
    localRepository.reset(new GlobList(series), Series.TYPE);
    localRepository.reset(repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                                 series.get(Series.ID)).getGlobs(),
                          SeriesBudget.TYPE);
    this.series = localRepository.get(series.getKey());
    Double min = computeMinAmountPerMonth(transactions);
    SortedSet<Integer> days = transactions.getSortedSet(Transaction.DAY);
    budgetArea = BudgetArea.get(this.series.get(Series.BUDGET_AREA));
    String name = stringifier.toString(series, localRepository);
    localRepository.update(series.getKey(),
                           value(Series.AMOUNT, min),
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
    selectionService.select(localRepository.get(series.getKey()));
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

  public void show(GlobList transactions, BudgetArea budget) {
    localRepository.rollback();
    budgetArea = BudgetArea.get(budget.getId());
    Double min = computeMinAmountPerMonth(transactions);
    SortedSet<Integer> days = transactions.getSortedSet(Transaction.DAY);
    Glob firstTransaction = transactions.get(0);
    String label = AllocationLearningService.anonymise(firstTransaction.get(Transaction.LABEL));
    series = localRepository.create(Series.TYPE,
                                    value(Series.BUDGET_AREA, budgetArea.getId()),
                                    value(Series.AMOUNT, min),
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
    selectionService.select(series);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private class AssignCategoryAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      CategoryChooserDialog chooser =
        new CategoryChooserDialog(new SeriesCategoryChooserCallback(),
                                  new TransactionRendererColors(localDirectory),
                                  localRepository, localDirectory);

      chooser.show();
    }

    private class SeriesCategoryChooserCallback implements CategoryChooserCallback {
      public void categorySelected(Glob category) {
        localRepository.setTarget(series.getKey(), Series.DEFAULT_CATEGORY, category.getKey());
        if (budgetArea == BudgetArea.EXPENSES_ENVELOPE) {
          localRepository.delete(localRepository.getAll(SeriesToCategory.TYPE,
                                                        GlobMatchers.linkedTo(series, SeriesToCategory.SERIES)));
          localRepository.create(SeriesToCategory.TYPE,
                                 value(SeriesToCategory.SERIES, series.get(Series.ID)),
                                 value(SeriesToCategory.CATEGORY, category.get(Category.ID)));
        }
      }

      public Set<Integer> getPreselectedCategoryIds() {
        return Collections.emptySet();
      }
    }
  }
}
