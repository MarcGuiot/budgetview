package com.budgetview.gui.projects;

import com.budgetview.gui.View;
import com.budgetview.gui.components.MonthSlider;
import com.budgetview.gui.description.AmountStringifier;
import com.budgetview.gui.model.ProjectStat;
import com.budgetview.gui.projects.actions.SortProjectItemsAction;
import com.budgetview.gui.projects.itemedition.ProjectItemPanelFactory;
import com.budgetview.gui.projects.utils.ImageStatusUpdater;
import com.budgetview.model.*;
import com.budgetview.gui.accounts.utils.AccountCreation;
import com.budgetview.gui.components.charts.SimpleGaugeView;
import com.budgetview.gui.components.images.GlobImageLabelView;
import com.budgetview.gui.components.images.IconFactory;
import com.budgetview.gui.projects.actions.DeleteProjectAction;
import com.budgetview.gui.projects.actions.DuplicateProjectAction;
import com.budgetview.gui.projects.components.DefaultPictureIcon;
import com.budgetview.gui.projects.components.ProjectEditor;
import com.budgetview.gui.projects.utils.ProjectItemComparator;
import com.budgetview.gui.projects.utils.ProjectPeriodSliderAdapter;
import com.budgetview.gui.time.TimeService;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobToggleEditor;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.components.GlobRepeatListener;
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
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectEditionView extends View implements GlobSelectionListener {

  private Key currentProjectKey;

  private GlobRepeat repeat;
  private ProjectEditor projectEditor;

  private SimpleGaugeView gauge;
  private GlobLabelView totalActual;
  private GlobLabelView totalPlanned;
  private MonthSlider monthSlider;
  private ImageStatusUpdater imageStatusUpdater;
  private GlobToggleEditor activationToggle;
  private ModifyNameAction modifyAction;
  private JPanel addItemPanel = new JPanel();
  private Map<Key, ProjectItemPanel> itemPanels = new HashMap<Key, ProjectItemPanel>();
  private JScrollPane scrollPane;

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

    projectEditor.setCurrentProject(currentProjectKey);
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
        menu.add(new DuplicateProjectAction(repository, directory));
        menu.addSeparator();
        menu.add(new SortProjectItemsAction(repository, directory));
        menu.addSeparator();
        menu.add(new DeleteProjectAction(repository, directory));
        return menu;
      }
    };

    scrollPane = new JScrollPane();
    builder.add("scroller", scrollPane);

    activationToggle = builder.addToggleEditor("activeToggle", Project.ACTIVE);

    projectEditor = new ProjectEditor(factory, repository, directory);
    builder.add("projectEditor", projectEditor.getPanel());
    projectEditor.setListener(new ProjectEditor.Listener() {
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

    totalActual = builder.addLabel("totalActual", ProjectStat.TYPE, AmountStringifier.getForList(ProjectStat.ACTUAL_AMOUNT, BudgetArea.EXTRAS));
    totalPlanned = builder.addLabel("totalPlanned", ProjectStat.TYPE, AmountStringifier.getForList(ProjectStat.PLANNED_AMOUNT, BudgetArea.EXTRAS));

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

    parentBuilder.add("projectEditionView", builder);

    updateSelection(null, null);
  }

  private class ModifyNameAction extends AbstractAction {
    private ModifyNameAction() {
      super(Lang.get("rename"));
    }

    public void actionPerformed(ActionEvent e) {
      projectEditor.edit();
    }
  }

  public void show(Key projectKey) {
    currentProjectKey = projectKey;
    updateSelection(projectKey, null);
  }

  private class ProjectItemRepeatFactory implements RepeatComponentFactory<Glob> {

    ProjectItemPanelFactory itemPanelFactory;

    private ProjectItemRepeatFactory() {
      itemPanelFactory = new ProjectItemPanelFactory(scrollPane, repository, directory);
    }

    public void registerComponents(PanelBuilder cellBuilder, final Glob item) {
      ProjectItemPanel itemPanel = itemPanelFactory.create(item);
      cellBuilder.add("projectItemPanel", itemPanel.getPanel());

      itemPanels.put(item.getKey(), itemPanel);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          itemPanels.remove(item.getKey());
        }
      });
      cellBuilder.addDisposable(itemPanel);
    }
  }

  private class AddItemAction extends AbstractAction {
    private ProjectItemType itemType;

    private AddItemAction(String labelKey, ProjectItemType itemType) {
      super(Lang.get(labelKey));
      this.itemType = itemType;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      if (!AccountCreation.containsUserAccount(repository, directory,
                                               Lang.get("accountCreation.projectCreation.message"))) {
        return;
      }

      repository.startChangeSet();
      try {
        String defaultLabel = itemType == ProjectItemType.TRANSFER ? Lang.get("projectView.item.transfer.defaultName") : "";
        Integer projectId = currentProjectKey.get(Project.ID);
        Glob item = repository.create(ProjectItem.TYPE,
                                      value(ProjectItem.ITEM_TYPE, itemType.getId()),
                                      value(ProjectItem.LABEL, defaultLabel),
                                      value(ProjectItem.FIRST_MONTH, getNewItemMonth()),
                                      value(ProjectItem.PROJECT, projectId),
                                      value(ProjectItem.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID),
                                      value(ProjectItem.SEQUENCE_NUMBER, ProjectItem.getNextSequenceNumber(projectId, ProjectEditionView.this.repository)));
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
