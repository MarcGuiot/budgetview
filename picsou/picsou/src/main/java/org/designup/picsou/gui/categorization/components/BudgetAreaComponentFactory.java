package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BudgetAreaComponentFactory extends AbstractSeriesComponentFactory {
  private final CardHandler cardHandler;

  public BudgetAreaComponentFactory(CardHandler cardHandler,
                                    JToggleButton invisibleToggle,
                                    GlobRepository repository,
                                    Directory directory, PicsouDialog dialog) {
    super(invisibleToggle, repository, directory, dialog);
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

    final BudgetAreaToggleUpdater updater =
      new BudgetAreaToggleUpdater(toggle, invisibleToggle, budgetArea, repository, selectionService);
    cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
      public void dispose() {
        buttonGroup.remove(toggle);
        updater.dispose();
      }
    });
  }
}
