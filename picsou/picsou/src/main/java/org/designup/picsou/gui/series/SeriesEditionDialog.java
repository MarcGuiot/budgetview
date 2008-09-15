package org.designup.picsou.gui.series;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.categories.CategoryChooserCallback;
import org.designup.picsou.gui.categories.CategoryChooserDialog;
import org.designup.picsou.gui.components.MonthChooserDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.ReadOnlyGlobTextFieldView;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.*;
import org.globsframework.gui.views.impl.StringListCellRenderer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.*;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Strings;

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
  private GlobRepository repository;
  private Glob currentSeries;
  private GlobListView seriesList;
  private JTextField singleCategoryField;
  private GlobListView multiCategoryList;
  private SeriesEditionDialog.AssignCategoryAction assignCategoryAction;
  private SeriesEditionDialog.ValidateAction okAction;
  private AbstractAction deleteBeginDateAction;
  private SeriesEditionDialog.CalendarAction beginDateCalendar;
  private AbstractAction deleteEndDateAction;
  private SeriesEditionDialog.CalendarAction endDateCalendar;
  private GlobTextEditor nameEditor;
  private GlobNumericEditor amountEditor;
  private TimeService timeService;
  private JLabel categoryLabel;

  public SeriesEditionDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Category.TYPE, BudgetArea.TYPE, Month.TYPE, SeriesToCategory.TYPE)
      .get();

    localRepository.addTrigger(new SeriesBudgetTrigger());
    selectionService = new SelectionService();
    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);

    this.timeService = localDirectory.get(TimeService.class);

    dialog = PicsouDialog.create(parent, Lang.get("seriesEdition.title"), directory);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/seriesEditionDialog.splits",
                                                      localRepository, localDirectory);

    GlobStringifier seriesStringifier = descriptionService.getStringifier(Series.TYPE);
    seriesList = GlobListView.init(Series.TYPE, localRepository, localDirectory)
      .setRenderer(new StringListCellRenderer(seriesStringifier, localRepository) {
        public Component getListCellRendererComponent(JList list, Object object, int index, boolean isSelected, boolean cellHasFocus) {
          Component component = super.getListCellRendererComponent(list, object, index, isSelected, cellHasFocus);
          Glob glob = (Glob)object;
          if (glob != null && glob.get(Series.DEFAULT_CATEGORY) == null) {
            component.setForeground(Color.RED);
          }
          return component;
        }
      },
                   seriesStringifier.getComparator(localRepository));
    builder.add("seriesList", seriesList.getComponent());

    builder.add("create", new CreateSeriesAction());
    builder.add("delete", new DeleteSeriesAction());

    nameEditor = builder.addEditor("nameField", Series.LABEL);

    registerCategoryComponents(descriptionService, builder);

    registerDateComponents(builder);

    amountEditor = builder.addEditor("amountEditor", SeriesBudget.AMOUNT).setMinusAllowed(false);

    final GlobTableView budgetTable = builder.addTable("seriesBudget", SeriesBudget.TYPE,
                                                       new ReverseGlobFieldComparator(SeriesBudget.MONTH))
      .setFilter(fieldEquals(SeriesBudget.ACTIVE, true))
      .setDefaultBackgroundPainter(new TableBackgroundPainter())
      .setDefaultLabelCustomizer(new SeriesBudgetLabelCustomizer())
      .addColumn(Lang.get("seriesEdition.year"), new YearStringifier())
      .addColumn(Lang.get("seriesEdition.month"), new MonthStringifier())
      .addColumn(Lang.get("seriesBudget.amount"), new AmountStringifier(), LabelCustomizers.ALIGN_RIGHT);
    PicsouTableHeaderPainter.install(budgetTable, localDirectory);

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentSeries = selection.getAll(Series.TYPE).getFirst();
        if (currentSeries != null) {
          budgetTable.setFilter(GlobMatchers.and(fieldEquals(SeriesBudget.ACTIVE, true),
                                                 fieldEquals(SeriesBudget.SERIES, currentSeries.get(Series.ID))));
          assignCategoryAction.setEnabled(true);
          multiCategoryList.setFilter(GlobMatchers.fieldEquals(SeriesToCategory.SERIES, currentSeries.get(Series.ID)));
        }
        else {
          budgetTable.setFilter(GlobMatchers.NONE);
          assignCategoryAction.setEnabled(false);
          multiCategoryList.setFilter(GlobMatchers.NONE);
        }
        updateDateState();
      }
    }, Series.TYPE);

    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        updateDateState();
      }
    });

    builder.addRepeat("monthRepeat", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                      new RepeatComponentFactory<Integer>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, final Integer monthIndex) {
                          cellBuilder.add("monthLabel", new JLabel(Month.getShortMonthLabel(monthIndex)));
                          MonthCheckBoxUpdater updater = new MonthCheckBoxUpdater(monthIndex);
                          cellBuilder.add("monthSelector", updater.getCheckBox());
                          localRepository.addChangeListener(updater);
                        }
                      });

    builder.addLabel("seriesEditionAmountLabel", SeriesBudget.TYPE, new AmountLabelStringifier());

    localRepository.addChangeListener(new OkButtonUpdater());

    JPanel panel = builder.load();
    okAction = new ValidateAction();
    dialog.addInPanelWithButton(panel, okAction, new CancelAction());
  }

  private void registerCategoryComponents(DescriptionService descriptionService, GlobsPanelBuilder builder) {
    final GlobStringifier categoryStringifier = descriptionService.getStringifier(Category.TYPE);

    categoryLabel = builder.add("categoryLabel", new JLabel());

    builder.add("missingCategoryLabel",
                GlobLabelView.init(Series.TYPE, localRepository, localDirectory, new MissingCategoryStringifier())
                  .setAutoHideIfEmpty(true)
                  .getComponent());

    multiCategoryList = GlobListView.init(SeriesToCategory.TYPE, localRepository, localDirectory)
      .setComparator(new GlobLinkComparator(SeriesToCategory.CATEGORY, localRepository,
                                            categoryStringifier.getComparator(localRepository)))
      .setRenderer(new SeriesToCategoryStringifier(categoryStringifier));
    builder.add("multipleCategoryList", multiCategoryList);

    singleCategoryField =
      ReadOnlyGlobTextFieldView.init(Series.DEFAULT_CATEGORY, localRepository, localDirectory)
        .getComponent();
    builder.add("singleCategoryField", singleCategoryField);

    assignCategoryAction = new AssignCategoryAction(dialog);
    builder.add("assignCategory", assignCategoryAction);
  }

  private void registerDateComponents(GlobsPanelBuilder builder) {

    builder.add("beginSeriesDate",
                ReadOnlyGlobTextFieldView.init(Series.TYPE, localRepository, localDirectory,
                                               new MonthYearStringifier(Series.FIRST_MONTH)));

    deleteBeginDateAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        localRepository.update(currentSeries.getKey(), Series.FIRST_MONTH, null);
      }
    };
    builder.add("deleteBeginSeriesDate", deleteBeginDateAction);

    beginDateCalendar = new CalendarAction(Series.FIRST_MONTH, -1) {
      protected Integer getMonthLimit() {
        GlobList transactions = localRepository.findByIndex(Transaction.SERIES_INDEX, currentSeries.get(Series.ID))
          .filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, false), localRepository)
          .sort(Transaction.MONTH);
        Glob firstMonth = transactions.getFirst();
        if (firstMonth == null) {
          return currentSeries.get(Series.LAST_MONTH);
        }
        if (currentSeries.get(Series.LAST_MONTH) != null) {
          return Math.min(firstMonth.get(Transaction.MONTH), currentSeries.get(Series.LAST_MONTH));
        }
        return firstMonth.get(Transaction.MONTH);
      }
    };
    builder.add("beginSeriesCalendar", beginDateCalendar);

    builder.add("endSeriesDate",
                ReadOnlyGlobTextFieldView.init(Series.TYPE, localRepository, localDirectory,
                                               new MonthYearStringifier(Series.LAST_MONTH)));

    deleteEndDateAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        localRepository.update(currentSeries.getKey(), Series.LAST_MONTH, null);
      }
    };
    builder.add("deleteEndSeriesDate", deleteEndDateAction);

    endDateCalendar = new CalendarAction(Series.LAST_MONTH, 1) {
      protected Integer getMonthLimit() {
        GlobList transactions = localRepository.findByIndex(Transaction.SERIES_INDEX, currentSeries.get(Series.ID))
          .sort(Transaction.MONTH);
        Glob lastMonth = transactions.getLast();
        if (lastMonth == null) {
          return currentSeries.get(Series.FIRST_MONTH);
        }
        if (currentSeries.get(Series.FIRST_MONTH) != null) {
          return Math.max(lastMonth.get(Transaction.MONTH), currentSeries.get(Series.FIRST_MONTH));
        }
        return lastMonth.get(Transaction.MONTH);
      }
    };
    builder.add("endSeriesCalendar", endDateCalendar);
  }

  private void updateDateState() {
    beginDateCalendar.setEnabled(currentSeries != null);
    boolean b = currentSeries != null && currentSeries.get(Series.FIRST_MONTH) != null;
    deleteBeginDateAction.setEnabled(b);
    endDateCalendar.setEnabled(currentSeries != null);
    deleteEndDateAction.setEnabled(currentSeries != null && currentSeries.get(Series.LAST_MONTH) != null);
  }

  public void show(BudgetArea budgetArea, Set<Integer> monthIds) {
    try {
      localRepository.enterBulkDispatchingMode();
      localRepository.rollback();
      initBudgetAreaSeries(budgetArea);
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }
    doShow(monthIds, null, null);
  }

  public void show(Glob series, Set<Integer> monthIds) {
    try {
      localRepository.enterBulkDispatchingMode();
      localRepository.rollback();
      initBudgetAreaSeries(BudgetArea.get(series.get(Series.BUDGET_AREA)));
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }
    doShow(monthIds, localRepository.get(series.getKey()), false);
  }

  public void showNewSeries(GlobList transactions, BudgetArea budgetArea) {
    this.budgetArea = BudgetArea.get(budgetArea.getId());
    Glob createdSeries;
    try {
      localRepository.enterBulkDispatchingMode();
      localRepository.rollback();
      initBudgetAreaSeries(budgetArea);

      Double initialAmount = computeMinAmountPerMonth(transactions);
      String label;
      if (!transactions.isEmpty()) {
        Glob firstTransaction = transactions.get(0);
        label = Transaction.anonymise(firstTransaction.get(Transaction.LABEL));
      }
      else {
        label = Lang.get("seriesEdition.newSeries");
      }
      SortedSet<Integer> days = transactions.getSortedSet(Transaction.DAY);
      Integer day = days.isEmpty() ? 1 : days.last();
      createdSeries = createSeries(label, initialAmount, day);
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }
    doShow(getCurrentMonthId(), createdSeries, true);
  }

  private Glob createSeries(String label, Double initialAmount, Integer day) {
    return localRepository.create(Series.TYPE,
                                  value(Series.BUDGET_AREA, budgetArea.getId()),
                                  value(Series.INITIAL_AMOUNT, initialAmount),
                                  value(Series.LABEL, label),
                                  value(Series.DAY, day),
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

  private void initBudgetAreaSeries(BudgetArea budgetArea) {
    this.budgetArea = budgetArea;
    initCategorizeVisibility();
    GlobList seriesList =
      repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, budgetArea.getId()));

    GlobList globsToLoad = new GlobList();
    for (Glob series : seriesList) {
      globsToLoad.add(series);
      globsToLoad.addAll(repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                                series.get(Series.ID)).getGlobs());
      globsToLoad.addAll(repository.findByIndex(Transaction.SERIES_INDEX, series.get(Series.ID))
        .filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, false), repository));
    }
    localRepository.reset(globsToLoad, SeriesBudget.TYPE, Series.TYPE, Transaction.TYPE);
  }

  private void initCategorizeVisibility() {
    this.categoryLabel.setText(Lang.get("seriesEdition.category.label." +
                                        (budgetArea.isMultiCategories() ? "multiple" : "single")));
    this.singleCategoryField.setVisible(!budgetArea.isMultiCategories());
    this.multiCategoryList.setVisible(budgetArea.isMultiCategories());
  }

  private Set<Integer> getCurrentMonthId() {
    return Collections.singleton(localDirectory.get(TimeService.class).getCurrentMonthId());
  }

  private void doShow(Set<Integer> monthIds, Glob series, final Boolean selectName) {
    this.currentSeries = series;
    if (series != null) {
      selectionService.select(series);
    }
    else {
      seriesList.selectFirst();
    }
    if (budgetArea.isIncome()) {
      amountEditor.setInvertValue(false);
    }
    else {
      amountEditor.setInvertValue(true);
    }
    selectionService.select(localRepository.getAll(SeriesBudget.TYPE,
                                                   fieldIn(SeriesBudget.MONTH, monthIds)), SeriesBudget.TYPE);
    dialog.pack();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (selectName != null) {
          if (selectName) {
            nameEditor.getComponent().requestFocusInWindow();
            nameEditor.getComponent().selectAll();
          }
          else {
            amountEditor.getComponent().requestFocusInWindow();
            amountEditor.getComponent().selectAll();
          }
        }
      }
    });
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

  public Window getDialog() {
    return dialog;
  }

  private class AssignCategoryAction extends AbstractAction {
    private Dialog parent;

    private AssignCategoryAction(Dialog parent) {
      super(Lang.get("seriesEdition.categorize"));
      this.parent = parent;
    }

    public void show() {
      CategoryChooserDialog chooser =
        new CategoryChooserDialog(new SeriesCategoryChooserCallback(), parent,
                                  !budgetArea.isMultiCategories(),
                                  localRepository, localDirectory);

      chooser.show();
    }

    public void actionPerformed(ActionEvent e) {
      show();
    }

    private class SeriesCategoryChooserCallback implements CategoryChooserCallback {
      public void processSelection(GlobList categories) {
        localRepository.enterBulkDispatchingMode();
        try {
          localRepository.delete(localRepository.getAll(SeriesToCategory.TYPE,
                                                        GlobMatchers.linkedTo(currentSeries, SeriesToCategory.SERIES)));
          localRepository.setTarget(currentSeries.getKey(), Series.DEFAULT_CATEGORY, null);
          for (Glob category : categories) {
            localRepository.setTarget(currentSeries.getKey(), Series.DEFAULT_CATEGORY, category.getKey());
            if (budgetArea.isMultiCategories()) {
              localRepository.create(SeriesToCategory.TYPE,
                                     value(SeriesToCategory.SERIES, currentSeries.get(Series.ID)),
                                     value(SeriesToCategory.CATEGORY, category.get(Category.ID)));
            }
          }
        }
        finally {
          localRepository.completeBulkDispatchingMode();
        }

      }

      public Set<Integer> getPreselectedCategoryIds() {
        Integer defaultCategory = currentSeries.get(Series.DEFAULT_CATEGORY);
        if (budgetArea.isMultiCategories()) {
          Set<Integer> valueSet = localRepository.getAll(SeriesToCategory.TYPE,
                                                         GlobMatchers.fieldEquals(SeriesToCategory.SERIES, currentSeries.get(Series.ID)))
            .getValueSet(SeriesToCategory.CATEGORY);
          if (defaultCategory != null) {
            valueSet.add(defaultCategory);
          }
          return valueSet;
        }
        if (defaultCategory != null) {
          return Collections.singleton(currentSeries.get(Series.DEFAULT_CATEGORY));
        }
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
      return Month.getFullMonthLabel(seriesBudget.get(SeriesBudget.MONTH));
    }
  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.commitChanges(false);
      localRepository.rollback();
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.rollback();
      dialog.setVisible(false);
    }
  }

  private class CreateSeriesAction extends AbstractAction {
    public CreateSeriesAction() {
      super("create");
    }

    public void actionPerformed(ActionEvent e) {
      Glob newSeries = createSeries(Lang.get("seriesEdition.newSeries"), 0.0, 1);
      selectionService.select(newSeries);
    }
  }

  private class DeleteSeriesAction extends AbstractAction {
    private GlobList seriesToDelete = GlobList.EMPTY;
    private SeriesDeleteDialog seriesDeleteDialog;

    public DeleteSeriesAction() {
      super("delete");
      selectionService.addListener(new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          seriesToDelete = selection.getAll(Series.TYPE);
          setEnabled(!seriesToDelete.isEmpty());
        }
      }, Series.TYPE);
      seriesDeleteDialog = new SeriesDeleteDialog(localRepository, localDirectory, dialog);
    }

    public void actionPerformed(ActionEvent e) {
      if (seriesToDelete.isEmpty()) {
        return;
      }
      Set<Integer> series = seriesToDelete.getValueSet(Series.ID);
      GlobList transactionsForSeries = localRepository.getAll(Transaction.TYPE, fieldIn(Transaction.SERIES, series));
      if (transactionsForSeries.isEmpty()) {
        localRepository.delete(seriesToDelete);
      }
      else {
        if (seriesDeleteDialog.show()) {
          localRepository.delete(seriesToDelete);
          for (Glob transaction : transactionsForSeries) {
            localRepository.update(transaction.getKey(), Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);
          }
          GlobList seriesToCategory = localRepository.getAll(SeriesToCategory.TYPE, fieldIn(SeriesToCategory.SERIES, series));
          localRepository.delete(seriesToCategory);
        }
      }
    }
  }

  private class MonthCheckBoxUpdater implements GlobSelectionListener, ItemListener, ChangeSetListener {
    private JCheckBox checkBox;
    private Integer monthIndex;
    private boolean updateInProgress;
    private boolean forceDisable;
    private Glob currentSeries;

    private MonthCheckBoxUpdater(Integer monthIndex) {
      this.checkBox = new JCheckBox();
      this.monthIndex = monthIndex;
      selectionService.addListener(this, Series.TYPE);
      checkBox.addItemListener(this);
    }

    public JCheckBox getCheckBox() {
      return checkBox;
    }

    public void selectionUpdated(GlobSelection selection) {
      GlobList seriesList = selection.getAll(Series.TYPE);
      currentSeries = seriesList.size() == 1 ? seriesList.get(0) : null;
      try {
        updateInProgress = true;
        updateCheckBox();
        checkBox.setSelected((currentSeries != null) && currentSeries.get(Series.getField(monthIndex)));
      }
      finally {
        updateInProgress = false;
      }
    }

    public void itemStateChanged(ItemEvent e) {
      if (updateInProgress) {
        return;
      }
      if (currentSeries != null) {
        BooleanField field = Series.getField(monthIndex);
        boolean newState = e.getStateChange() == ItemEvent.SELECTED;
        localRepository.update(currentSeries.getKey(), field, newState);
      }
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      updateCheckBox();
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      updateCheckBox();
    }

    private void updateCheckBox() {
      if (currentSeries != null) {
        Integer firstMonth = currentSeries.get(Series.FIRST_MONTH);
        Integer lastMonth = currentSeries.get(Series.LAST_MONTH);
        if (firstMonth == null || lastMonth == null) {
          forceDisable = false;
        }
        else {
          forceDisable = true;
          for (int month = firstMonth; month <= lastMonth; month = Month.next(month)) {
            if (Month.toMonth(month) == monthIndex) {
              forceDisable = false;
              break;
            }
          }
        }
      }
      checkBox.setEnabled(!forceDisable && currentSeries != null);
    }
  }

  private abstract class CalendarAction extends AbstractAction {
    private IntegerField date;
    private int sens;

    private CalendarAction(IntegerField date, int sens) {
      this.date = date;
      this.sens = sens;
    }

    protected abstract Integer getMonthLimit();

    public void actionPerformed(ActionEvent e) {
      int sens = this.sens;
      MonthChooserDialog chooser = new MonthChooserDialog(dialog, localDirectory);
      Integer monthId = currentSeries.get(date);
      Integer limit = getMonthLimit();
      if (monthId == null) {
        monthId = limit == null ? localDirectory.get(TimeService.class).getCurrentMonthId() : limit;
      }
      if (limit == null) {
        limit = 0;
        sens = 0;
      }
      int result = chooser.show(monthId, sens, limit);
      if (result == -1) {
        return;
      }
      localRepository.update(currentSeries.getKey(), date, result);
    }
  }

  private static class MonthYearStringifier implements GlobListStringifier {
    private IntegerField monthField;

    private MonthYearStringifier(IntegerField month) {
      monthField = month;
    }

    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty()) {
        return null;
      }
      Integer monthId = list.get(0).get(monthField);
      if (monthId == null) {
        return null;
      }
      return Month.getShortMonthLabel(monthId) + " " + Month.toYearString(monthId);
    }
  }

  private class AmountStringifier extends AbstractGlobStringifier {
    public String toString(Glob glob, GlobRepository repository) {
      Double value = glob.get(SeriesBudget.AMOUNT);
      if ((value == null) || (value == 0.0)) {
        return "0";
      }
      return PicsouDescriptionService.DECIMAL_FORMAT.format((budgetArea.isIncome() ? 1 : -1) * value);
    }
  }

  private class OkButtonUpdater extends DefaultChangeSetListener {
    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(Series.TYPE)) {
        update(repository);
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      update(repository);
    }

    private void update(GlobRepository repository) {
      GlobList series = repository.getAll(Series.TYPE);
      for (Glob glob : series) {
        if (glob.get(Series.DEFAULT_CATEGORY) == null) {
          okAction.setEnabled(false);
          return;
        }
      }
      okAction.setEnabled(true);
    }
  }

  private class TableBackgroundPainter implements CellPainter {
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

  private class SeriesBudgetLabelCustomizer implements LabelCustomizer, ColorChangeListener {
    private Color pastTextColor;
    private Color normalTextColor;

    private SeriesBudgetLabelCustomizer() {
      localDirectory.get(ColorService.class).addListener(this);
    }

    protected void finalize() throws Throwable {
      super.finalize();
      localDirectory.get(ColorService.class).removeListener(this);
    }

    public void colorsChanged(ColorLocator colorLocator) {
      pastTextColor = colorLocator.get("seriesEdition.table.text.past");
      normalTextColor = colorLocator.get("seriesEdition.table.text.normal");
    }

    public void process(JLabel label, Glob seriesBudget, boolean isSelected, boolean hasFocus, int row, int column) {
      int budgetMonthId = seriesBudget.get(SeriesBudget.MONTH);
      int currentMonthId = timeService.getCurrentMonthId();
      if (budgetMonthId == currentMonthId) {
        label.setForeground(normalTextColor);
        LabelCustomizers.BOLD.process(label, seriesBudget, isSelected, hasFocus, row, column);
      }
      else if (budgetMonthId < currentMonthId) {
        label.setForeground(pastTextColor);
        LabelCustomizers.PLAIN.process(label, seriesBudget, isSelected, hasFocus, row, column);
      }
      else if (budgetMonthId > currentMonthId) {
        label.setForeground(normalTextColor);
        LabelCustomizers.PLAIN.process(label, seriesBudget, isSelected, hasFocus, row, column);
      }
      if (isSelected) {
        label.setForeground(Color.WHITE);
      }
    }
  }

  private static class SeriesToCategoryStringifier extends AbstractGlobStringifier {
    private final GlobStringifier categoryStringifier;

    public SeriesToCategoryStringifier(GlobStringifier categoryStringifier) {
      this.categoryStringifier = categoryStringifier;
    }

    public String toString(Glob glob, GlobRepository repository) {
      Glob category = repository.get(Key.create(Category.TYPE, glob.get(SeriesToCategory.CATEGORY)));
      return categoryStringifier.toString(category, repository);
    }
  }

  private class MissingCategoryStringifier implements GlobListStringifier {
    public String toString(GlobList series, GlobRepository repository) {
      if (budgetArea == null) {
        return "";
      }
      if (series.size() != 1) {
        return "";
      }
      Integer category = series.get(0).get(Series.DEFAULT_CATEGORY);
      if (category != null) {
        return "";
      }
      if (budgetArea.isMultiCategories()) {
        return Lang.get("seriesEdition.missing.category.label.multiple");
      }
      else {
        return Lang.get("seriesEdition.missing.category.label.single");
      }
    }
  }

  private class AmountLabelStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Set<Integer> monthIds = list.getValueSet(SeriesBudget.MONTH);
      String monthDescription = MonthListStringifier.toString(monthIds);
      if (Strings.isNullOrEmpty(monthDescription)) {
        return Lang.get("seriesEdition.amount.label.short");
      }
      else {
        return Lang.get("seriesEdition.amount.label.full", monthDescription.toLowerCase());
      }
    }
  }
}
