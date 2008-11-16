package org.designup.picsou.gui.series;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.categories.CategoryChooserCallback;
import org.designup.picsou.gui.categories.CategoryChooserDialog;
import org.designup.picsou.gui.components.MonthChooserDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.ReadOnlyGlobTextFieldView;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.AutomaticSeriesBudgetTrigger;
import org.designup.picsou.triggers.SavingSeriesMirrorTrigger;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.impl.StringListCellRenderer;
import org.globsframework.metamodel.Field;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

public class SeriesEditionDialog {
  private BudgetArea budgetArea;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private SelectionService selectionService;
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;

  private Glob currentSeries;

  private JLabel titleLabel;
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
  private JLabel categoryLabel;
  private JPanel monthSelectionPanel;
  private JPanel seriesPanel;
  private Key createdSeries;
  Integer currentlySelectedCategory;
  private JPanel buttonSeriePanel;
  private SeriesBudgetEditionPanel budgetEditionPanel;
  private GlobList selectedTransactions = new EmptyGlobList();
  private GlobComboView savingsAccount;
  private GlobLinkComboEditor toSeries;
  private Map<Key, Key> savingAccountCacheForSavingSeries = new HashMap<Key, Key>();


  public SeriesEditionDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Category.TYPE, BudgetArea.TYPE, Month.TYPE, SeriesToCategory.TYPE, CurrentMonth.TYPE,
            ProfileType.TYPE, Account.TYPE)
      .get();

    addSeriesCreationTriggers(localRepository, new ProfileTypeSeriesTrigger.UserMonth() {
      public Set<Integer> getMonthWithTransction() {
        return selectedTransactions.getSortedSet(Transaction.MONTH);
      }
    });
    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
        changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
          public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
            Glob series = repository.get(key);
            if ((values.contains(Series.PROFILE_TYPE)
                 && values.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId()))
                && (values.contains(Series.IS_AUTOMATIC)
                    || series.get(Series.IS_AUTOMATIC))) { // le trigger de passage en automatique peut ne pas etre encore appelle
              GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE,
                                                         fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)));
              Glob currentMonth = repository.get(CurrentMonth.KEY);
              for (Glob budget : seriesBudgets) {
                if (budget.get(SeriesBudget.MONTH) > currentMonth.get(CurrentMonth.MONTH_ID)) {
                  repository.update(budget.getKey(), SeriesBudget.AMOUNT, 0.0);
                }
              }
            }
          }
        });
      }
    });
    selectionService = new SelectionService();
    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);

    dialog = PicsouDialog.create(parent, directory);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/seriesEditionDialog.splits",
                                                      localRepository, localDirectory);

    titleLabel = builder.add("title", new JLabel());

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
    seriesPanel = new JPanel();
    builder.add("seriesPanel", seriesPanel);

    builder.add("seriesList", seriesList.getComponent());

    buttonSeriePanel = new JPanel();
    builder.add("buttonSeriesPanel", buttonSeriePanel);
    builder.add("create", new CreateSeriesAction());
    builder.add("delete", new DeleteSeriesAction());

    nameEditor = builder.addEditor("nameField", Series.LABEL).setNotifyOnKeyPressed(true);

    registerCategoryComponents(descriptionService, builder);

    // Choix du comptes destination
    // si vide pas de comptes ==> pas de series
    savingsAccount = GlobComboView.init(Account.TYPE, localRepository, localDirectory)
      .setShowEmptyOption(true);
    builder.add("savingsAccount", savingsAccount);

    toSeries = new GlobLinkComboEditor(Series.SAVINGS_SERIES, localRepository, localDirectory)
      .setEmptyOptionLabel(Lang.get("seriesEdition.createPendingSeries"));
    builder.add("savingsToSeries", toSeries);

    GlobLinkComboEditor periodCombo =
      new GlobLinkComboEditor(Series.PROFILE_TYPE, localRepository, localDirectory);
    periodCombo.setShowEmptyOption(false);
    periodCombo.setComparator(new Comparator<Glob>() {

      public int compare(Glob o1, Glob o2) {
        return ProfileType.get(o1.get(ProfileType.ID)).getOrder()
          .compareTo(ProfileType.get(o2.get(ProfileType.ID)).getOrder());
      }
    });
    builder.add("periodCombo", periodCombo);

    monthSelectionPanel = new JPanel();
    builder.add("monthSelectionPanel", monthSelectionPanel);

    registerDateComponents(builder);

    budgetEditionPanel = new SeriesBudgetEditionPanel(dialog, repository, localRepository, localDirectory);
    JPanel seriesBudgetPanel = budgetEditionPanel.getPanel();
    builder.add("seriesBudgetEditionPanel", seriesBudgetPanel);

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentSeries = selection.getAll(Series.TYPE).getFirst();
        if (currentSeries != null) {
          assignCategoryAction.setEnabled(true);
          multiCategoryList.setFilter(fieldEquals(SeriesToCategory.SERIES, currentSeries.get(Series.ID)));
          boolean isSavingsSeries = currentSeries.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId());
          if (isSavingsSeries) {
            Key savingAccount = savingAccountCacheForSavingSeries.get(currentSeries.getKey());
            if (savingAccount == null) {
              selectionService.clear(Account.TYPE);
            }
            else {
              selectionService.select(GlobSelectionBuilder.create(localRepository.get(savingAccount)));
            }
          }
          savingsAccount.setEnable(isSavingsSeries);
          toSeries.setEnable(isSavingsSeries);
        }
        else {
          assignCategoryAction.setEnabled(false);
          multiCategoryList.setFilter(GlobMatchers.NONE);
          savingsAccount.setEnable(false);
          toSeries.setEnable(false);
        }
        updateDateState();
        updateMonthChooser();
      }
    }, Series.TYPE);

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        if (currentSeries != null) {
          Glob targetSavingAccount = selection.getAll(Account.TYPE).getFirst();
          if (targetSavingAccount != null) {
            savingAccountCacheForSavingSeries.put(currentSeries.getKey(), targetSavingAccount.getKey());
            toSeries.setFilter(savingsSeriesFilter(targetSavingAccount));
          }
          else {
            savingAccountCacheForSavingSeries.put(currentSeries.getKey(), null);
            toSeries.setFilter(NONE);
          }
        }
      }
    }, Account.TYPE);

    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        updateDateState();
        updateMonthChooser();
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

    localRepository.addChangeListener(new OkButtonUpdater());

    JPanel panel = builder.load();
    okAction = new ValidateAction();
    dialog.addPanelWithButtons(panel, okAction, new CancelAction());
  }

  private GlobMatcher savingsSeriesFilter(Glob account) {
    if (currentSeries != null) {
      return GlobMatchers.fieldEquals(Series.SAVINGS_ACCOUNT, account.get(Account.ID));
    }
    return NONE;
  }

  public static void addSeriesCreationTriggers(GlobRepository repository,
                                               final ProfileTypeSeriesTrigger.UserMonth userMonth) {
    repository.addTrigger(new ProfileTypeSeriesTrigger(userMonth));
    repository.addTrigger(new AutomaticSeriesBudgetTrigger());
    repository.addTrigger(new SeriesBudgetTrigger());
    repository.addTrigger(new SavingSeriesMirrorTrigger());
  }

  private void updateMonthChooser() {
    if (currentSeries != null) {
      monthSelectionPanel.setVisible(
        ProfileType.get(currentSeries.get(Series.PROFILE_TYPE)).getMonthStep() != -1);
    }
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
      .setSingleSelectionMode()
      .setRenderer(new SeriesToCategoryStringifier(categoryStringifier));
    builder.add("multipleCategoryList", multiCategoryList);

    singleCategoryField =
      ReadOnlyGlobTextFieldView.init(Series.DEFAULT_CATEGORY, localRepository, localDirectory)
        .getComponent();
    builder.add("singleCategoryField", singleCategoryField);

    assignCategoryAction = new AssignCategoryAction();
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
        GlobList transactions =
          localRepository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, currentSeries.get(Series.ID))
            .getGlobs().filterSelf(fieldEquals(Transaction.PLANNED, false), localRepository)
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
        GlobList transactions =
          localRepository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, currentSeries.get(Series.ID)).getGlobs()
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

  public void show(BudgetArea budgetArea, Set<Integer> monthIds, Integer seriesId) {
    try {
      localRepository.startChangeSet();
      localRepository.rollback();
      initBudgetAreaSeries(budgetArea);
    }
    finally {
      localRepository.completeChangeSet();
    }
    seriesPanel.setVisible(true);
    buttonSeriePanel.setVisible(true);
    Glob series = null;
    if (seriesId != null) {
      series = localRepository.get(Key.create(Series.TYPE, seriesId));
    }
    doShow(monthIds, series, null);
  }

  public void show(Glob series, Set<Integer> monthIds) {
    try {
      localRepository.startChangeSet();
      localRepository.rollback();
      initBudgetAreaSeries(BudgetArea.get(series.get(Series.BUDGET_AREA)));
    }
    finally {
      localRepository.completeChangeSet();
    }
    seriesPanel.setVisible(false);
    buttonSeriePanel.setVisible(false);
    doShow(monthIds, localRepository.get(series.getKey()), false);
  }

  public Key showNewSeries(GlobList transactions, BudgetArea budgetArea) {
    selectedTransactions = transactions;
    this.budgetArea = BudgetArea.get(budgetArea.getId());
    Glob createdSeries;
    try {
      localRepository.startChangeSet();
      localRepository.rollback();
      initBudgetAreaSeries(budgetArea);

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
      createdSeries = createSeries(label, day);
    }
    finally {
      localRepository.completeChangeSet();
    }
    seriesPanel.setVisible(false);
    buttonSeriePanel.setVisible(false);
    this.createdSeries = null;
    this.currentlySelectedCategory = null;
    doShow(getCurrentMonthId(), createdSeries, true);
    if (this.createdSeries != null) {
      return this.createdSeries;
    }
    return null;
  }

  private Glob createSeries(String label, Integer day) {
    java.util.List<FieldValue> values =
      new ArrayList<FieldValue>(Arrays.asList(value(Series.BUDGET_AREA, budgetArea.getId()),
                                              value(Series.INITIAL_AMOUNT, 0.),
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
                                              value(Series.DECEMBER, true)));
    if (budgetArea == BudgetArea.SPECIAL) {
      SelectionService selectionService = directory.get(SelectionService.class);
      if (!selectedTransactions.isEmpty()) {
        Integer first = Integer.MAX_VALUE;
        Integer last = Integer.MIN_VALUE;
        for (Glob transaction : selectedTransactions) {
          first = Math.min(first, transaction.get(Transaction.MONTH));
          last = Math.max(last, transaction.get(Transaction.MONTH));
        }
        values.add(value(Series.FIRST_MONTH, first));
        values.add(value(Series.LAST_MONTH, last));
      }
      else {
        GlobList list = selectionService.getSelection(Month.TYPE).sort(Month.ID);
        values.add(value(Series.IS_AUTOMATIC, false));
        if (!list.isEmpty()) {
          values.add(value(Series.FIRST_MONTH, list.getFirst().get(Month.ID)));
          values.add(value(Series.LAST_MONTH, list.getLast().get(Month.ID)));
        }
        else {
          int monthId = localDirectory.get(TimeService.class).getCurrentMonthId();
          values.add(value(Series.FIRST_MONTH, monthId));
          values.add(value(Series.LAST_MONTH, monthId));
        }
      }
    }
    if (budgetArea == BudgetArea.INCOME) {
      values.add(value(Series.DEFAULT_CATEGORY, MasterCategory.INCOME.getId()));
    }
    return localRepository.create(Series.TYPE, values.toArray(new FieldValue[values.size()]));
  }

  private void initBudgetAreaSeries(BudgetArea budgetArea) {
    this.budgetArea = budgetArea;

    this.titleLabel.setText(Lang.get("seriesEdition.title." + budgetArea.getName()));

    initCategorizeVisibility();
    GlobList seriesList =
      repository.getAll(Series.TYPE, not(fieldEquals(Series.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId())));

    GlobList globsToLoad = new GlobList();
    for (Glob series : seriesList) {
      globsToLoad.add(series);
      globsToLoad.addAll(repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                                series.get(Series.ID)).getGlobs());
      ReadOnlyGlobRepository.MultiFieldIndexed index =
        repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID));
      globsToLoad.addAll(index.getGlobs().filterSelf(fieldEquals(Transaction.PLANNED, false), repository));
    }
    localRepository.reset(globsToLoad, SeriesBudget.TYPE, Series.TYPE, Transaction.TYPE);

    this.seriesList.setFilter(fieldEquals(Series.BUDGET_AREA, budgetArea.getId()));
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
    if (currentSeries != null) {
      budgetEditionPanel.selectBudgets(monthIds);
    }
    dialog.pack();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (selectName != null) {
          if (selectName) {
            nameEditor.getComponent().requestFocusInWindow();
            nameEditor.getComponent().selectAll();
          }
          else {
            budgetEditionPanel.selectAmountEditor();
          }
        }
      }
    });
    GuiUtils.showCentered(dialog);
  }

  public Window getDialog() {
    return dialog;
  }

  public Integer getCurrentCategory() {
    return currentlySelectedCategory;
  }

  private class AssignCategoryAction extends AbstractAction {
    private AssignCategoryAction() {
      super(Lang.get("seriesEdition.categorize"));
    }

    public void show() {
      CategoryChooserDialog chooser =
        new CategoryChooserDialog(new SeriesCategoryChooserCallback(), dialog,
                                  !budgetArea.isMultiCategories(),
                                  null, localRepository, localDirectory);

      chooser.show();
    }

    public void actionPerformed(ActionEvent e) {
      show();
    }

    private class SeriesCategoryChooserCallback implements CategoryChooserCallback {
      public void processSelection(GlobList categories) {
        localRepository.startChangeSet();
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
          localRepository.completeChangeSet();
        }

        SeriesEditionDialog.this.multiCategoryList.selectFirst();
      }

      public Set<Integer> getPreselectedCategoryIds() {
        Integer defaultCategory = currentSeries.get(Series.DEFAULT_CATEGORY);
        if (budgetArea.isMultiCategories()) {
          Set<Integer> valueSet = localRepository.getAll(SeriesToCategory.TYPE,
                                                         fieldEquals(SeriesToCategory.SERIES, currentSeries.get(Series.ID)))
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

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      trimNames();
      if (currentSeries != null) {
        SeriesEditionDialog.this.createdSeries = currentSeries.getKey();
        if (budgetArea.isMultiCategories()) {
          GlobList globList = selectionService.getSelection(SeriesToCategory.TYPE);
          if (globList.size() == 1) {
            Glob series = globList.get(0);
            currentlySelectedCategory = series.get(SeriesToCategory.CATEGORY);
          }
        }
        localRepository.startChangeSet();
        try {
          Set<Key> newSeries = localRepository.getCurrentChanges().getCreated(Series.TYPE);
          for (Key seriesKey : newSeries) {
            if (isSavingSeries(seriesKey)) {
              Key pendingSeriesKey = savingAccountCacheForSavingSeries.get(seriesKey);
              Glob currentSeries = localRepository.get(seriesKey);
              if (pendingSeriesKey != null) {
                if (currentSeries.get(Series.SAVINGS_SERIES) == null) {
                  FieldValuesBuilder valuesBuilder = FieldValuesBuilder.init();
                  valuesBuilder.set(Series.SAVINGS_ACCOUNT, pendingSeriesKey.get(Account.ID));
                  valuesBuilder.set(Series.SAVINGS_SERIES, currentSeries.get(Series.ID));
                  valuesBuilder.set(Series.NAME, Lang.get("budgetArea.savings") + ":" + currentSeries.get(Series.NAME));
                  valuesBuilder.set(Series.LABEL, Lang.get("budgetArea.savings") + ":" + currentSeries.get(Series.LABEL));
                  for (Field field : Series.TYPE.getFields()) {
                    if (field != Series.ID && field != Series.SAVINGS_ACCOUNT && field != Series.SAVINGS_SERIES
                        && field != Series.NAME && field != Series.LABEL) {
                      valuesBuilder.setValue(field, currentSeries.getValue(field));
                    }
                  }
                  Glob createdSeries = localRepository.create(Series.TYPE, valuesBuilder.toArray());
                  localRepository.update(seriesKey, Series.SAVINGS_SERIES, createdSeries.get(Series.ID));
                }
              }
            }
          }
        }
        finally {
          localRepository.completeChangeSet();
        }
      }
      localRepository.commitChanges(false);
      localRepository.rollback();
      dialog.setVisible(false);
    }

    private boolean isSavingSeries(Key key) {
      return localRepository.get(key).get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId());
    }

    private void trimNames() {
      for (Glob series : localRepository.getAll(Series.TYPE)) {
        String name = series.get(Series.NAME);
        if (name == null) {
          name = "";
        }
        localRepository.update(series.getKey(), Series.NAME, name.trim());
      }
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
      Glob newSeries = createSeries(Lang.get("seriesEdition.newSeries"), 1);
      savingAccountCacheForSavingSeries.put(newSeries.getKey(), null);
      selectionService.select(newSeries);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          nameEditor.getComponent().requestFocusInWindow();
          nameEditor.getComponent().selectAll();
        }
      });
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
      }
      finally {
        updateInProgress = false;
      }
    }

    public void itemStateChanged(ItemEvent e) {
      if (updateInProgress) {
        return;
      }
      localRepository.startChangeSet();
      try {
        if (currentSeries != null) {
          BooleanField field = Series.getField(monthIndex);
          boolean newState = e.getStateChange() == ItemEvent.SELECTED;
          localRepository.update(currentSeries.getKey(), field, newState);
          ProfileType profileType = ProfileType.get(currentSeries.get(Series.PROFILE_TYPE));
          if (newState &&
              profileType.getMonthStep() != -1 && profileType != ProfileType.CUSTOM) {
            BooleanField[] months = Series.getMonths();
            for (BooleanField month : months) {
              if (month != field) {
                localRepository.update(currentSeries.getKey(), month, false);
              }
            }
          }
        }
      }
      finally {
        localRepository.completeChangeSet();
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
      checkBox.setSelected((currentSeries != null) && currentSeries.get(Series.getField(monthIndex)));
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
        if (!BudgetArea.UNCATEGORIZED.getId().equals(glob.get(Series.BUDGET_AREA)) &&
            glob.get(Series.DEFAULT_CATEGORY) == null) {
          okAction.setEnabled(false);
          return;
        }
      }
      okAction.setEnabled(true);
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
}
