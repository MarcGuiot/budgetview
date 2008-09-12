package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.SortedSet;

public class BudgetAreaSelector {

  private GlobList budgetAreas;
  private GlobRepository repository;
  private Directory directory;
  private CardHandler cardHandler;
  private JToggleButton invisibleBudgetAreaToggle;
  private GlobStringifier budgetAreaStringifier;

  public BudgetAreaSelector(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    budgetAreas = BudgetArea.getGlobs(this.repository,
                                      BudgetArea.INCOME, BudgetArea.SAVINGS,
                                      BudgetArea.RECURRING_EXPENSES, BudgetArea.EXPENSES_ENVELOPE,
                                      BudgetArea.PROJECTS, BudgetArea.OCCASIONAL_EXPENSES);

    budgetAreaStringifier = directory.get(DescriptionService.class).getStringifier(BudgetArea.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    cardHandler = builder.addCardHandler("cards");

    invisibleBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cardHandler.show("noBudgetArea");
      }
    });

    builder.add("invisibleBudgetAreaToggle", invisibleBudgetAreaToggle);
    builder.addRepeat("budgetAreas",
                      budgetAreas,
                      new BudgetAreaComponentFactory());
  }

  private void show(Glob budgetArea) {
    cardHandler.show(budgetArea.get(BudgetArea.NAME));
  }

  public class BudgetAreaComponentFactory  implements RepeatComponentFactory<Glob> {

    private ButtonGroup buttonGroup = new ButtonGroup();

    public BudgetAreaComponentFactory() {
      buttonGroup.add(invisibleBudgetAreaToggle);
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob budgetArea) {
      String name = budgetAreaStringifier.toString(budgetArea, repository);
      final JToggleButton toggle = new JToggleButton(new AbstractAction(name) {
        public void actionPerformed(ActionEvent e) {
          show(budgetArea);
        }
      });
      toggle.setName(budgetArea.get(BudgetArea.NAME));
      cellBuilder.add("budgetAreaToggle", toggle);
      buttonGroup.add(toggle);

      final BudgetAreaToggleUpdater updater =
        new BudgetAreaToggleUpdater(toggle, invisibleBudgetAreaToggle, budgetArea,
                                    repository, directory.get(SelectionService.class));
      cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
        public void dispose() {
          buttonGroup.remove(toggle);
          updater.dispose();
        }
      });
    }
  }

  public class BudgetAreaToggleUpdater implements GlobSelectionListener, ChangeSetListener {
    private final JToggleButton toggle;
    private final BudgetArea budgetArea;
    private GlobRepository repository;
    private SelectionService selectionService;
    private GlobList selectedTransactions = GlobList.EMPTY;
    private JToggleButton invisibleToggle;

    public BudgetAreaToggleUpdater(final JToggleButton toggle, JToggleButton invisibleToggle,
                                   final Glob budgetAreaGlob,
                                   GlobRepository repository, SelectionService selectionService) {
      this.toggle = toggle;
      this.invisibleToggle = invisibleToggle;
      this.budgetArea = BudgetArea.get(budgetAreaGlob.get(BudgetArea.ID));
      this.repository = repository;
      repository.addChangeListener(this);
      this.selectionService = selectionService;
      selectionService.addListener(this, Transaction.TYPE);
    }

    public void selectionUpdated(GlobSelection selection) {
      this.selectedTransactions = selection.getAll(Transaction.TYPE);
      updateState();
    }

    private void updateState() {
      if (selectedTransactions.isEmpty()) {
        invisibleToggle.doClick(0);
        toggle.setEnabled(false);
        return;
      }
      toggle.setEnabled(true);

      GlobList series = GlobUtils.getTargets(selectedTransactions, Transaction.SERIES, repository);
      SortedSet<Integer> areas = series.getSortedSet(Series.BUDGET_AREA);
      if (areas.size() != 1 || BudgetArea.UNCATEGORIZED.getId().equals(areas.first())) {
        invisibleToggle.doClick(0);
        return;
      }

      final Integer selectedAreaId = areas.first();
      if (budgetArea.getId().equals(selectedAreaId)) {
        toggle.doClick(0);
      }
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(Transaction.TYPE)) {
        updateState();
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      this.selectedTransactions = GlobList.EMPTY;
    }

    public void dispose() {
      repository.removeChangeListener(this);
      selectionService.removeListener(this);
    }
  }

}