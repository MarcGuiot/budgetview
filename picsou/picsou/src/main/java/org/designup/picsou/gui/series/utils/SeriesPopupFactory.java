package org.designup.picsou.gui.series.utils;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.series.edition.DeleteSeriesAction;
import org.designup.picsou.gui.series.edition.carryover.CarryOverAction;
import org.designup.picsou.gui.seriesgroups.SeriesGroupMenu;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SeriesPopupFactory implements DisposablePopupMenuFactory {

  private Glob series;
  private GlobListFunctor editSeriesFunctor;
  private GlobRepository repository;
  private Directory directory;
  private JPopupMenu menu;
  private SeriesGroupMenu seriesGroupMenu;
  private DisposableGroup disposables = new DisposableGroup();

  public SeriesPopupFactory(Glob series,
                            GlobListFunctor editSeriesFunctor, GlobRepository repository,
                            Directory directory) {
    this.series = series;
    this.editSeriesFunctor = editSeriesFunctor;
    this.repository = repository;
    this.directory = directory;
  }

  public JPopupMenu createPopup() {
    if (menu == null) {
      menu = new JPopupMenu();
      menu.add(new AbstractAction(Lang.get("series.edit")) {
        public void actionPerformed(ActionEvent actionEvent) {
          editSeriesFunctor.run(new GlobList(series), repository);
        }
      });
      menu.addSeparator();
      menu.add(new AbstractAction(Lang.get("series.goto.operations")) {
        public void actionPerformed(ActionEvent actionEvent) {
          directory.get(NavigationService.class).gotoDataForSeries(series);
        }
      });
      menu.add(new AbstractAction(Lang.get("series.goto.analysis")) {
        public void actionPerformed(ActionEvent actionEvent) {
          directory.get(NavigationService.class).gotoAnalysisForSeries(series);
        }
      });
      menu.addSeparator();

      CarryOverAction carryOverAction = new CarryOverAction(series.getKey(), repository, directory);
      disposables.add(carryOverAction);
      menu.add(carryOverAction);

      menu.addSeparator();

      seriesGroupMenu = new SeriesGroupMenu(series.getKey(), repository, directory);
      menu.add(seriesGroupMenu.getMenu());

      menu.add(new DeleteSeriesAction(series.getKey(), directory.get(JFrame.class), repository, directory));
    }
    seriesGroupMenu.update();
    return menu;
  }

  public void dispose() {
    disposables.dispose();
    editSeriesFunctor = null;
  }
}
