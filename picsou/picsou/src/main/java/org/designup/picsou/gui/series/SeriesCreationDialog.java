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
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class SeriesCreationDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private Glob series;
  private SelectionService selectionService;

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

  public void show(Key series, Glob transaction) {
    this.series = localRepository.get(series);
    localRepository.rollback();
    Double amount = transaction.get(Transaction.AMOUNT);
    localRepository.update(series,
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
    selectionService.select(localRepository.get(series));
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }


  public void show(Glob transaction, BudgetArea budget) {
    localRepository.rollback();
    series = localRepository.create(Series.TYPE,
                                    value(Series.BUDGET_AREA, budget.getId()),
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
      }

      public Set<Integer> getPreselectedCategoryIds() {
        return new HashSet<Integer>();
      }
    }
  }
}
