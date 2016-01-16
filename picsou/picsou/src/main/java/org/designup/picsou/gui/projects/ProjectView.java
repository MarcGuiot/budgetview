package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.Project;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Set;

public class ProjectView extends View implements GlobSelectionListener, ChangeSetListener {

  public static Dimension MAX_PICTURE_SIZE = new Dimension(200, 200);

  private CardHandler cards;
  private Set<Key> currentKeys = Collections.EMPTY_SET;
  private ProjectListView projectListView;

  public ProjectView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectView.splits",
                                                      repository, directory);

    JPanel panel = new JPanel();
    builder.add("projectView", panel);

    ProjectCreationView projectCreationView = new ProjectCreationView(repository, directory);
    projectCreationView.registerComponents(builder);

    projectListView = new ProjectListView(repository, directory);
    projectListView.registerComponents(builder);

    ProjectEditionView projectEditionView = new ProjectEditionView(repository, directory);
    projectEditionView.registerComponents(builder);

    cards = builder.addCardHandler("cards");

    parentBuilder.add("projectView", builder.<Component>load());

    repository.addChangeListener(this);
    selectionService.addListener(this, Project.TYPE);

    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  public void createProject() {
    repository.create(Project.TYPE).getKey();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Project.TYPE)) {
      Set<Key> created = changeSet.getCreated(Project.TYPE);
      if (created.size() > 0) {
        selectionService.select(repository.get(created.iterator().next()));
      }
      else {
        switchToListIfNeeded();
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE)) {
      switchToListIfNeeded();
    }
  }

  private void update() {
    if (!repository.contains(Project.TYPE)) {
      cards.show("empty");
      return;
    }
    currentKeys = selectionService.getSelection(Project.TYPE).getKeySet();
    if (currentKeys.size() == 1) {
      cards.show("edit");
    }
    else {
      cards.show("list");
    }
  }

  private void switchToListIfNeeded() {
    GlobList projects = new GlobList();
    for (Key currentKey : currentKeys) {
      Glob project = repository.find(currentKey);
      if (project != null) {
        projects.add(project);
      }
    }
    if (projects.isEmpty()) {
      cards.show("list");
    }
  }

  public void reset() {
    projectListView.reset();
    update();
  }
}
