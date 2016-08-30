package com.budgetview.desktop.categorization.components;

import com.budgetview.desktop.categorization.reconciliation.ReconciliationNavigationPanel;
import com.budgetview.desktop.categorization.reconciliation.ReconciliationPanel;
import com.budgetview.desktop.description.Labels;
import com.budgetview.desktop.transactions.columns.TransactionRendererColors;
import com.budgetview.model.*;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.util.*;

public class CategorizationHandler implements GlobSelectionListener, ChangeSetListener {

  private BudgetArea[] budgetAreas =
    {BudgetArea.UNCATEGORIZED,
      BudgetArea.INCOME, BudgetArea.RECURRING, BudgetArea.VARIABLE,
      BudgetArea.TRANSFER, BudgetArea.EXTRAS, BudgetArea.OTHER};

  private GlobRepository repository;
  private Directory directory;
  private GlobMatcher toReconcileMatcher;
  private TransactionRendererColors colors;
  private CategorizationCard categorizationCard;
  private CardHandler seriesCard;
  private JLabel title;

  private GlobStringifier budgetAreaStringifier;

  private Map<BudgetArea, JToggleButton> toggles = new HashMap<BudgetArea, JToggleButton>();
  private JToggleButton multiBudgetAreaToggle;

  private GlobList selectedTransactions = GlobList.EMPTY;
  private JEditorPane uncategorizedMessage;
  private ReconciliationPanel reconciliationPanel;
  private ReconciliationNavigationPanel reconciliationNavigation;
  private SplitsNode<JLabel> downArrow;
  private CategorizationTableView categorizationTableView;

  public CategorizationHandler(CategorizationTableView categorizationTableView, GlobRepository repository, Directory directory) {
    this.categorizationTableView = categorizationTableView;
    categorizationTableView.addTableListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        updateSelection();
      }
    });

    this.toReconcileMatcher = categorizationTableView.getToReconcileMatcher();
    this.colors = categorizationTableView.getColors();
    this.repository = repository;
    this.directory = directory;
    this.budgetAreaStringifier = directory.get(DescriptionService.class).getStringifier(BudgetArea.TYPE);

    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    categorizationCard = new CategorizationCard(builder.addCardHandler("categorizationCard"));
    seriesCard = builder.addCardHandler("seriesCard");

    title = builder.add("verticalTabSelectorTitle", new JLabel()).getComponent();

    multiBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        seriesCard.show("multipleAreas");
      }
    });

    uncategorizedMessage = builder.add("uncategorizedMessage", new JEditorPane()).getComponent();

    builder.add("uncategorizeSelected", new UncategorizeTransactionsAction());

    reconciliationPanel = new ReconciliationPanel(colors, repository, directory);
    builder.add("reconciliationPanel", reconciliationPanel.getPanel());

    reconciliationNavigation = new ReconciliationNavigationPanel(toReconcileMatcher, repository, directory) {
      protected void showCategorization() {
        categorizationCard.showCategorization();
      }

      protected void showReconciliation() {
        categorizationCard.showReconciliation();
      }
    };
    builder.add("reconciliationNavigationPanel", reconciliationNavigation.getPanel());

    builder.addRepeat("budgetAreaToggles",
                      Arrays.asList(budgetAreas),
                      new ToggleFactory());

    downArrow = builder.add("downArrow", new JLabel());
  }

  private void showNoSelection() {
    if (!repository.contains(Transaction.TYPE)) {
      categorizationCard.showNoDataImported();
    }
    else if (categorizationTableView.getDisplayedGlobs().isEmpty()) {
      categorizationCard.showNoDataShown();
    }
    else {
      categorizationCard.showNoSelection();
    }
  }

  private void select(BudgetArea budgetArea, boolean activateToggle) {
    if (activateToggle) {
      toggles.get(budgetArea).doClick(0);
      return;
    }

    categorizationCard.showCategorization();

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

    public void registerComponents(PanelBuilder cellBuilder, final BudgetArea budgetArea) {
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
      toggle.setToolTipText(Strings.toSplittedHtml(Labels.getDescription(budgetArea), 50));
      cellBuilder.add("budgetAreaToggle", toggle);
      buttonGroup.add(toggle);
      toggles.put(budgetArea, toggle);

      cellBuilder.addDisposable(new Disposable() {
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
    if (changeSet.containsUpdates(Account.ACCOUNT_TYPE)) {
      updateSelection();
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

    Glob first = selectedTransactions.getFirst();
    if (!first.exists()) {
      selectedTransactions.clear();
      select(BudgetArea.UNCATEGORIZED, true);
      showNoSelection();
      return;
    }

    if (selectedTransactions.size() == 1) {
      reconciliationPanel.update(first);
      if (toReconcileMatcher.matches(first, repository) && Transaction.isCategorized(first)) {
        categorizationCard.showReconciliation();
        return;
      }
    }

    SortedSet<Integer> areas = getSelectedTransactionAreas();
    if (areas.size() != 1) {
      categorizationCard.showCategorization();
      seriesCard.show("multipleAreas");
    }

    if (areas.size() != 1) {
      return;
    }

    final Integer selectedAreaId = areas.first();
    BudgetArea budgetArea = BudgetArea.get(selectedAreaId);
    if (budgetArea != BudgetArea.UNCATEGORIZED) {
      select(budgetArea, true);
    }
    else {
      categorizationCard.showCategorization();
      JToggleButton uncategorizedButton = this.toggles.get(BudgetArea.UNCATEGORIZED);
      for (JToggleButton button : toggles.values()) {
        if (button.isSelected()) {
          if (button == uncategorizedButton) {
            updateUncategorizedMessage(budgetArea);
          }
          return;
        }
      }
      select(BudgetArea.UNCATEGORIZED, true);
    }
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
      Transaction.uncategorize(selectedTransactions, repository);
    }
  }

  private class CategorizationCard {
    private CardHandler card;

    public CategorizationCard(CardHandler card) {
      this.card = card;
    }

    public void showCategorization() {
      card.show("series");
      downArrow.applyStyle("downArrowShown");
      reconciliationNavigation.categorizationShown();
    }

    public void showReconciliation() {
      card.show("reconciliation");
      downArrow.applyStyle("downArrowShown");
      reconciliationNavigation.reconciliationShown();
    }

    public void showNoDataShown() {
      card.show("noDataShown");
      downArrow.applyStyle("downArrowHidden");
      reconciliationNavigation.noSelectionShown();
    }

    public void showNoDataImported() {
      card.show("noDataImported");
      downArrow.applyStyle("downArrowHidden");
      reconciliationNavigation.noSelectionShown();
    }

    public void showNoSelection() {
      card.show("noSelection");
      downArrow.applyStyle("downArrowHidden");
      reconciliationNavigation.noSelectionShown();
    }
  }
}