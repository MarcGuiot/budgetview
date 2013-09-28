package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.actions.CreateProjectAction;
import org.designup.picsou.gui.projects.components.ProjectButton;
import org.designup.picsou.gui.projects.components.ProjectPopupMenuFactory;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Project;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.gui.splits.utils.ToggleVisibilityAction;
import org.globsframework.gui.utils.GlobBooleanNodeStyleUpdater;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ProjectListView extends View {

  private ToggleVisibilityAction togglePastProjectsAction;

  protected ProjectListView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectListView.splits",
                                                      repository, directory);

    builder.add("createProject", new CreateProjectAction(directory));
    ProjectComponentFactory componentFactory = new ProjectComponentFactory();
    builder.addRepeat("currentProjects", ProjectStat.TYPE,
                      new CurrentProjectsMatcher(), GlobComparators.ascending(ProjectStat.FIRST_MONTH), componentFactory);
    builder.addRepeat("pastProjects", ProjectStat.TYPE,
                      new PastProjectsMatcher(), GlobComparators.descending(ProjectStat.LAST_MONTH), componentFactory);

    JPanel pastProjectsPanel = new JPanel();
    builder.add("pastProjectsPanel", pastProjectsPanel);
    togglePastProjectsAction = new ToggleVisibilityAction(pastProjectsPanel, Lang.get("hide"), Lang.get("show"));
    togglePastProjectsAction.setParentName("projectListView");
    builder.add("togglePastProjects", togglePastProjectsAction);

    parentBuilder.add("projectListView", builder.load());
  }

  public void reset() {
    togglePastProjectsAction.setHidden();
  }

  private class ProjectComponentFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder builder, final Glob projectStat) {

      final Key projectKey = Key.create(Project.TYPE, projectStat.get(ProjectStat.PROJECT));

      PopupMenuFactory menuFactory = createPopupFactory(projectKey, builder);

      ProjectButton button = new ProjectButton(projectKey, menuFactory, repository, directory);
      builder.add("projectButton", button);
      builder.addDisposeListener(button);

      GlobLabelView name = GlobLabelView.init(Project.NAME, repository, directory)
        .forceSelection(projectKey);
      SplitsNode<JLabel> nameNode = builder.add("name", name.getComponent());
      builder.addDisposeListener(name);

      final GlobBooleanNodeStyleUpdater nameUpdater =
        new GlobBooleanNodeStyleUpdater(Project.ACTIVE, nameNode,
                                        "activeProjectName", "inactiveProjectName",
                                        repository);
      builder.addDisposeListener(nameUpdater);

      builder.addOnLoadListener(new OnLoadListener() {
        public void processLoad() {
          nameUpdater.setKey(projectKey);
        }
      });
    }

    private PopupMenuFactory createPopupFactory(Key projectKey, RepeatCellBuilder builder) {
      ProjectPopupMenuFactory menuFactory = new ProjectPopupMenuFactory(projectKey, repository, directory);
      builder.addDisposeListener(menuFactory);
      return menuFactory;
    }
  }

  private class CurrentProjectsMatcher implements GlobMatcher {
    public boolean matches(Glob stat, GlobRepository repository) {
      Integer lastMonth = stat.get(ProjectStat.LAST_MONTH);
      return lastMonth == null || lastMonth >= CurrentMonth.getCurrentMonth(repository);
    }
  }

  private class PastProjectsMatcher implements GlobMatcher {
    public boolean matches(Glob stat, GlobRepository repository) {
      Integer lastMonth = stat.get(ProjectStat.LAST_MONTH);
      return lastMonth != null && lastMonth < CurrentMonth.getCurrentMonth(repository);
    }
  }
}