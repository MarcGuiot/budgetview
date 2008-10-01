package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
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
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class BudgetAreaSelector implements GlobSelectionListener, ChangeSetListener {

  private BudgetArea[] budgetAreas =
    {BudgetArea.UNCATEGORIZED,
     BudgetArea.INCOME, BudgetArea.RECURRING_EXPENSES, BudgetArea.EXPENSES_ENVELOPE,
     BudgetArea.OCCASIONAL_EXPENSES, BudgetArea.SAVINGS, BudgetArea.PROJECTS};

  private GlobRepository repository;
  private CardHandler budgetAreaCard;
  private CardHandler seriesCard;
  private JLabel title;

  private GlobStringifier budgetAreaStringifier;

  private Map<BudgetArea, JToggleButton> toggles = new HashMap<BudgetArea, JToggleButton>();
  private JToggleButton multiBudgetAreaToggle;

  private GlobList selectedTransactions = GlobList.EMPTY;

  public BudgetAreaSelector(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.budgetAreaStringifier = directory.get(DescriptionService.class).getStringifier(BudgetArea.TYPE);

    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    budgetAreaCard = builder.addCardHandler("budgetAreaCard");
    seriesCard = builder.addCardHandler("seriesCard");

    title = builder.add("budgetAreaSelectorTitle", new JLabel());

    multiBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        seriesCard.show("multipleAreas");
      }
    });

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
    }
    else {
      budgetAreaCard.show("series");
      seriesCard.show(budgetArea.getName());
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
          if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
            uncategorizeSelectedTransactions();
          }
        }
      });
      toggle.setName(budgetArea.getName());
      toggle.setToolTipText("<html>" + Lang.get("budgetArea.description." + budgetArea.getName()) + "</html>");
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

    private void uncategorizeSelectedTransactions() {
      try {
        repository.enterBulkDispatchingMode();
        for (Glob transaction : selectedTransactions) {
          repository.update(transaction.getKey(),
                            value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                            value(Transaction.CATEGORY, MasterCategory.NONE.getId()));
        }
      }
      finally {
        repository.completeBulkDispatchingMode();
      }
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
  }

  private void updateSelection() {
    if (selectedTransactions.isEmpty()) {
      showNoSelection();
      return;
    }

    GlobList series = GlobUtils.getTargets(selectedTransactions, Transaction.SERIES, repository);
    SortedSet<Integer> areas = series.getSortedSet(Series.BUDGET_AREA);
    if (areas.size() != 1) {
      multiBudgetAreaToggle.doClick(0);
      return;
    }

    final Integer selectedAreaId = areas.first();
    select(BudgetArea.get(selectedAreaId), true);
  }
}