package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.MonthRangeBound;
import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.description.MonthFieldListStringifier;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.ProjectTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;

public class ProjectEditionDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Directory directory;
  private JLabel titleLabel = new JLabel();
  private GlobRepeat repeat;
  private GlobTextEditor projectNameEditor;
  private Key currentProjectKey;
  private Map<Key, JTextField> itemNameCheckers = new HashMap<Key, JTextField>();

  public ProjectEditionDialog(GlobRepository parentRepository, Directory directory) {
    this(parentRepository, directory, directory.get(JFrame.class));
  }

  public ProjectEditionDialog(GlobRepository parentRepository, Directory directory, Window owner) {
    this.directory = directory;
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(Project.TYPE, ProjectItem.TYPE, Month.TYPE, Series.TYPE, SeriesBudget.TYPE)
      .get();
    this.localRepository.addTrigger(new ProjectTrigger());

    createDialog(owner);
  }

  private void createDialog(Window owner) {
    dialog = PicsouDialog.create(owner, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectEditionDialog.splits",
                                                      localRepository, directory);

    builder.add("title", titleLabel);

    projectNameEditor = GlobTextEditor.init(Project.NAME, localRepository, directory);
    builder.add("projectName", projectNameEditor.getComponent());

    repeat = builder.addRepeat("items",
                               ProjectItem.TYPE,
                               GlobMatchers.NONE,
                               new ProjectItemComparator(),
                               new ProjectItemRepeatFactory());

    builder.add("addItem", new AddItemAction());

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new OkAction(), new CancelAction(dialog),
                               new DeleteAction());
    dialog.pack();
  }

  public void showNewProject() {
    titleLabel.setText(Lang.get("projectEdition.title.create"));
    localRepository.rollback();
    currentProjectKey = localRepository.create(Project.TYPE).getKey();
    createItem();
    doShow();
  }

  public void show(Key projectKey) {
    titleLabel.setText(Lang.get("projectEdition.title.edit"));
    localRepository.rollback();
    currentProjectKey = projectKey;
    doShow();
  }

  private void doShow() {
    projectNameEditor.forceSelection(currentProjectKey);
    repeat.setFilter(GlobMatchers.linkedTo(currentProjectKey, ProjectItem.PROJECT));
    dialog.setVisible(true);
  }

  private boolean check() {
    JTextField projectNameField = projectNameEditor.getComponent();
    if (Strings.isNullOrEmpty(projectNameField.getText())) {
      ErrorTip.showLeft(projectNameField,
                        Lang.get("projectEdition.error.noProjectName"),
                        directory);
      projectNameField.requestFocus();
      return false;
    }

    for (Glob item : repeat.getCurrentGlobs()) {
      JTextField itemNameField = itemNameCheckers.get(item.getKey());
      if (Strings.isNullOrEmpty(itemNameField.getText())) {
        ErrorTip.showLeft(itemNameField,
                          Lang.get("projectEdition.error.noItemName"),
                          directory);
        itemNameField.requestFocus();
        return false;
      }
    }

    return true;
  }

  private class ProjectItemRepeatFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob item) {
      final Key itemKey = item.getKey();
      JTextField nameField =
        GlobTextEditor.init(ProjectItem.LABEL, localRepository, directory)
          .forceSelection(itemKey)
          .getComponent();
      cellBuilder.add("label", nameField);
      itemNameCheckers.put(itemKey, nameField);
      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          itemNameCheckers.remove(itemKey);
        }
      });

      cellBuilder.add("month",
                      GlobButtonView.init(ProjectItem.TYPE,
                                          localRepository, directory,
                                          new MonthFieldListStringifier(ProjectItem.MONTH),
                                          new EditMonthCallback())
                        .forceSelection(itemKey)
                        .getComponent());

      AmountEditor amountEditor =
        new AmountEditor(ProjectItem.AMOUNT, localRepository, directory, false, null)
          .forceSelection(itemKey)
          .update(false, false);
      JTextField amountField = amountEditor.getNumericEditor().getComponent();
      cellBuilder.add("amount", amountField);
      cellBuilder.add("positiveAmounts", amountEditor.getPositiveRadio());
      cellBuilder.add("negativeAmounts", amountEditor.getNegativeRadio());
    }
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!check()) {
        return;
      }
      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
  }

  private class DeleteAction extends AbstractAction {
    private DeleteAction() {
      super(Lang.get("projectEdition.delete"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      localRepository.delete(currentProjectKey);
      dialog.setVisible(false);
    }
  }

  private class EditMonthCallback implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      if (list.isEmpty()) {
        return;
      }
      Glob item = list.getFirst();
      Integer month = item.get(ProjectItem.MONTH);
      MonthChooserDialog monthChooser = new MonthChooserDialog(dialog, directory);
      int result = monthChooser.show(month, MonthRangeBound.NONE, month);
      if (result > 0) {
        localRepository.update(item.getKey(), ProjectItem.MONTH, result);
      }
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

  private void createItem() {
    localRepository.create(ProjectItem.TYPE,
                           value(ProjectItem.LABEL, ""),
                           value(ProjectItem.MONTH, getLastMonth()),
                           value(ProjectItem.PROJECT, currentProjectKey.get(Project.ID)));
  }

  private Integer getLastMonth() {
    GlobList existingItems =
      localRepository.getAll(ProjectItem.TYPE,
                             GlobMatchers.linkedTo(currentProjectKey, ProjectItem.PROJECT));
    if (existingItems.isEmpty()) {
      return directory.get(TimeService.class).getCurrentMonthId();
    }
    return existingItems.getSortedSet(ProjectItem.MONTH).last();
  }
}
