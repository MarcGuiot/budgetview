package com.budgetview.gui.series.utils;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.gui.seriesgroups.DeleteSeriesGroupAction;
import com.budgetview.gui.seriesgroups.RenameSeriesGroupAction;
import com.budgetview.model.AddOns;
import com.budgetview.model.Project;
import com.budgetview.model.SeriesGroup;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SeriesGroupPopupFactory implements DisposablePopupMenuFactory {

  private Glob seriesGroup;
  private GlobRepository repository;
  private Directory directory;
  private JPopupMenu menu;
  private DisposableGroup disposables = new DisposableGroup();
  private final TypeChangeSetListener listener;

  public SeriesGroupPopupFactory(Glob seriesGroup,
                                 GlobRepository repository,
                                 Directory directory) {
    this.seriesGroup = seriesGroup;
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
      if (AddOns.isEnabled(AddOns.ANALYSIS, repository)) {
        menu.add(new AbstractAction(Lang.get("series.goto.analysis")) {
          public void actionPerformed(ActionEvent actionEvent) {
            directory.get(NavigationService.class).gotoAnalysisForSeries(seriesGroup);
          }
        });
      }
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
    repository.removeChangeListener(listener);
    disposables.dispose();
    menu = null;
  }
}
