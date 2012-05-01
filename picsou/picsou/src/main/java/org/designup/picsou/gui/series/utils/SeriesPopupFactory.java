package org.designup.picsou.gui.series.utils;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.series.edition.carryover.CarryOverAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SeriesPopupFactory implements PopupMenuFactory, Disposable {

  private Glob series;
  private GlobListFunctor editSeriesFunctor;
  private GlobRepository repository;
  private Directory directory;
  private JPopupMenu menu;
  private CarryOverAction carryOverAction;

  public SeriesPopupFactory(Glob series,
                            GlobListFunctor editSeriesFunctor, GlobRepository repository,
                            Directory directory) {
    this.series = series;
    this.editSeriesFunctor = editSeriesFunctor;
    this.repository = repository;
    this.directory = directory;
    this.carryOverAction = new CarryOverAction(series.getKey(), repository, directory);
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
      menu.add(carryOverAction);
    }
    return menu;
  }

  public void dispose() {
    this.carryOverAction.dispose();
    this.carryOverAction = null;
    this.editSeriesFunctor = null;
  }
}
