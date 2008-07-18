package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.transactions.categorization.CategoryChooserCallback;
import org.designup.picsou.gui.transactions.categorization.CategoryChooserDialog;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

public class SeriesCreationDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private Glob series;
  private SelectionService selectionService;
  private BudgetArea budgetArea;

  public SeriesCreationDialog(Window parent, GlobRepository repository, Directory directory) {

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Series.TYPE, SeriesBudget.TYPE, Category.TYPE, BudgetArea.TYPE)
      .get();

    selectionService = new SelectionService();
    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesCreationDialog.class,
                                                      "/layout/seriesCreationDialog.splits",
                                                      localRepository, localDirectory);

    builder.addEditor("nameField", Series.LABEL);

    builder.add("assignCategoryAction", new AssignCategoryAction());

    builder.add("ok", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        localRepository.commitChanges(false);
        dialog.setVisible(false);
      }
    });

    builder.add("cancel", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });

    JPanel panel = builder.load();
    dialog = PicsouDialog.create(parent);
    dialog.setContentPane(panel);
  }

  public void show(Key seriesKey, Glob transaction) {
    this.series = localRepository.get(seriesKey);
    localRepository.rollback();
    Double amount = transaction.get(Transaction.AMOUNT);
    budgetArea = BudgetArea.get(this.series.get(Series.BUDGET_AREA));
    localRepository.update(seriesKey,
                           value(Series.AMOUNT, amount == null ? 0 : -amount),
                           value(Series.DAY, transaction.get(Transaction.DAY)),
                           value(Series.JANUARY, true),
                           value(Series.FEBRUARY, true),
                           value(Series.MARCH, true),
                           value(Series.APRIL, true),
                           value(Series.MAY, true),
                           value(Series.JUNE, true),
                           value(Series.JULY, true),
                           value(Series.AUGUST, true),
                           value(Series.SEPTEMBER, true),
                           value(Series.OCTOBER, true),
                           value(Series.NOVEMBER, true),
                           value(Series.DECEMBER, true));
    selectionService.select(localRepository.get(seriesKey));
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }


  public void show(Glob transaction, BudgetArea budget) {
    localRepository.rollback();
    budgetArea = BudgetArea.get(budget.getId());
    series = localRepository.create(Series.TYPE,
                                    value(Series.BUDGET_AREA, budgetArea.getId()),
                                    value(Series.AMOUNT, -transaction.get(Transaction.AMOUNT)),
                                    value(Series.LABEL, transaction.get(Transaction.LABEL)),
                                    value(Series.DAY, transaction.get(Transaction.DAY)),
                                    value(Series.JANUARY, true),
                                    value(Series.FEBRUARY, true),
                                    value(Series.MARCH, true),
                                    value(Series.APRIL, true),
                                    value(Series.MAY, true),
                                    value(Series.JUNE, true),
                                    value(Series.JULY, true),
                                    value(Series.AUGUST, true),
                                    value(Series.SEPTEMBER, true),
                                    value(Series.OCTOBER, true),
                                    value(Series.NOVEMBER, true),
                                    value(Series.DECEMBER, true));
    selectionService.select(series);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private class AssignCategoryAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      CategoryChooserDialog chooser =
        new CategoryChooserDialog(new SeriesCategoryChooserCallback(),
                                  new TransactionRendererColors(localDirectory),
                                  localRepository, localDirectory, dialog);

      chooser.show();
    }

    private class SeriesCategoryChooserCallback implements CategoryChooserCallback {
      public void categorySelected(Glob category) {
        localRepository.setTarget(series.getKey(), Series.DEFAULT_CATEGORY, category.getKey());
        if (budgetArea == BudgetArea.EXPENSES_ENVELOPE) {
          localRepository.delete(localRepository.getAll(SeriesToCategory.TYPE,
                                                        GlobMatchers.linkedTo(series, SeriesToCategory.SERIES)));
          localRepository.create(SeriesToCategory.TYPE,
                                 value(SeriesToCategory.SERIES, series.get(Series.ID)),
                                 value(SeriesToCategory.CATEGORY, category.get(Category.ID)));
        }
      }

      public Set<Integer> getPreselectedCategoryIds() {
        return Collections.emptySet();
      }
    }
  }
}
