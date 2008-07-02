package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
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

public class CategorizationDialog {
  private JPanel panel;
  private SelectionService selectionService = new SelectionService();
  private LocalGlobRepository localRepository;
  private Glob currentTransaction;
  private GlobStringifier budgetAreaStringifier;
  private GlobStringifier seriesStringifier;
  private PicsouDialog dialog;

  public CategorizationDialog(Window parent, final GlobRepository repository, Directory directory) {

    Directory localDirectory = init(repository, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationDialog.splits",
                                                      localRepository, localDirectory);

    builder.addMultiLineTextView("transactionLabel", Transaction.TYPE);

    final CardHandler cardHandler = builder.addCardHandler("cards");
    final ButtonGroup budgetAreasGroup = new ButtonGroup();
    builder.addRepeat("budgetAreas", BudgetArea.TYPE.getConstants(), new RepeatComponentFactory<Glob>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, final Glob budgetArea) {
        String name = budgetAreaStringifier.toString(budgetArea, localRepository);
        JToggleButton toggleButton = new JToggleButton(new AbstractAction(name) {
          public void actionPerformed(ActionEvent e) {
            cardHandler.show(budgetArea.get(BudgetArea.NAME));
          }
        });
        cellBuilder.add("budgetAreaToggle", toggleButton);
        budgetAreasGroup.add(toggleButton);
      }
    });

    builder.addRepeat("recurringSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.RECURRING_EXPENSES.getGlob(), Series.BUDGET_AREA),
                      new GlobFieldComparator(Series.ID),
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
                          String name = seriesStringifier.toString(series, localRepository);
                          cellBuilder.add("recurringSeriesToggle", new JToggleButton(new AbstractAction(name) {
                            public void actionPerformed(ActionEvent e) {
                              localRepository.setTarget(currentTransaction.getKey(), Transaction.SERIES, series.getKey());
                            }
                          }));
                        }
                      });

    builder.add("ok", new AbstractAction("ok"){
      public void actionPerformed(ActionEvent e) {
        localRepository.commitChanges(false);
        dialog.setVisible(false);
      }
    });

    builder.add("cancel", new AbstractAction("cancel"){
      public void actionPerformed(ActionEvent e) {
        localRepository.rollback();
        dialog.setVisible(false);
      }
    });

    panel = builder.load();
    dialog = PicsouDialog.create(parent);
    dialog.setContentPane(panel);
  }

  private Directory init(GlobRepository repository, Directory directory) {

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(BudgetArea.TYPE, Category.TYPE, Series.TYPE)
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
    return localDirectory;
  }

  public void show(GlobList transactions) {
    if (transactions.isEmpty()) {
      return;
    }
    localRepository.rollback();
    localRepository.reset(transactions, Transaction.TYPE);
    selectionService.select(transactions.get(0));
    GuiUtils.showCentered(dialog);
  }
}
