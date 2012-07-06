package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.categorization.reconciliation.ReconciliationPanel;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class CategorizationSelector implements GlobSelectionListener, ChangeSetListener {

  private BudgetArea[] budgetAreas =
    {BudgetArea.UNCATEGORIZED,
     BudgetArea.INCOME, BudgetArea.RECURRING, BudgetArea.VARIABLE,
     BudgetArea.SAVINGS, BudgetArea.EXTRAS, BudgetArea.OTHER};

  private TransactionRendererColors colors;
  private GlobRepository repository;
  private Directory directory;
  private CardHandler categorizationCard;
  private CardHandler seriesCard;
  private JLabel title;

  private GlobStringifier budgetAreaStringifier;

  private Map<BudgetArea, JToggleButton> toggles = new HashMap<BudgetArea, JToggleButton>();
  private JToggleButton multiBudgetAreaToggle;

  private GlobList selectedTransactions = GlobList.EMPTY;
  private JEditorPane noSelectionMessage;
  private JEditorPane uncategorizedMessage;
  private ReconciliationPanel reconciliationPanel;

  public CategorizationSelector(TransactionRendererColors colors, GlobRepository repository, Directory directory) {
    this.colors = colors;
    this.repository = repository;
    this.directory = directory;
    this.budgetAreaStringifier = directory.get(DescriptionService.class).getStringifier(BudgetArea.TYPE);

    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    categorizationCard = builder.addCardHandler("categorizationCard");
    seriesCard = builder.addCardHandler("seriesCard");

    title = builder.add("budgetAreaSelectorTitle", new JLabel()).getComponent();

    multiBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        seriesCard.show("multipleAreas");
      }
    });

    noSelectionMessage = builder.add("noSelectionInfoMessage", new JEditorPane()).getComponent();
    uncategorizedMessage = builder.add("uncategorizedMessage", new JEditorPane()).getComponent();

    builder.add("uncategorizeSelected", new UncategorizeTransactionsAction());

    reconciliationPanel = new ReconciliationPanel(colors, repository, directory);
    builder.add("reconciliationPanel", reconciliationPanel.getPanel());
    
    builder.addRepeat("budgetAreaToggles",
                      Arrays.asList(budgetAreas),
                      new ToggleFactory());
  }

  private void showNoSelection() {
    categorizationCard.show("noSelection");
  }

  private void select(BudgetArea budgetArea, boolean activateToggle) {
    if (activateToggle) {
      toggles.get(budgetArea).doClick(0);
      return;
    }

    categorizationCard.show("series");

    if (BudgetArea.UNCATEGORIZED.equals(budgetArea)) {
      updateUncategorizedMessage(budgetArea);
    }
    else {
      seriesCard.show(budgetArea.getName());
    }
  }

  private void updateUncategorizedMessage(BudgetArea budgetArea) {
    SortedSet<Integer> areaIds = getSelectedTransactionAreas();
    if ((areaIds.size() == 1) && BudgetArea.UNCATEGORIZED.getId().equals(areaIds.iterator().next())) {
      if (selectedTransactions.size() == 1) {
        uncategorizedMessage.setText(Lang.get("categorization.uncategorized.single"));
      }
      else {
        uncategorizedMessage.setText(Lang.get("categorization.uncategorized.multiple"));
      }
      seriesCard.show(budgetArea.getName());
    }
    else {
      seriesCard.show("revertToUncategorized");
    }
  }

  public class ToggleFactory implements RepeatComponentFactory<BudgetArea> {

    private ButtonGroup buttonGroup = new ButtonGroup();

    public ToggleFactory() {
      buttonGroup.add(multiBudgetAreaToggle);
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final BudgetArea budgetArea) {
      String label = budgetAreaStringifier.toString(repository.get(budgetArea.getKey()), repository);
      final JToggleButton toggle = new JToggleButton(new AbstractAction(label) {
        public void actionPerformed(ActionEvent e) {
          if (!BudgetArea.UNCATEGORIZED.equals(budgetArea)) {
            SignpostStatus.setCompleted(SignpostStatus.CATEGORIZATION_AREA_SELECTION_DONE, repository);
          }
          select(budgetArea, false);
        }
      });
      toggle.setName(budgetArea.getName());
      toggle.setToolTipText(Strings.toSplittedHtml(budgetArea.getDescription(), 50));
      cellBuilder.add("budgetAreaToggle", toggle);
      buttonGroup.add(toggle);
      toggles.put(budgetArea, toggle);

      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          buttonGroup.remove(toggle);
          toggles.remove(budgetArea);
        }
      });
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    this.selectedTransactions = selection.getAll(Transaction.TYPE);
    title.setText(Lang.get(selectedTransactions.size() == 1 ?
                           "categorization.budgetAreaSelection.title.singular" :
                           "categorization.budgetAreaSelection.title.plural"));
    updateSelection();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      updateNoSelectionMessage(repository);
      for (Glob transaction : selectedTransactions) {
        if (changeSet.containsChanges(transaction.getKey(), Transaction.SERIES)) {
          updateSelection();
          return;
        }
      }
    }
    if (changeSet.containsUpdates(Account.ACCOUNT_TYPE)) {
      updateSelection();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    updateNoSelectionMessage(repository);
    this.selectedTransactions = GlobList.EMPTY;
    showNoSelection();
  }

  private void updateNoSelectionMessage(GlobRepository repository) {
    noSelectionMessage.setText(repository.contains(Transaction.TYPE) ? Lang.get("categorization.no.selection") : "");
  }

  private void updateSelection() {
    if (selectedTransactions.isEmpty()) {
      select(BudgetArea.UNCATEGORIZED, true);
      showNoSelection();
      return;
    }
    
    if (selectedTransactions.size() == 1) {
      Glob first = selectedTransactions.getFirst();
      if (ReconciliationStatus.isToReconcile(first)) {
        categorizationCard.show("reconciliation");
        reconciliationPanel.update(first);
        return;
      }
    }

    SortedSet<Integer> areas = getSelectedTransactionAreas();
    if (areas.size() != 1) {
      categorizationCard.show("series");
      seriesCard.show("multipleAreas");
    }

    GlobList accounts = getSelectedTransactionAccounts();
    for (Glob account : accounts) {
      if (AccountType.SAVINGS.getId().equals(account.get(Account.ACCOUNT_TYPE))) {
        enableValidBudgetAreas(false);
        if (areas.size() == 1) {
          select(BudgetArea.SAVINGS, true);
        }
        return;
      }
    }
    enableValidBudgetAreas(true);

    if (areas.size() != 1) {
      return;
    }

    final Integer selectedAreaId = areas.first();
    BudgetArea budgetArea = BudgetArea.get(selectedAreaId);
    if (budgetArea != BudgetArea.UNCATEGORIZED) {
      select(budgetArea, true);
    }
    else {
      categorizationCard.show("series");
      JToggleButton uncategorizedButton = this.toggles.get(BudgetArea.UNCATEGORIZED);
      for (JToggleButton button : toggles.values()) {
        if (button.isSelected()){
          if (button == uncategorizedButton){
            updateUncategorizedMessage(budgetArea);
          }
          return;
        }
      }
      select(BudgetArea.UNCATEGORIZED, true);
    }
  }

  private void enableValidBudgetAreas(boolean enableAll) {
    for (Map.Entry<BudgetArea, JToggleButton> entry : toggles.entrySet()) {
      entry.getValue().setEnabled(enableAll
                                  || entry.getKey() == BudgetArea.SAVINGS 
                                  || entry.getKey() == BudgetArea.UNCATEGORIZED);
    }
  }

  private GlobList getSelectedTransactionAccounts() {
    return GlobUtils.getUniqueTargets(selectedTransactions, Transaction.ACCOUNT, repository);
  }

  private SortedSet<Integer> getSelectedTransactionAreas() {
    GlobList series = GlobUtils.getUniqueTargets(selectedTransactions, Transaction.SERIES, repository);
    return series.getSortedSet(Series.BUDGET_AREA);
  }

  private class UncategorizeTransactionsAction extends AbstractAction {

    private UncategorizeTransactionsAction() {
      super(Lang.get("categorization.revert.uncategorized.button"));
    }

    public void actionPerformed(ActionEvent e) {
      try {
        repository.startChangeSet();
        for (Glob transaction : selectedTransactions) {
          repository.update(transaction.getKey(),
                            value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                            value(Transaction.SUB_SERIES, null));
        }
      }
      finally {
        repository.completeChangeSet();
      }
    }
  }

}