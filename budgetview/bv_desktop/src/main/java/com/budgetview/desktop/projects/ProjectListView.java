package com.budgetview.desktop.projects;

import com.budgetview.desktop.View;
import com.budgetview.desktop.model.ProjectStat;
import com.budgetview.desktop.projects.actions.CreateProjectAction;
import com.budgetview.desktop.projects.components.ProjectButton;
import com.budgetview.desktop.projects.components.ProjectPopupMenuFactory;
import com.budgetview.desktop.projects.utils.CurrentProjectsMatcher;
import com.budgetview.desktop.projects.utils.PastProjectsMatcher;
import com.budgetview.model.Project;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.ToggleVisibilityAction;
import org.globsframework.gui.utils.GlobBooleanNodeStyleUpdater;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class ProjectListView extends View {

  private ToggleVisibilityAction togglePastProjectsAction;

  protected ProjectListView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectListView.splits",
                                                      repository, directory);

    builder.add("createProject", new CreateProjectAction(repository, directory));
    ProjectComponentFactory componentFactory = new ProjectComponentFactory();
    builder.addRepeat("currentProjects", ProjectStat.TYPE,
                      new CurrentProjectsMatcher(),
                      new GlobFieldsComparator(ProjectStat.FIRST_MONTH, true,
                                               ProjectStat.PLANNED_AMOUNT, true),
                      componentFactory);
    builder.addRepeat("pastProjects", ProjectStat.TYPE,
                      new PastProjectsMatcher(),
                      new GlobFieldsComparator(ProjectStat.LAST_MONTH, false,
                                               ProjectStat.PLANNED_AMOUNT, true),
                      componentFactory);

    JPanel pastProjectsPanel = new JPanel();
    builder.add("pastProjectsPanel", pastProjectsPanel);
    togglePastProjectsAction = new ToggleVisibilityAction(pastProjectsPanel, Lang.get("hide"), Lang.get("show"));
    togglePastProjectsAction.setParentName("projectListView");
    builder.add("togglePastProjects", togglePastProjectsAction);

    parentBuilder.add("projectListView", builder.<Component>load());
  }

  public void reset() {
    togglePastProjectsAction.setHidden();
  }

  private class ProjectComponentFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(PanelBuilder builder, final Glob projectStat) {

      final Key projectKey = Key.create(Project.TYPE, projectStat.get(ProjectStat.PROJECT));

      PopupMenuFactory menuFactory = createPopupFactory(projectKey, builder);

      ProjectButton button = new ProjectButton(projectKey, menuFactory, repository, directory);
      builder.add("projectButton", button);
      builder.addDisposable(button);

      GlobLabelView name = GlobLabelView.init(Project.NAME, repository, directory)
        .forceSelection(projectKey);
      SplitsNode<JLabel> nameNode = builder.add("name", name.getComponent());
      builder.addDisposable(name);

      final GlobBooleanNodeStyleUpdater nameUpdater =
        new GlobBooleanNodeStyleUpdater(Project.ACTIVE, nameNode,
                                        "activeProjectName", "inactiveProjectName",
                                        repository);
      nameUpdater.setKeyOnLoad(projectKey, builder);
      builder.addDisposable(nameUpdater);
    }

    private PopupMenuFactory createPopupFactory(Key projectKey, PanelBuilder builder) {
      ProjectPopupMenuFactory menuFactory = new ProjectPopupMenuFactory(projectKey, repository, directory);
      builder.addDisposable(menuFactory);
      return menuFactory;
    }
  }
}
