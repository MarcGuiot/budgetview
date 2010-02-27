package org.designup.picsou.gui.categorization.components;

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

public class BudgetAreaSelector implements GlobSelectionListener, ChangeSetListener {

  private BudgetArea[] budgetAreas =
    {BudgetArea.UNCATEGORIZED,
     BudgetArea.INCOME, BudgetArea.RECURRING, BudgetArea.ENVELOPES,
     BudgetArea.SAVINGS, BudgetArea.EXTRAS, BudgetArea.OTHER};

  private GlobRepository repository;
  private CardHandler budgetAreaCard;
  private CardHandler seriesCard;
  private JLabel title;

  private GlobStringifier budgetAreaStringifier;

  private Map<BudgetArea, JToggleButton> toggles = new HashMap<BudgetArea, JToggleButton>();
  private JToggleButton multiBudgetAreaToggle;

  private GlobList selectedTransactions = GlobList.EMPTY;
  private JEditorPane uncategorizedMessage;

  public BudgetAreaSelector(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.budgetAreaStringifier = directory.get(DescriptionService.class).getStringifier(BudgetArea.TYPE);

    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    budgetAreaCard = builder.addCardHandler("budgetAreaCard");
    seriesCard = builder.addCardHandler("seriesCard");

    title = builder.add("budgetAreaSelectorTitle", new JLabel()).getComponent();

    multiBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        seriesCard.show("multipleAreas");
      }
    });

    uncategorizedMessage = builder.add("uncategorizedMessage", new JEditorPane()).getComponent();

    builder.add("uncategorizeSelected", new UncategorizeTransactionsAction());

    builder.addRepeat("budgetAreaToggles",
                      Arrays.asList(budgetAreas),
                      new ToggleFactory());
  }

  private void showNoSelection() {
    budgetAreaCard.show("noSelection");
  }

  private void select(BudgetArea budgetArea, boolean activateToggle) {
    if (activateToggle) {
      toggles.get(budgetArea).doClick(0);
      return;
    }

    budgetAreaCard.show("series");

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
      for (Glob transaction : selectedTransactions) {
        if (changeSet.containsChanges(transaction.getKey(), Transaction.SERIES)) {
          updateSelection();
          return;
        }
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    this.selectedTransactions = GlobList.EMPTY;
    showNoSelection();
  }

  private void updateSelection() {
    if (selectedTransactions.isEmpty()) {
      select(BudgetArea.UNCATEGORIZED, true);
      showNoSelection();
      return;
    }

    SortedSet<Integer> areas = getSelectedTransactionAreas();
    if (areas.size() != 1) {
      budgetAreaCard.show("series");
      seriesCard.show("multipleAreas");
    }

    GlobList accounts = getSelectedTransacionAcounts();
    for (Glob account : accounts) {
      if (account.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
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
      budgetAreaCard.show("series");
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

  private GlobList getSelectedTransacionAcounts() {
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