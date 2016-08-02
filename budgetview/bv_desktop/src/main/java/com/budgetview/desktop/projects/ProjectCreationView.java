package com.budgetview.desktop.projects;

import com.budgetview.desktop.View;
import com.budgetview.desktop.projects.actions.CreateProjectAction;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class ProjectCreationView extends View {
  protected ProjectCreationView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectCreationView.splits",
                                                      repository, directory);

    builder.add("createProject", new CreateProjectAction(repository, directory));

    parentBuilder.add("projectCreationView", builder);
  }
}
