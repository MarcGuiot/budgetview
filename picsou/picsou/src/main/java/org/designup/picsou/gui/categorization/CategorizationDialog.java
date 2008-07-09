package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.components.DisposeCallback;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.series.SeriesCreationDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CategorizationDialog {
  private SelectionService selectionService = new SelectionService();
  private LocalGlobRepository localRepository;
  private Glob currentTransaction;
  private GlobStringifier budgetAreaStringifier;
  private GlobStringifier seriesStringifier;
  private GlobStringifier categoryStringifier;
  private PicsouDialog dialog;
  private int transactionIndex = 0;
  private GlobList transactions;
  private JToggleButton invisibleBudgetAreaToggle;

  public CategorizationDialog(Window parent, final GlobRepository repository, Directory directory) {

    final Directory localDirectory = init(repository, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationDialog.splits",
                                                      localRepository, localDirectory);

    builder.addMultiLineTextView("transactionLabel", Transaction.TYPE);

    builder.add("nextTransaction", new NextTransactionAction(selectionService));
    builder.add("previousTransaction", new PreviousTransactionAction(selectionService));

    final CardHandler cardHandler = builder.addCardHandler("cards");

    invisibleBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cardHandler.show("noBudgetArea");
      }
    });
    builder.add("invisibleBudgetAreaToggle", invisibleBudgetAreaToggle);
    builder.addRepeat("budgetAreas", BudgetArea.TYPE.getConstants(),
                      new BudgetAreaComponentFactory(cardHandler));

    JToggleButton invisibleIncomeToggle = new JToggleButton();
    builder.add("invisibleIncomeToggle", invisibleIncomeToggle);
    builder.addRepeat("incomeSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.INCOME.getGlob(), Series.BUDGET_AREA),
                      new GlobFieldComparator(Series.ID),
                      new SeriesComponentFactory(invisibleIncomeToggle));
    builder.add("createIncomeSeries", new SeriesCreationAction(BudgetArea.INCOME, localDirectory));

    JToggleButton invisibleRecurringToggle = new JToggleButton();
    builder.add("invisibleRecurringToggle", invisibleRecurringToggle);
    builder.addRepeat("recurringSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.RECURRING_EXPENSES.getGlob(), Series.BUDGET_AREA),
                      new GlobFieldComparator(Series.ID),
                      new SeriesComponentFactory(invisibleRecurringToggle));
    builder.add("createRecurringSeries", new SeriesCreationAction(BudgetArea.RECURRING_EXPENSES, localDirectory));

    final JToggleButton invisibleEnvelopeToggle = new JToggleButton();
    builder.add("invisibleEnvelopeToggle", invisibleEnvelopeToggle);
    builder.addRepeat("envelopeSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.EXPENSES_ENVELOPE.getGlob(), Series.BUDGET_AREA),
                      new GlobFieldComparator(Series.ID),
                      new EnvelopeSeriesComponentFactory(invisibleEnvelopeToggle));

    JToggleButton invisibleOccasionalToggle = new JToggleButton();
    builder.add("invisibleOccasionalToggle", invisibleOccasionalToggle);
    builder.addRepeat("occasionalSeriesRepeat",
                      SeriesToCategory.TYPE,
                      new GlobMatcher() {
                        public boolean matches(Glob seriesToCategory, GlobRepository repository) {
                          final Glob series = repository.findLinkTarget(seriesToCategory, SeriesToCategory.SERIES);
                          return series.get(Series.BUDGET_AREA).equals(BudgetArea.OCCASIONAL_EXPENSES.getId());
                        }
                      },
                      new GlobFieldComparator(SeriesToCategory.ID),
                      new SeriesToCategoryComponentFactory("occasionalSeries", "occasionalCategoryToggle", BudgetArea.OCCASIONAL_EXPENSES,
                                                           invisibleOccasionalToggle));

    builder.add("ok", new AbstractAction("ok") {
      public void actionPerformed(ActionEvent e) {
        localRepository.commitChanges(false);
        dialog.setVisible(false);
      }
    });

    builder.add("cancel", new AbstractAction("cancel") {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });

    JPanel panel = builder.load();
    dialog = PicsouDialog.create(parent);
    dialog.setContentPane(panel);
    dialog.setWindowCloseCallback(new DisposeCallback() {
      public void processDispose() {
      }
    });
  }

  private Directory init(GlobRepository repository, Directory directory) {

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(BudgetArea.TYPE, Category.TYPE, Series.TYPE, SeriesToCategory.TYPE)
      .get();

    Directory localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransaction = selection.getAll(Transaction.TYPE).get(0);
        if ((currentTransaction != null) && (currentTransaction.get(Transaction.SERIES) == null)) {
          invisibleBudgetAreaToggle.doClick();
        }
      }
    }, Transaction.TYPE);

    DescriptionService descriptionService = localDirectory.get(DescriptionService.class);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);
    seriesStringifier = descriptionService.getStringifier(Series.TYPE);
    categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    return localDirectory;
  }

  public void show(GlobList transactions) {
    if (transactions.isEmpty()) {
      return;
    }
    localRepository.rollback();
    transactionIndex = 0;
    localRepository.reset(transactions, Transaction.TYPE);
    this.transactions = localRepository.getAll(Transaction.TYPE).sort(TransactionComparator.ASCENDING);
    selectionService.select(this.transactions.get(transactionIndex));
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private class BudgetAreaComponentFactory implements RepeatComponentFactory<Glob> {
    private final CardHandler cardHandler;
    private final ButtonGroup budgetAreasGroup;

    public BudgetAreaComponentFactory(CardHandler cardHandler) {
      this.cardHandler = cardHandler;
      this.budgetAreasGroup = new ButtonGroup();
      this.budgetAreasGroup.add(invisibleBudgetAreaToggle);
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob budgetArea) {
      String name = budgetAreaStringifier.toString(budgetArea, localRepository);
      final JToggleButton toggleButton = new JToggleButton(new AbstractAction(name) {
        public void actionPerformed(ActionEvent e) {
          cardHandler.show(budgetArea.get(BudgetArea.NAME));
        }
      });
      toggleButton.setName(budgetArea.get(BudgetArea.NAME));
      cellBuilder.add("budgetAreaToggle", toggleButton);
      budgetAreasGroup.add(toggleButton);
      final BudgetAreaToggleUpdater listener = new BudgetAreaToggleUpdater(toggleButton, budgetArea, localRepository);
      localRepository.addChangeListener(listener);
      selectionService.addListener(listener, Transaction.TYPE);
      cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
        public void dispose() {
          budgetAreasGroup.remove(toggleButton);
          selectionService.removeListener(listener);
          localRepository.removeChangeListener(listener);
        }
      });
    }
  }

  private class SeriesComponentFactory implements RepeatComponentFactory<Glob> {
    ButtonGroup seriesGroup = new ButtonGroup();
    private JToggleButton invisibleToggle;

    public SeriesComponentFactory(JToggleButton invisibleToggle) {
      this.invisibleToggle = invisibleToggle;
      seriesGroup.add(invisibleToggle);
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
      String name = seriesStringifier.toString(series, localRepository);
      final Key seriesKey = series.getKey();
      final Key categoryKey = series.getTargetKey(Series.DEFAULT_CATEGORY);

      final JToggleButton toggle = new JToggleButton(new AbstractAction(name) {
        public void actionPerformed(ActionEvent e) {
          localRepository.setTarget(currentTransaction.getKey(), Transaction.SERIES, seriesKey);
          localRepository.setTarget(currentTransaction.getKey(), Transaction.CATEGORY, categoryKey);
        }
      });
      seriesGroup.add(toggle);

      final GlobSelectionListener listener = new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          GlobList transactions = selection.getAll(Transaction.TYPE);
          if (transactions.size() != 1) {
            return;
          }
          Glob transaction = transactions.get(0);
          Glob transactionSeries = localRepository.findLinkTarget(transaction, Transaction.SERIES);
          if (transactionSeries != null) {
            toggle.setSelected(transactionSeries.getKey().equals(seriesKey));
          }
          else {
            invisibleToggle.setSelected(true);
          }
        }
      };
      selectionService.addListener(listener, Transaction.TYPE);

      cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
        public void dispose() {
          selectionService.removeListener(listener);
          seriesGroup.remove(toggle);
        }
      });
      cellBuilder.add("seriesToggle", toggle);
    }
  }

  private class EnvelopeSeriesComponentFactory implements RepeatComponentFactory<Glob> {
    private JToggleButton invisibleToggle;

    public EnvelopeSeriesComponentFactory(JToggleButton invisibleToggle) {
      this.invisibleToggle = invisibleToggle;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
      cellBuilder.add("envelopeSeriesName",
                      new JLabel(seriesStringifier.toString(series, localRepository)));

      cellBuilder.addRepeat("envelopeCategoryRepeat",
                            localRepository.findLinkedTo(series, SeriesToCategory.SERIES).sort(SeriesToCategory.ID), new SeriesToCategoryComponentFactory(seriesStringifier.toString(series, localRepository),
                                                                 "envelopeCategoryToggle",
                                                                 BudgetArea.EXPENSES_ENVELOPE,
                                                                 invisibleToggle)
      );
    }
  }

  private class SeriesToCategoryComponentFactory implements RepeatComponentFactory<Glob> {
    private String seriesName;
    private String name;
    private BudgetArea budgetArea;
    private ButtonGroup categoriesGroup = new ButtonGroup();
    private JToggleButton invisibleToggle;

    public SeriesToCategoryComponentFactory(String seriesName, String name, BudgetArea budgetArea, JToggleButton invisibleToggle) {
      this.seriesName = seriesName;
      this.name = name;
      this.budgetArea = budgetArea;
      this.invisibleToggle = invisibleToggle;
      categoriesGroup.add(invisibleToggle);
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob seriesToCategory) {
      Glob category = localRepository.findLinkTarget(seriesToCategory, SeriesToCategory.CATEGORY);
      String toggleLabel = categoryStringifier.toString(category, localRepository);
      final Key seriesKey = seriesToCategory.getTargetKey(SeriesToCategory.SERIES);
      final Key categoryKey = seriesToCategory.getTargetKey(SeriesToCategory.CATEGORY);

      final JToggleButton toggle = new JToggleButton(new AbstractAction(toggleLabel) {
        public void actionPerformed(ActionEvent e) {
          localRepository.setTarget(currentTransaction.getKey(), Transaction.SERIES, seriesKey);
          localRepository.setTarget(currentTransaction.getKey(), Transaction.CATEGORY, categoryKey);
        }
      });

      final CategoryUpdater updater =
        new CategoryUpdater(toggle, invisibleToggle, seriesKey, categoryKey, budgetArea, localRepository);     
      selectionService.addListener(updater, Transaction.TYPE);

      cellBuilder.add(this.name, toggle);
      categoriesGroup.add(toggle);
      toggle.setName(seriesName + ":" + category.get(Category.NAME));
      cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
        public void dispose() {
          selectionService.removeListener(updater);
          categoriesGroup.remove(toggle);
        }
      });
    }
  }

  private class NextTransactionAction extends AbstractAction implements GlobSelectionListener {

    private NextTransactionAction(SelectionService selectionService) {
      super("nextTransaction");
      selectionService.addListener(this, Transaction.TYPE);
    }

    public void actionPerformed(ActionEvent e) {
      if (transactionIndex < transactions.size() - 1) {
        transactionIndex++;
        CategorizationDialog.this.selectionService.select(transactions.get(transactionIndex));
      }
      setEnabled(transactionIndex < transactions.size() - 1);
    }

    public void selectionUpdated(GlobSelection selection) {
      setEnabled(transactionIndex < transactions.size() - 1);
    }
  }

  private class PreviousTransactionAction extends AbstractAction implements GlobSelectionListener {
    private PreviousTransactionAction(SelectionService selectionService) {
      super("previousTransaction");
      setEnabled(false);
      selectionService.addListener(this, Transaction.TYPE);
    }

    public void actionPerformed(ActionEvent e) {
      if (transactionIndex > 0) {
        transactionIndex--;
        selectionService.select(transactions.get(transactionIndex));
      }
      setEnabled(transactionIndex > 0);
    }

    public void selectionUpdated(GlobSelection selection) {
      setEnabled(transactionIndex >= 1);
    }
  }

  private class SeriesCreationAction extends AbstractAction {
    private final BudgetArea budgetArea;
    private final Directory localDirectory;

    public SeriesCreationAction(BudgetArea budgetArea, Directory localDirectory) {
      this.budgetArea = budgetArea;
      this.localDirectory = localDirectory;
    }

    public void actionPerformed(ActionEvent e) {
      SeriesCreationDialog creationDialog = new SeriesCreationDialog(budgetArea, dialog, localRepository, localDirectory);
      creationDialog.show();
    }
  }
}
