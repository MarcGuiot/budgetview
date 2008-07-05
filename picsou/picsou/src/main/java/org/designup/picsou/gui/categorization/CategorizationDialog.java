package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.DisposeCallback;
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
import java.util.HashSet;
import java.util.Set;

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

  public CategorizationDialog(Window parent, final GlobRepository repository, Directory directory) {

    Directory localDirectory = init(repository, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationDialog.splits",
                                                      localRepository, localDirectory);

    builder.addMultiLineTextView("transactionLabel", Transaction.TYPE);

    builder.add("nextTransaction", new NextTransactionAction(selectionService));

    builder.add("previousTransaction", new PreviousTransactionAction(selectionService));

    final CardHandler cardHandler = builder.addCardHandler("cards");
    builder.addRepeat("budgetAreas", BudgetArea.TYPE.getConstants(),
                      new BudgetAreaComponentFactory(cardHandler));

    JToggleButton invisibleRecurringToggle = new JToggleButton();
    builder.add("invisibleRecurringToggle", invisibleRecurringToggle);
    builder.addRepeat("recurringSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.RECURRING_EXPENSES.getGlob(), Series.BUDGET_AREA),
                      new GlobFieldComparator(Series.ID),
                      new RecurringSeriesComponentFactory(invisibleRecurringToggle));

    builder.addRepeat("envelopeSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.EXPENSES_ENVELOPE.getGlob(), Series.BUDGET_AREA),
                      new GlobFieldComparator(Series.ID),
                      new EnvelopeSeriesComponentFactory());

    GlobList occasionalSeries = repository.findLinkedTo(BudgetArea.OCCASIONAL_EXPENSES.getGlob(), Series.BUDGET_AREA);
    Set<Glob> categories = new HashSet<Glob>();
    for (Glob oneOccasionalSeries : occasionalSeries) {
      categories.addAll(repository.findLinkedTo(oneOccasionalSeries, SeriesToCategory.SERIES));
    }

    JToggleButton invisibleOccasionalSeries = new JToggleButton();
    builder.add("invibleOccasionalToggle", invisibleOccasionalSeries);
    builder.addRepeat("occasionalSeriesRepeat",
                      new GlobList(categories).sort(SeriesToCategory.ID),
                      new SeriesToCategoryComponentFactory("occasionalSeries", "occasionalCategoryToggle", BudgetArea.OCCASIONAL_EXPENSES,
                                                           invisibleOccasionalSeries));

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
        localRepository.rollback();
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
        }
      });
    }
  }

  private class RecurringSeriesComponentFactory implements RepeatComponentFactory<Glob> {
    ButtonGroup seriesGroup = new ButtonGroup();
    private JToggleButton invisibleToggle;

    public RecurringSeriesComponentFactory(JToggleButton invisibleToggle) {
      this.invisibleToggle = invisibleToggle;
      seriesGroup.add(invisibleToggle);
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
      String name = seriesStringifier.toString(series, localRepository);
      final JToggleButton toggle = new JToggleButton(new AbstractAction(name) {
        public void actionPerformed(ActionEvent e) {
          localRepository.setTarget(currentTransaction.getKey(), Transaction.SERIES, series.getKey());
          localRepository.setTarget(currentTransaction.getKey(), Transaction.CATEGORY,
                                    series.getTargetKey(Series.DEFAULT_CATEGORY));
        }
      });
      final GlobSelectionListener listener = new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          GlobList transactions = selection.getAll(Transaction.TYPE);
          if (transactions.size() != 1) {
            return;
          }
          Glob transaction = transactions.get(0);
          Integer seriesId = transaction.get(Transaction.SERIES);
          if (seriesId != null) {
            toggle.setSelected(seriesId.equals(series.get(Series.ID)));
          }
          else {
            invisibleToggle.setSelected(true);
          }
        }
      };
      seriesGroup.add(toggle);
      selectionService.addListener(listener, Transaction.TYPE);
      cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
        public void dispose() {
          selectionService.removeListener(listener);
          seriesGroup.remove(toggle);
        }
      });
      cellBuilder.add("recurringSeriesToggle", toggle);
    }
  }

  private class EnvelopeSeriesComponentFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
      cellBuilder.add("envelopeSeriesName",
                      new JLabel(seriesStringifier.toString(series, localRepository)));

      JToggleButton invisibleCategory = new JToggleButton();
      cellBuilder.add("invisibleCategory", invisibleCategory);
      cellBuilder.addRepeat("envelopeCategoryRepeat",
                            new SeriesToCategoryComponentFactory(seriesStringifier.toString(series, localRepository),
                                                                 "envelopeCategoryToggle", BudgetArea.EXPENSES_ENVELOPE, invisibleCategory),
                            localRepository.findLinkedTo(series, SeriesToCategory.SERIES).sort(SeriesToCategory.ID));
    }
  }

  private class SeriesToCategoryComponentFactory implements RepeatComponentFactory<Glob> {
    private String seriesName;
    private String name;
    private BudgetArea budgetArea;
    private ButtonGroup categoriesGroup = new ButtonGroup();
    private JToggleButton invisibleButton;

    public SeriesToCategoryComponentFactory(String seriesName, String name, BudgetArea budgetArea, JToggleButton invisibleButton) {
      this.seriesName = seriesName;
      this.name = name;
      this.budgetArea = budgetArea;
      this.invisibleButton = invisibleButton;
      categoriesGroup.add(invisibleButton);
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob seriesToCategory) {
      Glob category = localRepository.findLinkTarget(seriesToCategory, SeriesToCategory.CATEGORY);
      String name = categoryStringifier.toString(category, localRepository);
      final JToggleButton toggle = new JToggleButton(new AbstractAction(name) {
        public void actionPerformed(ActionEvent e) {
          localRepository.setTarget(currentTransaction.getKey(), Transaction.SERIES,
                                    seriesToCategory.getTargetKey(SeriesToCategory.SERIES));
          localRepository.setTarget(currentTransaction.getKey(), Transaction.CATEGORY,
                                    seriesToCategory.getTargetKey(SeriesToCategory.CATEGORY));
        }
      });
      final CategoryUpdater updater = new CategoryUpdater(toggle, invisibleButton, seriesToCategory, budgetArea, localRepository);
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
}
