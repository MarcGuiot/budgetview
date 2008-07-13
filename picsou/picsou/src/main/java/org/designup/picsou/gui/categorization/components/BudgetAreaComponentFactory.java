package org.designup.picsou.gui.categorization.components;

import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Transaction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BudgetAreaComponentFactory extends AbstractSeriesComponentFactory {
  private final CardHandler cardHandler;

  public BudgetAreaComponentFactory(CardHandler cardHandler,
                                    JToggleButton invisibleToggle,
                                    GlobRepository repository,
                                    Directory directory) {
    super(invisibleToggle, repository, directory);
    this.cardHandler = cardHandler;
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob budgetArea) {
    String name = budgetAreaStringifier.toString(budgetArea, repository);
    final JToggleButton toggle = new JToggleButton(new AbstractAction(name) {
      public void actionPerformed(ActionEvent e) {
        cardHandler.show(budgetArea.get(BudgetArea.NAME));
      }
    });
    toggle.setName(budgetArea.get(BudgetArea.NAME));
    cellBuilder.add("budgetAreaToggle", toggle);
    buttonGroup.add(toggle);

    final BudgetAreaToggleUpdater listener = new BudgetAreaToggleUpdater(toggle, budgetArea, repository);
    repository.addChangeListener(listener);
    selectionService.addListener(listener, Transaction.TYPE);
    cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
      public void dispose() {
        buttonGroup.remove(toggle);
        selectionService.removeListener(listener);
        repository.removeChangeListener(listener);
      }
    });
  }
}
