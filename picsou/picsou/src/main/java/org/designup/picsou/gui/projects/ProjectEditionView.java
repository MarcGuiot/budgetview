package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.MonthSlider;
import org.designup.picsou.gui.components.charts.SimpleGaugeView;
import org.designup.picsou.gui.components.images.GlobImageLabelView;
import org.designup.picsou.gui.components.images.IconFactory;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.actions.CreateProjectAction;
import org.designup.picsou.gui.projects.actions.DeleteProjectAction;
import org.designup.picsou.gui.projects.components.DefaultPictureIcon;
import org.designup.picsou.gui.projects.components.ProjectNameEditor;
import org.designup.picsou.gui.projects.itemedition.ProjectItemPanelFactory;
import org.designup.picsou.gui.projects.utils.ImageStatusUpdater;
import org.designup.picsou.gui.projects.utils.ProjectItemComparator;
import org.designup.picsou.gui.projects.utils.ProjectPeriodSliderAdapter;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobToggleEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.utils.GlobRepeatListener;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
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
  private MonthSlider monthSlider;
  private ImageStatusUpdater imageStatusUpdater;
  private GlobToggleEditor activationToggle;
  private ModifyNameAction modifyAction;
  private JPanel addItemPanel = new JPanel();
  private Map<Key, ProjectItemPanel> itemPanels = new HashMap<Key, ProjectItemPanel>();

  public ProjectEditionView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.selectionService.addListener(this, Project.TYPE);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Project.TYPE)) {
          updateSelection(null, null);
        }
      }
    });
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList projects = selection.getAll(Project.TYPE);
    updateSelection(projects.size() == 1 ? projects.getFirst().getKey() : null,
                    selection.getAll(ProjectItem.TYPE).getKeySet());
  }

  private void updateSelection(Key projectKey, Set<Key> projectItemKeys) {
    this.currentProjectKey = projectKey;

    projectNameEditor.setCurrentProject(currentProjectKey);
    repeat.setFilter(linkedTo(currentProjectKey, ProjectItem.PROJECT));

    Key projectStatKey = currentProjectKey != null ? Key.create(ProjectStat.TYPE, currentProjectKey.get(Project.ID)) : null;
    totalActual.forceSelection(projectStatKey);
    totalPlanned.forceSelection(projectStatKey);
    gauge.setKey(projectStatKey);
    monthSlider.setKey(projectStatKey);
    imageStatusUpdater.setKey(currentProjectKey);

    if (projectItemKeys != null) {
      for (Key itemKey : projectItemKeys) {
        ProjectItemPanel itemPanel = itemPanels.get(itemKey);
        if (itemPanel != null) {
          itemPanel.edit();
        }
      }
    }
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectEditionView.splits",
                                                            repository, directory);

    modifyAction = new ModifyNameAction();
    PopupMenuFactory factory = new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(modifyAction);
        menu.add(new DeleteProjectAction(repository, directory));
        return menu;
      }
    };

    activationToggle = builder.addToggleEditor("activeToggle", Project.ACTIVE);
    builder.add("modify", modifyAction);

    projectNameEditor = new ProjectNameEditor(factory, repository, directory);
    builder.add("projectNameEditor", projectNameEditor.getPanel());
    projectNameEditor.setListener(new ProjectNameEditor.Listener() {
      public void processEditShown(boolean shown) {
        activationToggle.getComponent().setVisible(!shown);
        addItemPanel.setVisible(!shown);
      }
    });

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

    imageStatusUpdater = new ImageStatusUpdater(Project.ACTIVE, imageLabel, repository);

    monthSlider = new MonthSlider(new ProjectPeriodSliderAdapter(), repository, directory);
    builder.add("monthSlider", monthSlider);

    totalActual = builder.addLabel("totalActual", ProjectStat.ACTUAL_AMOUNT);
    totalPlanned = builder.addLabel("totalPlanned", ProjectStat.PLANNED_AMOUNT);

    final JPanel gaugePanel = new JPanel();
    builder.add("gaugePanel", gaugePanel);
    gauge =
      SimpleGaugeView.init(ProjectStat.ACTUAL_AMOUNT, ProjectStat.PLANNED_AMOUNT, repository, directory);
    builder.add("gauge", gauge.getComponent());

    repeat = builder.addRepeat("items",
                               ProjectItem.TYPE,
                               GlobMatchers.NONE,
                               new ProjectItemComparator(),
                               new ProjectItemRepeatFactory());
    repeat.addListener(new GlobRepeatListener() {
      public void listChanged(GlobList currentList) {
        gaugePanel.setVisible(!currentList.isEmpty());
      }
    });

    builder.add("addItemPanel", addItemPanel);
    builder.add("addExpenseItem", new JButton(new AddItemAction("projectEdition.addItem.expense", ProjectItemType.EXPENSE)));
    builder.add("addTransferItem", new JButton(new AddItemAction("projectEdition.addItem.transfer", ProjectItemType.TRANSFER)));

    builder.add("backToList", new BackToListAction());
    builder.add("createProject", new CreateProjectAction(directory));

    parentBuilder.add("projectEditionView", builder);

    updateSelection(null, null);
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
    updateSelection(projectKey, null);
  }

  private class ProjectItemRepeatFactory implements RepeatComponentFactory<Glob> {

    ProjectItemPanelFactory itemPanelFactory;

    private ProjectItemRepeatFactory() {
      itemPanelFactory = new ProjectItemPanelFactory(repository, directory);
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob item) {
      ProjectItemPanel itemPanel = itemPanelFactory.create(item);
      cellBuilder.add("projectItemPanel", itemPanel.getPanel());

      itemPanels.put(item.getKey(), itemPanel);
      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          itemPanels.remove(item.getKey());
        }
      });
      cellBuilder.addDisposeListener(itemPanel);
    }
  }

  private class AddItemAction extends AbstractAction {
    private ProjectItemType itemType;

    private AddItemAction(String labelKey, ProjectItemType itemType) {
      super(Lang.get(labelKey));
      this.itemType = itemType;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      repository.startChangeSet();
      try {
        String defaultLabel = itemType == ProjectItemType.TRANSFER ? Lang.get("projectView.item.transfer.defaultName") : "";
        Glob item = repository.create(ProjectItem.TYPE,
                                      value(ProjectItem.ITEM_TYPE, itemType.getId()),
                                      value(ProjectItem.LABEL, defaultLabel),
                                      value(ProjectItem.FIRST_MONTH, getNewItemMonth()),
                                      value(ProjectItem.PROJECT, currentProjectKey.get(Project.ID)));
        if (ProjectItemType.TRANSFER.equals(itemType)) {
          repository.create(ProjectTransfer.TYPE, value(ProjectTransfer.PROJECT_ITEM, item.get(ProjectItem.ID)));
        }
      }
      finally {
        repository.completeChangeSet();
      }
    }
  }

  private Integer getNewItemMonth() {
    GlobList items = repository.findLinkedTo(repository.find(currentProjectKey), ProjectItem.PROJECT);
    if (items.isEmpty()) {
      GlobList selection = selectionService.getSelection(Month.TYPE);
      if (selection.isEmpty()) {
        return directory.get(TimeService.class).getCurrentMonthId();
      }
      return selection.getSortedSet(Month.ID).last();
    }

    return items.getSortedSet(ProjectItem.FIRST_MONTH).last();
  }

  private class BackToListAction extends AbstractAction {
    private BackToListAction() {
      super(Lang.get("projectEdition.back"));
    }

    public void actionPerformed(ActionEvent e) {
      Glob project = repository.get(currentProjectKey);
      if (Strings.isNullOrEmpty(project.get(Project.NAME)) &&
          repository.findLinkedTo(project, ProjectItem.PROJECT).isEmpty()) {
        updateSelection(null, null);
        repository.delete(project);
      }
      selectionService.clear(Project.TYPE);
    }
  }
}
