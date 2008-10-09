package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.AutomaticSeriesBudgetTrigger;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class OccasionalSeriesEditionDialog {
  private LocalGlobRepository localRepository;
  private SelectionService selectionService;
  private DefaultDirectory localDirectory;
  private GlobRepository repository;
  private PicsouDialog dialog;

  public OccasionalSeriesEditionDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Category.TYPE, BudgetArea.TYPE, Month.TYPE, CurrentMonth.TYPE, ProfileType.TYPE)
      .get();

    localRepository.addTrigger(new AutomaticSeriesBudgetTrigger());
    localRepository.addTrigger(new SeriesBudgetTrigger());

    selectionService = new SelectionService();
    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);

    dialog = PicsouDialog.create(parent, localDirectory);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/occasionalSeriesEditionDialog.splits",
                                                      localRepository, localDirectory);

    SeriesBudgetEditionPanel budgetEditionPanel =
      new SeriesBudgetEditionPanel(dialog, repository, localRepository, localDirectory);
    builder.add("serieBudgetEditionPanel", budgetEditionPanel.getPanel());

    dialog.addPanelWithButtons(builder.<JPanel>load(), new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        localRepository.commitChanges(false);
        dialog.setVisible(false);
      }
    }, new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        localRepository.rollback();
        dialog.setVisible(false);
      }
    });
  }

  public void show() {
    GlobList globList = repository.getAll(SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID));
    Glob series = repository.get(Series.OCCASIONAL_SERIES);
    globList.add(series);
    localRepository.rollback();
    globList.addAll(repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
      .getGlobs().filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, false), repository));
    localRepository.reset(globList, Series.TYPE, SeriesBudget.TYPE, Transaction.TYPE);
    localDirectory.get(SelectionService.class).select(series);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

}
