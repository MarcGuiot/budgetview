package com.budgetview.gui.projects;

import com.budgetview.gui.View;
import com.budgetview.gui.projects.actions.CreateProjectAction;
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
