package org.designup.picsou.gui.series;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.categories.CategoryChooserCallback;
import org.designup.picsou.gui.categories.CategoryChooserDialog;
import org.designup.picsou.gui.components.MonthChooserDialog;
import org.designup.picsou.gui.components.MonthRangeBound;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.ReadOnlyGlobTextFieldView;
import org.designup.picsou.gui.description.MonthYearStringifier;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.AutomaticSeriesBudgetTrigger;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
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
import java.awt.event.ActionListener;
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
  private Set<Integer> currentMonthIds = Collections.emptySet();

  private JLabel titleLabel;
  private GlobListView seriesList;
  private JTextField singleCategoryField;
  private GlobListView multiCategoryList;
  private SeriesEditionDialog.AssignCategoryAction assignCategoryAction;
  private SeriesEditionDialog.ValidateAction okAction;
  private AbstractAction deleteStartDateAction;
  private OpenMonthChooserAction startDateChooserAction;
  private AbstractAction deleteEndDateAction;
  private OpenMonthChooserAction endDateChooserAction;
  private OpenMonthChooserAction singleMonthChooserAction;
  private GlobTextEditor nameEditor;
  private JLabel categoryLabel;
  private JPanel monthSelectionPanel;
  private JPanel seriesPanel;
  private Key createdSeries;
  Integer currentlySelectedCategory;
  private JPanel seriesListButtonPanel;
  private SeriesBudgetEditionPanel budgetEditionPanel;
  private GlobList selectedTransactions = new EmptyGlobList();
  private GlobLinkComboEditor fromAccountsCombo;
  private GlobLinkComboEditor toAccountsCombo;
  private JComboBox dayChooser;
  private CardHandler monthSelectionCards;
  private JButton singleSeriesDeleteButton;
  private JLabel savingsMessage;

  public SeriesEditionDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Category.TYPE, BudgetArea.TYPE, Month.TYPE, SeriesToCategory.TYPE, CurrentMonth.TYPE,
            ProfileType.TYPE, Account.TYPE)
      .get();

    localRepository.addTrigger(new UpdateDeleteMirror());
    localRepository.addTrigger(new SingleMonthProfileTypeUpdater());
    localRepository.addTrigger(new ResetAllBudgetIfInAutomaticAndNoneAccountAreImported());
    addSeriesCreationTriggers(localRepository, new ProfileTypeSeriesTrigger.UserMonth() {
      public Set<Integer> getMonthWithTransction() {
        return selectedTransactions.getSortedSet(Transaction.MONTH);
      }
    });
    localRepository.addTrigger(new UpdateUpdateMirror());
    localRepository.addTrigger(new UpdateBudgetOnSeriesAccountsChange());
    localRepository.addChangeListener(new ProfileTypeChangeListener());
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
      }, seriesStringifier.getComparator(localRepository));
    seriesPanel = new JPanel();
    builder.add("seriesPanel", seriesPanel);

    builder.add("seriesList", seriesList.getComponent());

    seriesListButtonPanel = new JPanel();
    builder.add("seriesListButtonPanel", seriesListButtonPanel);
    builder.add("create", new CreateSeriesAction());
    builder.add("delete", new DeleteSeriesAction(false));

    nameEditor = builder.addEditor("nameField", Series.NAME).setNotifyOnKeyPressed(true);

    registerCategoryComponents(descriptionService, builder);

    GlobMatcher accountFilter = GlobMatchers.and(
      GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID, Account.ALL_SUMMARY_ACCOUNT_ID)),
      GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID, Account.MAIN_SUMMARY_ACCOUNT_ID)),
      GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID, Account.SAVINGS_SUMMARY_ACCOUNT_ID)));

    fromAccountsCombo = new GlobLinkComboEditor(Series.FROM_ACCOUNT, localRepository, localDirectory)
      .setFilter(accountFilter)
      .setShowEmptyOption(true)
      .setEmptyOptionLabel(Lang.get("seriesEdition.account.external"));
    builder.add("fromAccount", fromAccountsCombo);

    toAccountsCombo = new GlobLinkComboEditor(Series.TO_ACCOUNT, localRepository, localDirectory)
      .setFilter(accountFilter)
      .setShowEmptyOption(true)
      .setEmptyOptionLabel(Lang.get("seriesEdition.account.external"));
    builder.add("toAccount", toAccountsCombo);

    savingsMessage = new JLabel(Lang.get("seriesEdition.savingsMessagesError"));
    builder.add("savingsMessage", savingsMessage);

    Integer[] days = new Integer[31];
    for (int i = 0; i < days.length; i++) {
      days[i] = i + 1;
    }
    dayChooser = new JComboBox(days);
    dayChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (currentSeries != null) {
          localRepository.update(currentSeries.getKey(), Series.DAY, dayChooser.getSelectedItem());
        }
      }
    });
    builder.add("dayChooser", dayChooser);

    GlobLinkComboEditor profileTypeCombo =
      new GlobLinkComboEditor(Series.PROFILE_TYPE, localRepository, localDirectory);
    profileTypeCombo
      .setShowEmptyOption(false)
      .setComparator(new Comparator<Glob>() {
        public int compare(Glob o1, Glob o2) {
          return ProfileType.get(o1.get(ProfileType.ID)).getOrder()
            .compareTo(ProfileType.get(o2.get(ProfileType.ID)).getOrder());
        }
      });
    builder.add("profileCombo", profileTypeCombo);

    monthSelectionPanel = new JPanel();
    builder.add("monthSelectionPanel", monthSelectionPanel);

    monthSelectionCards = builder.addCardHandler("monthSelection");
    registerDateRangeComponents(builder);
    registerSingleMonthComponents(builder);

    budgetEditionPanel = new SeriesBudgetEditionPanel(dialog, repository, localRepository, localDirectory);
    JPanel seriesBudgetPanel = budgetEditionPanel.getPanel();
    builder.add("seriesBudgetEditionPanel", seriesBudgetPanel);

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentSeries = selection.getAll(Series.TYPE).getFirst();
        assignCategoryAction.setEnabled(currentSeries != null);
        if (currentSeries != null) {
          multiCategoryList.setFilter(fieldEquals(SeriesToCategory.SERIES, currentSeries.get(Series.ID)));
          boolean isSavingsSeries = currentSeries.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId());
          fromAccountsCombo.setVisible(isSavingsSeries);
          toAccountsCombo.setVisible(isSavingsSeries);
          dayChooser.setSelectedItem(currentSeries.get(Series.DAY));
          Glob fromAccount = repository.findLinkTarget(currentSeries, Series.FROM_ACCOUNT);
          Glob toAccount = repository.findLinkTarget(currentSeries, Series.TO_ACCOUNT);
          boolean noneImported = Account.areNoneImported(fromAccount, toAccount);
          dayChooser.setVisible(noneImported);
          boolean savingsOnError = fromAccount == null && toAccount == null;
          savingsMessage.setVisible(isSavingsSeries && savingsOnError);
          if (isSavingsSeries && savingsOnError) {
            okAction.setEnabled(false);
          }
        }
        else {
          multiCategoryList.setFilter(GlobMatchers.NONE);
        }
        updateDateSelectors();
        updateMonthChooser();
        updateMonthSelectionCard();
      }
    }, Series.TYPE);

    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        updateDateSelectors();
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
    singleSeriesDeleteButton = new JButton(new DeleteSeriesAction(true));
    singleSeriesDeleteButton.setOpaque(false);
    singleSeriesDeleteButton.setName("deleteSingleSeries");
    dialog.addPanelWithButtons(panel, okAction, new CancelAction(), singleSeriesDeleteButton);
  }

  public static void addSeriesCreationTriggers(GlobRepository repository,
                                               final ProfileTypeSeriesTrigger.UserMonth userMonth) {
    repository.addTrigger(new ProfileTypeSeriesTrigger(userMonth));
    repository.addTrigger(new AutomaticSeriesBudgetTrigger());
    repository.addTrigger(new SeriesBudgetTrigger());
  }

  private void updateMonthChooser() {
    if (currentSeries != null) {
      ProfileType profileType = ProfileType.get(currentSeries.get(Series.PROFILE_TYPE));
      monthSelectionPanel.setVisible(profileType.getMonthStep() != -1);
    }
    else {
      monthSelectionPanel.setVisible(false);
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

  private void registerDateRangeComponents(GlobsPanelBuilder builder) {

    builder.add("seriesStartDate",
                ReadOnlyGlobTextFieldView.init(Series.TYPE, localRepository, localDirectory,
                                               new MonthYearStringifier(Series.FIRST_MONTH)));

    deleteStartDateAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        localRepository.update(currentSeries.getKey(), Series.FIRST_MONTH, null);
      }
    };
    builder.add("deleteSeriesStartDate", deleteStartDateAction);

    startDateChooserAction = new OpenMonthChooserAction(Series.FIRST_MONTH, MonthRangeBound.LOWER) {
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
    builder.add("seriesStartDateChooser", startDateChooserAction);

    builder.add("seriesEndDate",
                ReadOnlyGlobTextFieldView.init(Series.TYPE, localRepository, localDirectory,
                                               new MonthYearStringifier(Series.LAST_MONTH)));

    deleteEndDateAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        localRepository.update(currentSeries.getKey(), Series.LAST_MONTH, null);
      }
    };
    builder.add("deleteSeriesEndDate", deleteEndDateAction);

    endDateChooserAction = new OpenMonthChooserAction(Series.LAST_MONTH, MonthRangeBound.UPPER) {
      protected Integer getMonthLimit() {
        GlobList transactions =
          localRepository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, currentSeries.get(Series.ID))
            .getGlobs().sort(Transaction.MONTH);
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
    builder.add("seriesEndDateChooser", endDateChooserAction);
  }

  private void registerSingleMonthComponents(GlobsPanelBuilder builder) {
    builder.add("singleMonthDate",
                ReadOnlyGlobTextFieldView.init(Series.TYPE, localRepository, localDirectory,
                                               new MonthYearStringifier(Series.FIRST_MONTH)));

    singleMonthChooserAction = new OpenMonthChooserAction(Series.FIRST_MONTH, MonthRangeBound.NONE) {
      protected Integer getMonthLimit() {
        return null;
      }
    };
    builder.add("singleMonthChooser", singleMonthChooserAction);
  }

  private void updateDateSelectors() {
    startDateChooserAction.setEnabled(currentSeries != null);
    deleteStartDateAction.setEnabled(currentSeries != null && currentSeries.get(Series.FIRST_MONTH) != null);
    endDateChooserAction.setEnabled(currentSeries != null);
    deleteEndDateAction.setEnabled(currentSeries != null && currentSeries.get(Series.LAST_MONTH) != null);
    singleMonthChooserAction.setEnabled(currentSeries != null);
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
    setSeriesListVisible(true);
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
    setSeriesListVisible(false);
    seriesPanel.setVisible(false);
    seriesListButtonPanel.setVisible(false);
    doShow(monthIds, localRepository.get(series.getKey()), false);
  }

  public Key showNewSeries(GlobList transactions, GlobList selectedMonths, BudgetArea budgetArea) {
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
    setSeriesListVisible(false);

    this.createdSeries = null;
    this.currentlySelectedCategory = null;
    doShow(selectedMonths.getValueSet(Month.ID), createdSeries, true);
    return this.createdSeries;
  }

  private void setSeriesListVisible(boolean visible) {
    seriesPanel.setVisible(visible);
    seriesListButtonPanel.setVisible(visible);
    singleSeriesDeleteButton.setVisible(!visible);
  }

  private Glob createSeries(String label, Integer day) {
    java.util.List<FieldValue> values =
      new ArrayList<FieldValue>(Arrays.asList(value(Series.BUDGET_AREA, budgetArea.getId()),
                                              value(Series.INITIAL_AMOUNT, 0.),
                                              value(Series.NAME, label),
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
      values.add(value(Series.IS_AUTOMATIC, false));
      SelectionService selectionService = directory.get(SelectionService.class);
      if (!selectedTransactions.isEmpty()) {
        SortedSet<Integer> months = selectedTransactions.getSortedSet(Transaction.MONTH);
        values.add(value(Series.FIRST_MONTH, months.first()));
        values.add(value(Series.LAST_MONTH, months.last()));
        if (selectedTransactions.size() == 1) {
          values.add(value(Series.PROFILE_TYPE, ProfileType.SINGLE_MONTH.getId()));
          values.add(value(Series.INITIAL_AMOUNT, selectedTransactions.getFirst().get(Transaction.AMOUNT)));
        }
      }
      else {
        GlobList monthIds = selectionService.getSelection(Month.TYPE).sort(Month.ID);
        if (!monthIds.isEmpty()) {
          values.add(value(Series.FIRST_MONTH, monthIds.getFirst().get(Month.ID)));
          values.add(value(Series.LAST_MONTH, monthIds.getLast().get(Month.ID)));
        }
        else {
          int monthId = localDirectory.get(TimeService.class).getCurrentMonthId();
          values.add(value(Series.FIRST_MONTH, monthId));
          values.add(value(Series.LAST_MONTH, monthId));
        }
        if (monthIds.size() == 1) {
          values.add(value(Series.PROFILE_TYPE, ProfileType.SINGLE_MONTH.getId()));
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

    this.seriesList.setFilter(GlobMatchers.and(fieldEquals(Series.BUDGET_AREA, budgetArea.getId()),
                                               fieldEquals(Series.IS_MIRROR, false)));
  }

  private void initCategorizeVisibility() {
    this.categoryLabel.setText(Lang.get("seriesEdition.category.label." +
                                        (budgetArea.isMultiCategories() ? "multiple" : "single")));
    this.singleCategoryField.setVisible(!budgetArea.isMultiCategories());
    this.multiCategoryList.setVisible(budgetArea.isMultiCategories());
  }

  private void doShow(Set<Integer> monthIds, Glob series, final Boolean selectName) {
    if (series != null && series.get(Series.IS_MIRROR)) {
      series = localRepository.findLinkTarget(series, Series.MIRROR_SERIES);
    }
    this.currentSeries = series;
    this.currentMonthIds = new TreeSet<Integer>(monthIds);
    if (currentSeries != null) {
      selectionService.select(currentSeries);
    }
    else {
      seriesList.selectFirst();
    }
    if (currentSeries != null) {
      budgetEditionPanel.selectMonths(monthIds);
      updateMonthSelectionCard();
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

  private void updateMonthSelectionCard() {
    if (currentSeries != null) {
      if (ProfileType.SINGLE_MONTH.getId().equals(currentSeries.get(Series.PROFILE_TYPE))) {
        monthSelectionCards.show("singleMonthSelection");
      }
      else {
        monthSelectionCards.show("monthRangeSelection");
      }
    }
  }

  public Window getDialog() {
    return dialog;
  }

  public Integer getCurrentCategory() {
    return currentlySelectedCategory;
  }

  public static double computeMultiplier(Glob fromAccount, Glob toAccount, GlobRepository repository) {
    if (Account.areBothImported(fromAccount, toAccount)) {
      return 1;
    }
    double multiplier = Account.getMultiplierWithMainAsPointOfView(fromAccount, toAccount, repository);
    if (multiplier == 0) {
      if (fromAccount == null && toAccount == null) {
        multiplier = 1;
      }
      else {
        if (Account.onlyOneIsImported(fromAccount, toAccount)) {
          if (fromAccount.get(Account.IS_IMPORTED_ACCOUNT)) {
            multiplier = -1;
          }
          else {
            multiplier = 1;
          }
        }
        else if (fromAccount == null) {
          multiplier = 1;
        }
        else {
          multiplier = -1;
        }
      }
    }
    return multiplier;
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

      public Set<Integer> getUnUncheckable() {
        GlobList transactions =
          localRepository.getAll(Transaction.TYPE,
                                 fieldEquals(Transaction.SERIES, currentSeries.get(Series.ID)));
        Set<Integer> categoryIds = new HashSet<Integer>();
        for (Glob transaction : transactions) {
          categoryIds.add(transaction.get(Transaction.CATEGORY));
        }
        return categoryIds;
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
      }
      checkSavingsAccountChange();
      localRepository.commitChanges(false);
      localRepository.rollback();
      dialog.setVisible(false);
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

  private void checkSavingsAccountChange() {
    ChangeSet changeSet = localRepository.getCurrentChanges();

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob budget = localRepository.get(key);
        Glob series = localRepository.find(Key.create(Series.TYPE, budget.get(SeriesBudget.SERIES)));
        Glob fromAccount = localRepository.findLinkTarget(series, Series.FROM_ACCOUNT);
        Glob toAccount = localRepository.findLinkTarget(series, Series.TO_ACCOUNT);
        if (Account.areBothImported(fromAccount, toAccount)) {
          if (series.get(Series.IS_MIRROR)) {
            return;
          }
          Integer mirrorSeries = series.get(Series.MIRROR_SERIES);
          final Glob mirrorBudget =
            localRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeries)
              .findByIndex(SeriesBudget.MONTH, budget.get(SeriesBudget.MONTH)).getGlobs().getFirst();
          values.safeApply(new FieldValues.Functor() {
            public void process(Field field, Object value) throws Exception {
              if (field.equals(SeriesBudget.AMOUNT)) {
                localRepository.update(mirrorBudget.getKey(), SeriesBudget.AMOUNT, -((Double)value));
              }
              else {
                localRepository.update(mirrorBudget.getKey(), field, value);
              }
            }
          });
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        createMirrorSeries(key, values, localRepository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.TO_ACCOUNT) || values.contains(Series.FROM_ACCOUNT)) {
          Glob series = localRepository.get(key);

//          final LocalGlobRepository tmpRepo =
//            LocalGlobRepositoryBuilder.init(localRepository).copy(Month.TYPE, CurrentMonth.TYPE).get();
//          tmpRepo.addTrigger(new SeriesBudgetTrigger());
//          int seriesId = tmpRepo.getIdGenerator().getNextId(Series.ID, 1);
//          Glob newSeries = tmpRepo.create(Key.create(Series.TYPE, seriesId), series.toArray());
//          GlobList targetBudget =
//            tmpRepo.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId).getGlobs();
//
//          ReadOnlyGlobRepository.MultiFieldIndexed sourceBudget =
//            localRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(Series.ID));
//          for (Glob budget : targetBudget) {
//            tmpRepo.update(budget.getKey(), SeriesBudget.AMOUNT,
//                           sourceBudget.findByIndex(SeriesBudget.MONTH, budget.get(SeriesBudget.MONTH))
//                             .getGlobs().getFirst().get(SeriesBudget.AMOUNT));
//          }

          uncategorize(series.get(Series.ID));
//          createMirrorSeries(newSeries.getKey(), newSeries, tmpRepo);
          if (series.get(Series.MIRROR_SERIES) != null && !series.get(Series.IS_MIRROR)) {
            Integer seriesToDelete = series.get(Series.MIRROR_SERIES);
//            localRepository.delete(Key.create(Series.TYPE, seriesToDelete));
            uncategorize(seriesToDelete);
          }
//          localRepository.delete(key);
//          localRepository.commitChanges(false);
//          tmpRepo.commitChanges(true);
//          localRepository.commitChanges(false);
//          for (Glob transaction : transactions) {
//            localRepository.update(transaction.getKey(), FieldValue.value(Transaction.SERIES, seriesId));
//          }
//          for (Glob transaction : mirrorTransactions) {
//            localRepository.update(transaction.getKey(), FieldValue.value(Transaction.SERIES, mirrorId));
//          }
        }
        else {
          Glob series = localRepository.get(key);
          final Glob mirror = localRepository.findLinkTarget(series, Series.MIRROR_SERIES);
          if (mirror != null && !series.get((Series.IS_MIRROR))) {
            values.safeApply(new FieldValues.Functor() {
              public void process(Field field, Object value) throws Exception {
                localRepository.update(mirror.getKey(), field, value);
              }
            });
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private GlobList uncategorize(final Integer seriesId) {
    GlobList transactions = localRepository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES,
                                                        seriesId)
      .getGlobs().filterSelf(GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.PLANNED, false),
                                              GlobMatchers.fieldEquals(Transaction.CREATED_BY_SERIES, false)),
                             localRepository);
    for (Glob transaction : transactions) {
      localRepository.update(transaction.getKey(),
                             FieldValue.value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                             FieldValue.value(Transaction.CATEGORY, Category.NONE));
    }
    return transactions;
  }

  private Integer createMirrorSeries(Key key, FieldValues values, LocalGlobRepository repository) {
    Glob series = localRepository.find(key);
    if (series == null || series.get(Series.MIRROR_SERIES) != null) {
      return null;
    }
    Glob fromAccount = localRepository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = localRepository.findLinkTarget(series, Series.TO_ACCOUNT);
    if (Account.areBothImported(fromAccount, toAccount)) {
      FieldValue seriesFieldValues[] = values.toArray();
      GlobIdGenerator generator = repository.getIdGenerator();
      int mirrorId = generator.getNextId(Series.ID, 1);
      for (int i = 0; i < seriesFieldValues.length; i++) {
        if (seriesFieldValues[i].getField().equals(Series.IS_MIRROR)) {
          seriesFieldValues[i] = new FieldValue(Series.IS_MIRROR, true);
        }
        else if (seriesFieldValues[i].getField().equals(Series.MIRROR_SERIES)) {
          seriesFieldValues[i] = new FieldValue(Series.MIRROR_SERIES, key.get(Series.ID));
        }
      }
      Glob mirrorSeries = repository.create(Key.create(Series.TYPE, mirrorId), seriesFieldValues);
      repository.update(key, Series.MIRROR_SERIES, mirrorSeries.get(Series.ID));

      GlobList targetBudgets =
        repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorId).getGlobs();

      ReadOnlyGlobRepository.MultiFieldIndexed sourceBudgets =
        repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
      for (Glob budget : targetBudgets) {
        Glob sourceBudget = sourceBudgets.findByIndex(SeriesBudget.MONTH, budget.get(SeriesBudget.MONTH))
          .getGlobs().getFirst();
        repository.update(budget.getKey(), SeriesBudget.AMOUNT,
                          -Math.abs(sourceBudget.get(SeriesBudget.AMOUNT)));
        repository.update(sourceBudget.getKey(), SeriesBudget.AMOUNT, Math.abs(sourceBudget.get(SeriesBudget.AMOUNT)));
      }

      return mirrorId;
    }
    return null;
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
    private boolean closeOnDelete;

    public DeleteSeriesAction(boolean closeOnDelete) {
      super(Lang.get("seriesEdition.delete"));
      this.closeOnDelete = closeOnDelete;
      selectionService.addListener(new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          seriesToDelete = selection.getAll(Series.TYPE);
          setEnabled(!seriesToDelete.isEmpty());
        }
      }, Series.TYPE);
    }

    public void actionPerformed(ActionEvent e) {
      if (seriesToDelete.isEmpty()) {
        return;
      }

      Set<Integer> series = seriesToDelete.getValueSet(Series.ID);
      GlobList transactionsForSeries = localRepository.getAll(Transaction.TYPE, fieldIn(Transaction.SERIES, series));
      boolean deleted = false;
      SeriesDeleteDialog seriesDeleteDialog = new SeriesDeleteDialog(localRepository, localDirectory, dialog);
      if (transactionsForSeries.isEmpty()) {
        localRepository.delete(seriesToDelete);
        GlobList seriesToCategory = localRepository.getAll(SeriesToCategory.TYPE, fieldIn(SeriesToCategory.SERIES, series));
        localRepository.delete(seriesToCategory);
        deleted = true;
      }
      else if (seriesDeleteDialog.show()) {
        localRepository.delete(seriesToDelete);
        for (Glob transaction : transactionsForSeries) {
          localRepository.update(transaction.getKey(), Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);
        }
        GlobList seriesToCategory = localRepository.getAll(SeriesToCategory.TYPE, fieldIn(SeriesToCategory.SERIES, series));
        localRepository.delete(seriesToCategory);
        deleted = true;
      }

      if (deleted && closeOnDelete) {
        localRepository.commitChanges(false);
        localRepository.rollback();
        dialog.setVisible(false);
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
          BooleanField field = Series.getMonthField(monthIndex);
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
      checkBox.setSelected((currentSeries != null) && currentSeries.get(Series.getMonthField(monthIndex)));
    }
  }

  private abstract class OpenMonthChooserAction extends AbstractAction {
    private IntegerField date;
    private MonthRangeBound bound;

    private OpenMonthChooserAction(IntegerField date, MonthRangeBound bound) {
      this.date = date;
      this.bound = bound;
    }

    protected abstract Integer getMonthLimit();

    public void actionPerformed(ActionEvent e) {
      MonthRangeBound bound = this.bound;
      MonthChooserDialog chooser = new MonthChooserDialog(dialog, localDirectory);
      Integer monthId = currentSeries.get(date);
      Integer limit = getMonthLimit();
      if (monthId == null) {
        monthId = limit == null ? localDirectory.get(TimeService.class).getCurrentMonthId() : limit;
      }
      if (limit == null) {
        limit = 0;
        bound = MonthRangeBound.NONE;
      }
      int result = chooser.show(monthId, bound, limit);
      if (result == -1) {
        return;
      }
      localRepository.update(currentSeries.getKey(), date, result);
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
        if ((!BudgetArea.UNCATEGORIZED.getId().equals(glob.get(Series.BUDGET_AREA))
             && glob.get(Series.DEFAULT_CATEGORY) == null)
            || ((BudgetArea.SAVINGS.getId().equals(glob.get(Series.BUDGET_AREA))) &&
                (glob.get(Series.FROM_ACCOUNT) == null && glob.get(Series.TO_ACCOUNT) == null))) {
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

  private class ProfileTypeChangeListener extends DefaultChangeSetListener {
    public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
      changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          Glob series = repository.get(key);
          if (!values.contains(Series.PROFILE_TYPE)) {
            return;
          }

          if (values.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())
              && (values.contains(Series.IS_AUTOMATIC) || series.get(Series.IS_AUTOMATIC))) {
            // le trigger de passage en automatique peut ne pas etre encore appelle
            GlobList seriesBudgets =
              repository.getAll(SeriesBudget.TYPE, fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)));
            Glob currentMonth = repository.get(CurrentMonth.KEY);
            for (Glob budget : seriesBudgets) {
              if (budget.get(SeriesBudget.MONTH) > currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
                repository.update(budget.getKey(),
                                  FieldValue.value(SeriesBudget.AMOUNT, 0.0),
                                  FieldValue.value(SeriesBudget.OBSERVED_AMOUNT, 0.0));
              }
            }
          }
        }
      });
    }
  }

  private void alignFirstAndLastMonth(Key seriesKey, Glob series, GlobRepository repository) {
    Integer firstMonth = currentMonthIds.iterator().next();
    if (series.get(Series.FIRST_MONTH) == null) {
      repository.update(seriesKey, Series.FIRST_MONTH, firstMonth);
    }
    repository.update(seriesKey, Series.LAST_MONTH, series.get(Series.FIRST_MONTH));
    repository.update(seriesKey, Series.getMonthField(series.get(Series.FIRST_MONTH)), true);
  }

  private class SingleMonthProfileTypeUpdater extends DefaultChangeSetListener {
    public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
      changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
        public void visitUpdate(Key seriesKey, FieldValuesWithPrevious values) throws Exception {
          Glob series = repository.get(seriesKey);
          if (values.contains(Series.PROFILE_TYPE) &&
              values.get(Series.PROFILE_TYPE).equals(ProfileType.SINGLE_MONTH.getId())) {
            alignFirstAndLastMonth(seriesKey, series, repository);
          }
          if (series.get(Series.PROFILE_TYPE).equals(ProfileType.SINGLE_MONTH.getId())
              && (values.contains(Series.FIRST_MONTH) ||
                  values.contains(Series.LAST_MONTH))) {
            alignFirstAndLastMonth(seriesKey, series, repository);
          }
          if (values.contains(Series.PROFILE_TYPE)) {
            updateMonthSelectionCard();
          }
        }
      });
    }
  }

  private class ResetAllBudgetIfInAutomaticAndNoneAccountAreImported implements ChangeSetListener {
    public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
      changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Series.TO_ACCOUNT) || values.contains(Series.FROM_ACCOUNT)) {
            Glob series = repository.get(key);
            Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
            Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
            boolean noneImported = Account.areNoneImported(fromAccount, toAccount);
            if (noneImported && series.get(Series.IS_AUTOMATIC)) {
              repository.update(key, Series.IS_AUTOMATIC, false);
              GlobList seriesBudgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                                              key.get(Series.ID)).getGlobs();
              for (Glob budget : seriesBudgets) {
                repository.update(budget.getKey(), SeriesBudget.AMOUNT, 0.);
              }
            }
            SeriesEditionDialog.this.dayChooser.setVisible(noneImported);
            SeriesEditionDialog.this.savingsMessage.setVisible(fromAccount == null && toAccount == null);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    }
  }

  private static class UpdateDeleteMirror extends DefaultChangeSetListener {

    public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

      changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
          Integer mirror = previousValues.get(Series.MIRROR_SERIES);
          if (mirror != null && !previousValues.get(Series.IS_MIRROR)) {
            repository.delete(Key.create(Series.TYPE, mirror));
          }
        }
      });
    }
  }

  private static class UpdateUpdateMirror extends DefaultChangeSetListener {

    public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

      changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          Glob series = repository.get(key);
          Integer mirror = series.get(Series.MIRROR_SERIES);
          if (mirror != null && !series.get(Series.IS_MIRROR)) {
            final Key mirrorKey = Key.create(Series.TYPE, mirror);
            values.safeApply(new FieldValues.Functor() {
              public void process(Field field, Object value) throws Exception {
                if (!field.equals(Series.MIRROR_SERIES)) {
                  repository.update(mirrorKey, field, value);
                }
              }
            });
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });

      changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          Integer seriesId = values.get(SeriesBudget.SERIES);
          Glob series = repository.get(Key.create(Series.TYPE, seriesId));
          Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
          if (mirrorSeriesId != null && !series.get(Series.IS_MIRROR)) {
            FieldValue[] fieldValues = values.toArray();
            for (int i = 0; i < fieldValues.length; i++) {
              FieldValue value = fieldValues[i];
              if (value.getField().equals(SeriesBudget.SERIES)) {
                fieldValues[i] = new FieldValue(SeriesBudget.SERIES, mirrorSeriesId);
              }
              else if (value.getField().equals(SeriesBudget.AMOUNT)) {
                fieldValues[i] = new FieldValue(SeriesBudget.AMOUNT, -values.get(SeriesBudget.AMOUNT));
              }
            }
            repository.create(Key.create(SeriesBudget.TYPE, repository.getIdGenerator()
              .getNextId(SeriesBudget.ID, 1)), fieldValues);
          }
        }

        public void visitUpdate(Key key, final FieldValuesWithPrevious values) throws Exception {
          Glob budget = repository.get(key);
          Integer seriesId = budget.get(SeriesBudget.SERIES);
          Glob series = repository.get(Key.create(Series.TYPE, seriesId));
          Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
          if (mirrorSeriesId != null && !series.get(Series.IS_MIRROR)) {
            final Glob mirrorBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeriesId)
              .findByIndex(SeriesBudget.MONTH, budget.get(SeriesBudget.MONTH)).getGlobs().getFirst();
            values.safeApply(new FieldValues.Functor() {
              public void process(Field field, Object value) throws Exception {
                if (field.equals(SeriesBudget.AMOUNT)) {
                  repository.update(mirrorBudget.getKey(), field, -((Double)value));
                }
                else {
                  repository.update(mirrorBudget.getKey(), field, value);
                }
              }
            });
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
          Integer seriesId = previousValues.get(SeriesBudget.SERIES);
          Glob series = repository.find(Key.create(Series.TYPE, seriesId));
          if (series == null) {
            return;
          }
          Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
          if (mirrorSeriesId != null && !series.get(Series.IS_MIRROR)) {
            Glob budget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeriesId)
              .findByIndex(SeriesBudget.MONTH, previousValues.get(SeriesBudget.MONTH)).getGlobs().getFirst();
            if (budget != null) {
              repository.delete(budget.getKey());
            }
          }
        }
      });
    }
  }

  private static class UpdateBudgetOnSeriesAccountsChange extends DefaultChangeSetListener {
    public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
      changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Series.TO_ACCOUNT) || values.contains(Series.FROM_ACCOUNT)) {
            Glob series = repository.get(key);
            if (series.get(Series.IS_AUTOMATIC)) {
              return;
            }
            Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
            Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
            double multiplier = computeMultiplier(fromAccount, toAccount, repository);
            if (!series.get(Series.IS_MIRROR)) {
              GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE,
                                                         fieldEquals(SeriesBudget.SERIES, key.get(Series.ID)));
              for (Glob budget : seriesBudgets) {
                repository.update(budget.getKey(), SeriesBudget.AMOUNT,
                                  multiplier * Math.abs(budget.get(SeriesBudget.AMOUNT)));
              }
            }
          }
        }
      });
    }
  }
}
