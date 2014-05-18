package org.designup.picsou.gui.series.utils;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.seriesgroups.DeleteSeriesGroupAction;
import org.designup.picsou.gui.seriesgroups.RenameSeriesGroupAction;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.SeriesGroup;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SeriesGroupPopupFactory implements DisposablePopupMenuFactory {

  private Glob seriesGroup;
  private GlobRepository repository;
  private Directory directory;
  private JPopupMenu menu;
  private DisposableGroup disposables = new DisposableGroup();

  public SeriesGroupPopupFactory(Glob seriesGroup,
                                 GlobRepository repository,
                                 Directory directory) {
    this.seriesGroup = seriesGroup;
    this.repository = repository;
    this.directory = directory;
  }

  public JPopupMenu createPopup() {
    if (menu == null) {
      menu = new JPopupMenu();
      menu.add(createExpandCollapseAction());
      final Glob project = Project.findProjectForGroup(seriesGroup, repository);
      if (project == null) {
        menu.add(new RenameSeriesGroupAction(seriesGroup.getKey(), repository, directory));
      }
      menu.addSeparator();
      if (project != null) {
        menu.add(new AbstractAction(Lang.get("seriesGroup.goto.project")) {
          public void actionPerformed(ActionEvent actionEvent) {
            directory.get(NavigationService.class).gotoProject(project);
          }
        });
      }
      menu.add(new AbstractAction(Lang.get("series.goto.operations")) {
        public void actionPerformed(ActionEvent actionEvent) {
          directory.get(NavigationService.class).gotoDataForSeriesGroup(seriesGroup);
        }
      });
      menu.add(new AbstractAction(Lang.get("series.goto.analysis")) {
        public void actionPerformed(ActionEvent actionEvent) {
          directory.get(NavigationService.class).gotoAnalysisForSeries(seriesGroup);
        }
      });
      if (project == null) {
        menu.addSeparator();
        menu.add(new DeleteSeriesGroupAction(seriesGroup.getKey(), repository));
      }
    }
    return menu;
  }

  private ToggleBooleanAction createExpandCollapseAction() {
    ToggleBooleanAction expandCollapseAction =
      new ToggleBooleanAction(seriesGroup.getKey(), SeriesGroup.EXPANDED,
                              Lang.get("seriesGroup.menu.collapse"),
                              Lang.get("seriesGroup.menu.expand"),
                              repository);
    disposables.add(expandCollapseAction);
    return expandCollapseAction;
  }

  public void dispose() {
    disposables.dispose();
  }
}
