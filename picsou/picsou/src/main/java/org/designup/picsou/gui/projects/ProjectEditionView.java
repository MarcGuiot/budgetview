package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.charts.SimpleGaugeView;
import org.designup.picsou.gui.components.images.GlobImageLabelView;
import org.designup.picsou.gui.components.images.IconFactory;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.actions.DeleteProjectAction;
import org.designup.picsou.gui.projects.components.DefaultPictureIcon;
import org.designup.picsou.gui.projects.components.ProjectNameEditor;
import org.designup.picsou.gui.projects.utils.ProjectItemComparator;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectEditionView extends View implements GlobSelectionListener {

  private Key currentProjectKey;

  private GlobRepeat repeat;
  private ProjectNameEditor projectNameEditor;

  private SimpleGaugeView gauge;
  private GlobLabelView totalActual;
  private GlobLabelView totalPlanned;

  public ProjectEditionView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.selectionService.addListener(this, Project.TYPE);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Project.TYPE)) {
          currentProjectKey = null;
          updateSelection();
        }
      }
    });
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList projects = selection.getAll(Project.TYPE);
    currentProjectKey = projects.size() == 1 ? projects.getFirst().getKey() : null;
    updateSelection();
  }

  private void updateSelection() {
    projectNameEditor.setCurrentProject(currentProjectKey);
    repeat.setFilter(linkedTo(currentProjectKey, ProjectItem.PROJECT));

    Key projectStatKey = currentProjectKey != null ? Key.create(ProjectStat.TYPE, currentProjectKey.get(Project.ID)) : null;
    totalActual.forceSelection(projectStatKey);
    totalPlanned.forceSelection(projectStatKey);
    gauge.setKey(projectStatKey);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectEditionView.splits",
                                                      repository, directory);

    final ModifyNameAction modify = new ModifyNameAction();
    PopupMenuFactory factory = new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(modify);
        menu.add(new DeleteProjectAction(repository, directory));
        return menu;
      }
    };
    projectNameEditor = new ProjectNameEditor(factory, repository, directory);
    builder.add("projectNameEditor", projectNameEditor.getPanel());

    builder.addToggleEditor("activeToggle", Project.ACTIVE);
    builder.add("modify", modify);

    GlobImageLabelView imageLabel =
      GlobImageLabelView.init(Project.PICTURE, ProjectView.MAX_PICTURE_SIZE, repository, directory)
      .setDefaultIconFactory(new IconFactory() {
        public Icon createIcon(Dimension size) {
          DefaultPictureIcon defaultIcon = new DefaultPictureIcon(size, directory);
          builder.addDisposable(defaultIcon);
          return defaultIcon;
        }
      });
    builder.add("imageLabel", imageLabel.getLabel());

    totalActual = builder.addLabel("totalActual", ProjectStat.ACTUAL_AMOUNT);
    totalPlanned = builder.addLabel("totalPlanned", ProjectStat.PLANNED_AMOUNT);

    gauge =
      SimpleGaugeView.init(ProjectStat.ACTUAL_AMOUNT, ProjectStat.PLANNED_AMOUNT, repository, directory)
       .setAutoHideIfEmpty(true);
    builder.add("gauge", gauge.getComponent());

    repeat = builder.addRepeat("items",
                               ProjectItem.TYPE,
                               GlobMatchers.NONE,
                               new ProjectItemComparator(),
                               new ProjectItemRepeatFactory());

    builder.add("addItem", new AddItemAction());

    builder.add("backToList", new BackToListAction());

    parentBuilder.add("projectEditionView", builder);
  }

  private class ModifyNameAction extends AbstractAction {
    private ModifyNameAction() {
      super(Lang.get("rename"));
    }

    public void actionPerformed(ActionEvent e) {
      projectNameEditor.edit();
    }
  }

  public void show(Key projectKey) {
    currentProjectKey = projectKey;
    updateSelection();
  }

  private class ProjectItemRepeatFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob item) {
      ProjectItemPanel itemPanel = new ProjectItemPanel(item, repository, directory);
      cellBuilder.add("projectItemPanel", itemPanel.getPanel());
      cellBuilder.addDisposeListener(itemPanel);
    }
  }

  private class AddItemAction extends AbstractAction {
    private AddItemAction() {
      super(Lang.get("projectEdition.addItem"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      createItem();
    }
  }

  private Key createItem() {
    repository.startChangeSet();
    Glob item = null;
    try {
      item = repository.create(ProjectItem.TYPE,
                                    value(ProjectItem.LABEL, ""),
                                    value(ProjectItem.MONTH, getLastMonth()),
                                    value(ProjectItem.PROJECT, currentProjectKey.get(Project.ID)));
    }
    finally {
      repository.completeChangeSet();
    }
    if (item == null) {
      throw new InvalidState("Item creation failed");
    }
    return item.getKey();
  }

  private Integer getLastMonth() {
    GlobList existingItems =
      repository.getAll(ProjectItem.TYPE,
                        linkedTo(currentProjectKey, ProjectItem.PROJECT));
    if (existingItems.isEmpty()) {
      return directory.get(TimeService.class).getCurrentMonthId();
    }
    return existingItems.getSortedSet(ProjectItem.MONTH).last();
  }

  private class BackToListAction extends AbstractAction {
    private BackToListAction() {
      super(Lang.get("projectEdition.back"));
    }

    public void actionPerformed(ActionEvent e) {
      selectionService.clear(Project.TYPE);
    }
  }
}
