package org.designup.picsou.gui.series;

import org.designup.picsou.gui.accounts.CreateAccountAction;
import org.designup.picsou.gui.components.MonthRangeBound;
import org.designup.picsou.gui.components.ReadOnlyGlobTextFieldView;
import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.MonthYearStringifier;
import org.designup.picsou.gui.series.edition.MonthCheckBoxUpdater;
import org.designup.picsou.gui.series.subseries.SubSeriesEditionPanel;
import org.designup.picsou.gui.signpost.actions.SetSignpostStatusAction;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.AutomaticSeriesBudgetTrigger;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobCheckBoxView;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.*;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Ref;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
  private Integer lastSelectedSubSeriesId;
  private Set<Integer> currentMonthIds = Collections.emptySet();

  private JLabel titleLabel;
  private SeriesEditionDialog.ValidateAction okAction = new ValidateAction();
  private AbstractAction deleteStartDateAction;
  private OpenMonthChooserAction startDateChooserAction;
  private AbstractAction deleteEndDateAction;
  private OpenMonthChooserAction endDateChooserAction;
  private OpenMonthChooserAction singleMonthChooserAction;
  private GlobTextEditor nameEditor;
  private JPanel monthSelectionPanel;
  private Key createdSeries;
  private SeriesAmountEditionPanel amountEditionPanel;
  private GlobList selectedTransactions = new EmptyGlobList();
  private GlobLinkComboEditor fromAccountsCombo;
  private GlobLinkComboEditor toAccountsCombo;
  private Boolean isAutomatic = false;
  private JComboBox dayChooser;
  private CardHandler monthSelectionCards;
  private JLabel savingsMessage;
  private SubSeriesEditionPanel subSeriesEditionPanel;
  private GlobMatcher accountFilter;
  private GlobLinkComboEditor budgetAreaCombo;
  private JTabbedPane tabbedPane;
  private GlobCheckBoxView reportCheckBox;
  private ReadOnlyGlobTextFieldView startTextFieldView;
  private ReadOnlyGlobTextFieldView endDateTextFieldView;

  private static Set<Integer> CHANGEABLE_BUDGET_AREAS =
    new HashSet<Integer>(Arrays.asList(BudgetArea.VARIABLE.getId(), BudgetArea.RECURRING.getId(), BudgetArea.EXTRAS.getId()));

  public SeriesEditionDialog(final GlobRepository repository, Directory directory) {
    this(directory.get(JFrame.class), repository, directory);
  }

  public SeriesEditionDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(BudgetArea.TYPE, Month.TYPE, CurrentMonth.TYPE,
            ProfileType.TYPE, Account.TYPE, SubSeries.TYPE,
            Bank.TYPE, BankEntity.TYPE, AccountUpdateMode.TYPE)
      .get();

    localRepository.addTrigger(new SingleMonthProfileTypeUpdater());
    localRepository.addTrigger(new ResetAllBudgetIfInAutomaticAndNoneAccountAreImported());
    addSeriesCreationTriggers(localRepository, new ProfileTypeSeriesTrigger.UserMonth() {
      public Set<Integer> getMonthWithTransaction() {
        return selectedTransactions.getSortedSet(Transaction.BUDGET_MONTH);
      }
    }, repository);
//    localRepository.addTrigger(new UpdateUpdateMirror());
//    localRepository.addTrigger(new UpdateBudgetOnSeriesAccountsChange());
    localRepository.addChangeListener(new ProfileTypeChangeListener());

    localRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (currentSeries == null) {
          return;
        }
        if (changeSet.containsChanges(currentSeries.getKey())) {
          FieldValues previousValue = changeSet.getPreviousValue(currentSeries.getKey());
          if (previousValue.contains(Series.IS_AUTOMATIC)) {
            isAutomatic = currentSeries.get(Series.IS_AUTOMATIC);
          }
          if (previousValue.contains(Series.PROFILE_TYPE)) {
            if (previousValue.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())) {
              repository.update(currentSeries.getKey(), Series.IS_AUTOMATIC, isAutomatic);
            }
            else if (currentSeries.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())) {
              repository.update(currentSeries.getKey(), Series.IS_AUTOMATIC, false);
            }
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        currentSeries = null;
        selectionService.clear(Series.TYPE);
      }
    });

    selectionService = new SelectionService();
    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);

    dialog = PicsouDialog.create(parent, directory);
    dialog.addOnWindowClosedAction(new SetSignpostStatusAction(SignpostStatus.SERIES_PERIODICITY_CLOSED,
                                                               SignpostStatus.SERIES_PERIODICITY_SHOWN,
                                                               repository));

    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/series/seriesEditionDialog.splits",
                                                      localRepository, localDirectory);

    tabbedPane = builder.add("tabs", new JTabbedPane()).getComponent();
    titleLabel = builder.add("title", new JLabel("SeriesEditionDialog")).getComponent();

    nameEditor = builder.addEditor("nameField", Series.NAME).setNotifyOnKeyPressed(true);
    nameEditor.getComponent().addActionListener(okAction);

    reportCheckBox = builder.addCheckBox("autoReport", Series.SHOULD_REPORT);
    localRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
        changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
          public void visitCreation(Key key, FieldValues values) throws Exception {
            reportCheckBox.getComponent().setVisible(false && !values.get(Series.IS_AUTOMATIC));
          }

          public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
            if (values.contains(Series.IS_AUTOMATIC)) {
              if (values.get(Series.IS_AUTOMATIC)) {
                repository.update(key, Series.SHOULD_REPORT, false);
                reportCheckBox.getComponent().setVisible(false);
              }
              else {
                reportCheckBox.getComponent().setVisible(false);
              }
            }
          }
        });
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {

      }
    });

    builder.addMultiLineEditor("descriptionField", Series.DESCRIPTION).setNotifyOnKeyPressed(true);

    accountFilter = and(not(fieldEquals(Account.ID, Account.ALL_SUMMARY_ACCOUNT_ID)),
                        or(fieldEquals(Account.ID, Account.MAIN_SUMMARY_ACCOUNT_ID),
                           not(fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()))),
                        not(fieldEquals(Account.ID, Account.SAVINGS_SUMMARY_ACCOUNT_ID)));

    fromAccountsCombo = GlobLinkComboEditor.init(Series.FROM_ACCOUNT, localRepository, localDirectory)
      .setShowEmptyOption(false)
      .setFilter(accountFilter);
    builder.add("fromAccount", fromAccountsCombo);

    toAccountsCombo = GlobLinkComboEditor.init(Series.TO_ACCOUNT, localRepository, localDirectory)
      .setShowEmptyOption(false)
      .setFilter(accountFilter);
    builder.add("toAccount", toAccountsCombo);

    CreateAccountAction action = new CreateAccountAction(AccountType.SAVINGS, localRepository, directory, dialog);
    builder.add("createAccount", action);
    action.setAccountTypeEditable(false);

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
      GlobLinkComboEditor.init(Series.PROFILE_TYPE, localRepository, localDirectory)
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

    amountEditionPanel = new SeriesAmountEditionPanel(localRepository, localDirectory);
    JPanel seriesBudgetPanel = amountEditionPanel.getPanel();
    builder.add("seriesAmountEditionPanel", seriesBudgetPanel);

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        setCurrentSeries(selectionService.getSelection(Series.TYPE).getFirst());
        if (currentSeries != null) {
          boolean isSavingsSeries = currentSeries.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId());
          fromAccountsCombo.setVisible(isSavingsSeries);
          toAccountsCombo.setVisible(isSavingsSeries);
          dayChooser.setSelectedItem(currentSeries.get(Series.DAY));
          Glob fromAccount = repository.findLinkTarget(currentSeries, Series.FROM_ACCOUNT);
          Glob toAccount = repository.findLinkTarget(currentSeries, Series.TO_ACCOUNT);
          boolean noneImported = Account.areNoneImported(fromAccount, toAccount);
          dayChooser.setVisible(noneImported);
          savingsMessage.setVisible(!isValidSeries(currentSeries));
          okAction.setEnabled(isValidSeries(currentSeries));
        }
        updateDateSelectors();
        updateMonthChooser();
        updateMonthSelectionCard();
        updateBudgetAreaCombo();
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
                          MonthCheckBoxUpdater updater = new MonthCheckBoxUpdater(monthIndex, localRepository, selectionService);
                          cellBuilder.add("monthSelector", updater.getCheckBox());
                          localRepository.addChangeListener(updater);
                        }
                      });

    budgetAreaCombo = builder.addComboEditor("budgetAreaChooser", Series.BUDGET_AREA)
      .setShowEmptyOption(false)
      .setFilter(GlobMatchers.contained(BudgetArea.ID, CHANGEABLE_BUDGET_AREAS))
      .setComparator(new GlobFieldComparator(BudgetArea.ID));

    subSeriesEditionPanel = new SubSeriesEditionPanel(localRepository, localDirectory, dialog);
    builder.add("subSeriesEditionPanel", subSeriesEditionPanel.getPanel());

    localRepository.addChangeListener(new OkButtonUpdater());

    JPanel panel = builder.load();
    JButton deleteButton = new JButton(new DeleteSeriesAction("seriesEdition.deleteCurrent", true));
    deleteButton.setOpaque(false);
    deleteButton.setName("deleteSingleSeries");
    dialog.addPanelWithButtons(panel, okAction, new CancelAction(), deleteButton);
  }

  private boolean isValidSeries(Glob series) {
    return !series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId()) ||
           ((series.get(Series.FROM_ACCOUNT) != null && series.get(Series.TO_ACCOUNT) != null) &&
            !series.get(Series.FROM_ACCOUNT).equals(series.get(Series.TO_ACCOUNT)));
  }

  private void updateBudgetAreaCombo() {
    budgetAreaCombo.setVisible((budgetArea != null) && CHANGEABLE_BUDGET_AREAS.contains(budgetArea.getId()));
  }

  public static void addSeriesCreationTriggers(GlobRepository repository,
                                               final ProfileTypeSeriesTrigger.UserMonth userMonth,
                                               GlobRepository parentRepository) {
    repository.addTrigger(new ProfileTypeSeriesTrigger(userMonth));
    repository.addTrigger(new AutomaticSeriesBudgetTrigger());
    repository.addTrigger(new SeriesBudgetTrigger(parentRepository));
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

  private void registerDateRangeComponents(GlobsPanelBuilder builder) {

    startTextFieldView = ReadOnlyGlobTextFieldView.init(Series.TYPE, localRepository, localDirectory,
                                                        new MonthYearStringifier(Series.FIRST_MONTH));
    builder.add("seriesStartDate", startTextFieldView);

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
            .getGlobs()
            .filterSelf(isFalse(Transaction.PLANNED), localRepository)
            .filterSelf(isFalse(Transaction.CREATED_BY_SERIES), localRepository)
            .sort(Transaction.BUDGET_MONTH);
        Glob firstMonth = transactions.getFirst();
        if (firstMonth == null) {
          return currentSeries.get(Series.LAST_MONTH);
        }
        if (currentSeries.get(Series.LAST_MONTH) != null) {
          return Math.min(firstMonth.get(Transaction.BUDGET_MONTH), currentSeries.get(Series.LAST_MONTH));
        }
        return firstMonth.get(Transaction.BUDGET_MONTH);
      }
    };
    builder.add("seriesStartDateChooser", startDateChooserAction);
    this.startTextFieldView.getComponent().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (currentSeries != null) {
          startDateChooserAction.actionPerformed(null);
        }
      }
    });

    endDateTextFieldView = ReadOnlyGlobTextFieldView.init(Series.TYPE, localRepository, localDirectory,
                                                          new MonthYearStringifier(Series.LAST_MONTH));
    builder.add("seriesEndDate", endDateTextFieldView);

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
            .getGlobs()
            .filterSelf(isFalse(Transaction.CREATED_BY_SERIES), localRepository)
            .sort(Transaction.BUDGET_MONTH);
        Glob lastMonth = transactions.getLast();
        if (lastMonth == null) {
          return currentSeries.get(Series.FIRST_MONTH);
        }
        if (currentSeries.get(Series.FIRST_MONTH) != null) {
          return Math.max(lastMonth.get(Transaction.BUDGET_MONTH), currentSeries.get(Series.FIRST_MONTH));
        }
        return lastMonth.get(Transaction.BUDGET_MONTH);
      }
    };
    builder.add("seriesEndDateChooser", endDateChooserAction);
    this.endDateTextFieldView.getComponent().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (currentSeries != null) {
          endDateChooserAction.actionPerformed(null);
        }
      }
    });
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

  private void resetSeries() {
    amountEditionPanel.clear();
  }

  public void show(Glob series, Set<Integer> monthIds) {
    resetSeries();
    retrieveAssociatedTransactions(series.get(Series.ID));
    try {
      localRepository.startChangeSet();
      localRepository.rollback();
      initBudgetAreaSeries(BudgetArea.get(series.get(Series.BUDGET_AREA)), new Ref<Integer>(), new Ref<Integer>());
    }
    finally {
      localRepository.completeChangeSet();
    }
    doShow(monthIds, localRepository.get(series.getKey()), false, false);
  }

  public Key showNewSeries(GlobList transactions, GlobList selectedMonths, BudgetArea budgetArea, FieldValue... forcedValues) {
    resetSeries();
    selectedTransactions = transactions;
    Glob createdSeries;
    try {
      localRepository.startChangeSet();
      localRepository.rollback();

      this.budgetArea = budgetArea;
      Ref<Integer> fromAccount = new Ref<Integer>();
      Ref<Integer> toAccount = new Ref<Integer>();
      initBudgetAreaSeries(budgetArea, fromAccount, toAccount);

      String label;
      if (!transactions.isEmpty() && budgetArea == BudgetArea.RECURRING
          && !transactions.get(0).get(Transaction.TRANSACTION_TYPE).equals(TransactionType.CHECK.getId())) {
        Glob firstTransaction = transactions.get(0);
        label = Transaction.anonymise(firstTransaction.get(Transaction.LABEL));
      }
      else {
        label = Lang.get("seriesEdition.newSeries");
      }

      SortedSet<Integer> days = transactions.getSortedSet(Transaction.DAY);
      Integer day = days.isEmpty() ? 1 : days.last();
      createdSeries = createSeries(label, day, fromAccount.get(), toAccount.get(), forcedValues);
    }
    finally {
      localRepository.completeChangeSet();
    }
    this.createdSeries = null;
    doShow(selectedMonths.getValueSet(Month.ID), createdSeries, true, true);
    return this.createdSeries;
  }

  private void retrieveAssociatedTransactions(Integer seriesId) {
    selectedTransactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES,
                                                  seriesId).getGlobs();
    selectedTransactions.removeAll(and(isTrue(Transaction.PLANNED),
                                       isTrue(Transaction.CREATED_BY_SERIES)),
                                   repository);
  }

  private Glob createSeries(String label, Integer day, Integer fromAccountId, Integer toAccountId, FieldValue... forcedValues) {
    FieldValuesBuilder values =
      FieldValuesBuilder.init(value(Series.BUDGET_AREA, budgetArea.getId()),
                              value(Series.INITIAL_AMOUNT, null),
                              value(Series.NAME, label),
                              value(Series.DAY, day),
                              value(Series.IS_AUTOMATIC, budgetArea.isAutomatic()),
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
    if (fromAccountId != null) {
      values.set(value(Series.FROM_ACCOUNT, fromAccountId));
      values.set(value(Series.TARGET_ACCOUNT, fromAccountId));
    }
    if (toAccountId != null) {
      values.set(value(Series.TO_ACCOUNT, toAccountId));
    }
    if (budgetArea == BudgetArea.EXTRAS) {
      values.set(Series.IS_AUTOMATIC, false);
      SelectionService selectionService = directory.get(SelectionService.class);
      if (!selectedTransactions.isEmpty()) {
        SortedSet<Integer> months = selectedTransactions.getSortedSet(Transaction.BUDGET_MONTH);
        values.set(Series.FIRST_MONTH, months.first());
        values.set(Series.LAST_MONTH, months.last());
        if (selectedTransactions.size() == 1) {
          values.set(Series.PROFILE_TYPE, ProfileType.SINGLE_MONTH.getId());
          values.set(Series.INITIAL_AMOUNT, selectedTransactions.getFirst().get(Transaction.AMOUNT));
        }
      }
      else {
        GlobList monthIds = selectionService.getSelection(Month.TYPE).sort(Month.ID);
        if (!monthIds.isEmpty()) {
          values.set(Series.FIRST_MONTH, monthIds.getFirst().get(Month.ID));
          values.set(Series.LAST_MONTH, monthIds.getLast().get(Month.ID));
        }
        else {
          int monthId = localDirectory.get(TimeService.class).getCurrentMonthId();
          values.set(Series.FIRST_MONTH, monthId);
          values.set(Series.LAST_MONTH, monthId);
        }
        if (monthIds.size() == 1) {
          values.set(Series.PROFILE_TYPE, ProfileType.SINGLE_MONTH.getId());
        }
      }
    }

    values.set(forcedValues);

    return localRepository.create(Series.TYPE, values.toArray());
  }

  private void initBudgetAreaSeries(BudgetArea budgetArea, Ref<Integer> fromAccount, Ref<Integer> toAccount) {
    this.budgetArea = budgetArea;

    GlobList seriesList =
      repository.getAll(Series.TYPE, not(fieldEquals(Series.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId())));

    GlobList globsToLoad = new GlobList();
    for (Glob series : seriesList) {
      globsToLoad.add(series);
      globsToLoad.addAll(repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                                series.get(Series.ID)).getGlobs());
      ReadOnlyGlobRepository.MultiFieldIndexed index =
        repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID));
      globsToLoad.addAll(index.getGlobs().filterSelf(isFalse(Transaction.PLANNED), repository));
    }
    localRepository.reset(globsToLoad, SeriesBudget.TYPE, Series.TYPE, Transaction.TYPE);

    if (budgetArea == BudgetArea.SAVINGS) {
      Set<Integer> positiveAccount = new HashSet<Integer>();
      Set<Integer> negativeAccount = new HashSet<Integer>();
      for (Glob transaction : selectedTransactions) {
        Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
//        if (account.isTrue(Account.IS_IMPORTED_ACCOUNT) &&
//            account.get(Account.UPDATE_MODE).equals(AccountUpdateMode.AUTOMATIC.getId())) {
          Integer accountId = transaction.get(Transaction.ACCOUNT);
          if (AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE))) {
            accountId = Account.MAIN_SUMMARY_ACCOUNT_ID;
          }
          if (transaction.get(Transaction.AMOUNT) >= 0) {
            positiveAccount.add(accountId);
          }
          else {
            negativeAccount.add(accountId);
          }
//        }
      }
      if (positiveAccount.size() == 1) {
        toAccountsCombo
          .setFilter(fieldEquals(Account.ID, positiveAccount.iterator().next()));
        toAccount.set(positiveAccount.iterator().next());
      }
      else {
        if (negativeAccount.size() == 1) {
          toAccountsCombo.setFilter(
            and(accountFilter,
                GlobMatchers.not(fieldEquals(Account.ID, negativeAccount.iterator().next()))));
        }
        else {
          toAccountsCombo.setFilter(accountFilter);
        }
      }
      if (negativeAccount.size() == 1) {
        fromAccountsCombo
          .setFilter(fieldEquals(Account.ID, negativeAccount.iterator().next()));
        fromAccount.set(negativeAccount.iterator().next());
      }
      else {
        if (positiveAccount.size() == 1) {
          fromAccountsCombo.setFilter(
            and(accountFilter,
                GlobMatchers.not(fieldEquals(Account.ID, positiveAccount.iterator().next()))));
        }
        else {
          fromAccountsCombo.setFilter(accountFilter);
        }
      }
    }
  }

  private void doShow(Set<Integer> monthIds, Glob series, boolean creation, final Boolean selectName) {
    setCurrentSeries(series);
    this.currentMonthIds = new TreeSet<Integer>(monthIds);
    this.lastSelectedSubSeriesId = null;
    amountEditionPanel.selectMonths(monthIds);
    amountEditionPanel.setCurrentSeries(currentSeries.getKey());
    selectionService.select(currentSeries);
    updateMonthSelectionCard();

    tabbedPane.setSelectedIndex(0);

    titleLabel.setText(Lang.get("seriesEdition.title." + (creation ? "creation" : "edition")));

    dialog.pack();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (selectName != null) {
          if (selectName) {
            nameEditor.getComponent().requestFocusInWindow();
            nameEditor.getComponent().selectAll();
          }
          else {
            amountEditionPanel.selectAmountEditor();
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

  public void setCurrentSeries(Glob currentSeries) {
    this.currentSeries = currentSeries;
    this.subSeriesEditionPanel.setCurrentSeries(currentSeries);
    endDateTextFieldView.getComponent().setEnabled(this.currentSeries != null);
    startTextFieldView.getComponent().setEnabled(this.currentSeries != null);
    reportCheckBox.getComponent().setEnabled(this.currentSeries != null);
    reportCheckBox.getComponent().setVisible(false); //this.currentSeries != null && !currentSeries.get(Series.IS_AUTOMATIC));
    if (currentSeries != null) {
      isAutomatic = currentSeries.get(Series.IS_AUTOMATIC);
    }
  }

  private Integer getCurrentSubSeriesId() {
    GlobList subSeriesList = selectionService.getSelection(SubSeries.TYPE);
    if (subSeriesList.size() != 1) {
      return null;
    }
    return subSeriesList.getFirst().get(SubSeries.ID);
  }

  public Integer getLastSelectedSubSeriesId() {
    return lastSelectedSubSeriesId;
  }

  private class ValidateAction extends AbstractAction {

    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      trimNames();
      if (currentSeries != null) {
        createdSeries = currentSeries.getKey();
        lastSelectedSubSeriesId = SeriesEditionDialog.this.getCurrentSubSeriesId();
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
//    ChangeSet changeSet = localRepository.getCurrentChanges();


//    localRepository.startChangeSet();
//    try {
//      changeSet.safeVisit(SeriesBudget.TYPE, new UpdateMirrorSeriesBudgetChangeSetVisitor(localRepository));
//    }
//    finally {
//      localRepository.completeChangeSet();
//    }
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

  private class DeleteSeriesAction extends AbstractAction {
    private GlobList seriesToDelete = GlobList.EMPTY;
    private boolean closeOnDelete;

    public DeleteSeriesAction(String labelKey, boolean closeOnDelete) {
      super(Lang.get(labelKey));
      this.closeOnDelete = closeOnDelete;
      selectionService.addListener(new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          seriesToDelete = new GlobList();
          GlobList currentSeries = selection.getAll(Series.TYPE);
          for (Glob series : currentSeries) {
            Glob mirrorSeries = repository.findLinkTarget(series, Series.MIRROR_SERIES);
            if (mirrorSeries != null) {
              seriesToDelete.add(mirrorSeries);
            }
            seriesToDelete.add(series);
          }
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
      SeriesDeletionDialog seriesDeletionDialog = new SeriesDeletionDialog(localRepository, localDirectory, dialog);
      if (transactionsForSeries.isEmpty()) {
        GlobList tmp = new GlobList(seriesToDelete);
        selectionService.clear(Series.TYPE);
        localRepository.delete(tmp);
        deleted = true;
      }
      else if (seriesDeletionDialog.show()) {
        GlobList tmp = new GlobList(seriesToDelete);
        selectionService.clear(Series.TYPE);
        localRepository.delete(tmp);
        deleted = true;
      }

      if (deleted && closeOnDelete) {
        localRepository.commitChanges(false);
        localRepository.rollback();
        dialog.setVisible(false);
      }
    }
  }

  private abstract class OpenMonthChooserAction extends AbstractAction {
    private IntegerField dateField;
    private MonthRangeBound bound;

    private OpenMonthChooserAction(IntegerField dateField, MonthRangeBound bound) {
      this.dateField = dateField;
      this.bound = bound;
    }

    protected abstract Integer getMonthLimit();

    public void actionPerformed(ActionEvent e) {
      MonthRangeBound bound = this.bound;
      MonthChooserDialog chooser = new MonthChooserDialog(dialog, localDirectory);
      Integer monthId = currentSeries.get(dateField);
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
      localRepository.update(currentSeries.getKey(), dateField, result);
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
        if (!isValidSeries(glob)) {
          okAction.setEnabled(false);
          return;
        }
      }
      okAction.setEnabled(true);
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
              && (values.contains(Series.IS_AUTOMATIC) || series.isTrue(Series.IS_AUTOMATIC))) {
            // le trigger de passage en automatique peut ne pas etre encore appelle
            GlobList seriesBudgets =
              repository.getAll(SeriesBudget.TYPE, fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)));
            Glob currentMonth = repository.get(CurrentMonth.KEY);
            for (Glob budget : seriesBudgets) {
              if (budget.get(SeriesBudget.MONTH) > currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
                repository.update(budget.getKey(),
                                  value(SeriesBudget.AMOUNT, 0.0),
                                  value(SeriesBudget.OBSERVED_AMOUNT, null));
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
            SeriesEditionDialog.this.dayChooser.setVisible(noneImported);
            SeriesEditionDialog.this.savingsMessage.setVisible(!isValidSeries(series));
            SeriesEditionDialog.this.okAction.setEnabled(isValidSeries(series));
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    }
  }

//  private static class UpdateBudgetOnSeriesAccountsChange extends DefaultChangeSetListener {
//
//    public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
//      changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
//        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
//          if (values.contains(Series.TO_ACCOUNT) || values.contains(Series.FROM_ACCOUNT)) {
//            Glob series = repository.get(key);
//            Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
//            Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
////            double multiplier = Account.computeAmountMultiplier(fromAccount, toAccount, repository);
//            double multiplier = series.get(Series.FROM_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT)) ? -1. : 1.;
//            GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE,
//                                                       fieldEquals(SeriesBudget.SERIES, key.get(Series.ID)));
//            for (Glob budget : seriesBudgets) {
//              repository.update(budget.getKey(), SeriesBudget.AMOUNT,
//                                budget.get(SeriesBudget.AMOUNT) == null ? null : multiplier * Math.abs(budget.get(SeriesBudget.AMOUNT)));
//            }
//          }
//        }
////        }
//      });
//    }
//  }
}
