package com.budgetview.desktop.series.utils;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.series.edition.DeleteSeriesAction;
import com.budgetview.desktop.series.edition.carryover.CarryOverAction;
import com.budgetview.desktop.seriesgroups.ClearSeriesGroupAction;
import com.budgetview.desktop.seriesgroups.SeriesGroupMenu;
import com.budgetview.model.AddOns;
import com.budgetview.model.ProjectItem;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.TypeChangeSetListener;
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
  private CarryOverAction carryOverAction;
  private final TypeChangeSetListener listener;

  public SeriesPopupFactory(Glob series,
                            GlobListFunctor editSeriesFunctor, GlobRepository repository,
                            Directory directory) {
    this.series = series;
    this.editSeriesFunctor = editSeriesFunctor;
    this.repository = repository;
    this.directory = directory;
    listener = new TypeChangeSetListener(AddOns.TYPE) {
      public void update(GlobRepository repository) {
        disposables.dispose();
        menu = null;
      }
    };
    repository.addChangeListener(listener);
  }

  public JPopupMenu createPopup() {
    if (menu == null) {
      menu = new JPopupMenu();

      final Glob projectItem = ProjectItem.findProjectItem(series, repository);

      if (projectItem != null) {
        menu.add(new AbstractAction(Lang.get("seriesGroup.goto.project")) {
          public void actionPerformed(ActionEvent actionEvent) {
            directory.get(NavigationService.class).gotoProjectItem(projectItem);
          }
        });
      }
      else {
        menu.add(new AbstractAction(Lang.get("series.edit")) {
          public void actionPerformed(ActionEvent actionEvent) {
            editSeriesFunctor.run(new GlobList(series), repository);
          }
        });
      }
      menu.addSeparator();
      menu.add(new AbstractAction(Lang.get("series.goto.operations")) {
        public void actionPerformed(ActionEvent actionEvent) {
          directory.get(NavigationService.class).gotoDataForSeries(series);
        }
      });
      if (AddOns.isEnabled(AddOns.ANALYSIS, repository)) {
        menu.add(new AbstractAction(Lang.get("series.goto.analysis")) {
          public void actionPerformed(ActionEvent actionEvent) {
            directory.get(NavigationService.class).gotoAnalysisForSeries(series);
          }
        });
      }

      seriesGroupMenu = new SeriesGroupMenu(series.getKey(), repository, directory);

      if (projectItem == null) {

        menu.addSeparator();

        carryOverAction = new CarryOverAction(series.getKey(), repository, directory);
        disposables.add(carryOverAction);
        menu.add(carryOverAction);

        menu.addSeparator();

        if (AddOns.isEnabled(AddOns.GROUPS, repository)) {
          menu.add(seriesGroupMenu.getMenu());

          ClearSeriesGroupAction clearSeriesGroupAction = new ClearSeriesGroupAction(series.getKey(), repository);
          disposables.add(clearSeriesGroupAction);
          menu.add(clearSeriesGroupAction);

          menu.addSeparator();
        }

        menu.add(new DeleteSeriesAction(series.getKey(), directory.get(JFrame.class), repository, directory));
      }
    }

    seriesGroupMenu.update();

    return menu;
  }

  public void dispose() {
    repository.removeChangeListener(listener);
    disposables.dispose();
    editSeriesFunctor = null;
    menu = null;
  }
}
