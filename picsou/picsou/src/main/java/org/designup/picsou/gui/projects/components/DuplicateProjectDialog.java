package org.designup.picsou.gui.projects.components;

import org.designup.picsou.gui.components.MonthSlider;
import org.designup.picsou.gui.components.MonthSliderAdapter;
import org.designup.picsou.gui.components.dialogs.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.utils.ProjectPeriodSliderAdapter;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class DuplicateProjectDialog {
  private Glob project;
  private GlobRepository repository;
  private Directory directory;

  private PicsouDialog dialog;
  private GlobsPanelBuilder builder;
  private Glob duplicateProject;
  private JTextField nameField;
  private Integer initialFirstMonth;

  public DuplicateProjectDialog(Glob project,
                                GlobRepository repository,
                                Directory directory,
                                Window owner) {
    this.project = project;
    this.repository = repository;
    this.directory = directory;
    createDialog(owner);
  }

  private void createDialog(Window owner) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/projects/components/duplicateProjectDialog.splits",
                                    repository, directory);

    Glob projectStat = repository.get(Key.create(ProjectStat.TYPE, project.get(Project.ID)));

    ProjectPeriodSliderAdapter adapter = new ProjectPeriodSliderAdapter();
    builder.add("message",
                GuiUtils.createReadOnlyHtmlComponent(
                  Lang.get("projectEdition.duplicate.message",
                           project.get(Project.NAME),
                           adapter.getText(projectStat, repository))));

    GlobRepository privateRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Month.TYPE)
        .get();
    initialFirstMonth = projectStat.get(ProjectStat.FIRST_MONTH);
    duplicateProject = privateRepository.create(DuplicateProject.TYPE,
                                                value(DuplicateProject.FIRST_MONTH, initialFirstMonth));

    nameField = GlobTextEditor.init(DuplicateProject.NAME, privateRepository, directory)
      .forceSelection(duplicateProject.getKey())
      .getComponent();
    builder.add("nameField", nameField);

    MonthSlider monthSlider = new MonthSlider(new DuplicateSliderAdapter(), privateRepository, directory);
    builder.add("firstMonth", monthSlider);
    monthSlider.setKey(duplicateProject.getKey());

    JPanel panel = builder.load();

    dialog = PicsouDialog.create(owner, true, directory);
    dialog.addPanelWithButtons(panel, new ValidateAction(), new CancelAction(dialog));
  }

  public void show() {
    dialog.pack();
    GuiUtils.selectAndRequestFocus(nameField);
    dialog.showCentered();
    builder.dispose();
  }

  public void createDuplicate(String name, Integer duplicateFirstMonth) {

    int offset = Month.distance(initialFirstMonth, duplicateFirstMonth);

    repository.startChangeSet();
    try {
      Glob newProject = repository.create(Project.TYPE,
                                          value(Project.NAME, name),
                                          value(Project.ACTIVE, true),
                                          value(Project.PICTURE, project.get(Project.PICTURE)));

      for (Glob item : repository.findLinkedTo(project, ProjectItem.PROJECT)) {

        FieldValues values = FieldValuesBuilder.initWithoutKeyFields(item)
          .set(ProjectItem.PROJECT, newProject.get(Project.ID))
          .set(ProjectItem.FIRST_MONTH, Month.offset(item.get(ProjectItem.FIRST_MONTH), offset))
          .remove(ProjectItem.SERIES)
          .get();

        Glob newItem = repository.create(ProjectItem.TYPE, values.toArray());

        for (Glob projectAmount : repository.findLinkedTo(item, ProjectItemAmount.PROJECT_ITEM)) {
          FieldValues amountValues =
            FieldValuesBuilder.initWithoutKeyFields(projectAmount)
              .set(ProjectItemAmount.PROJECT_ITEM, newItem.get(ProjectItem.ID))
              .set(ProjectItemAmount.MONTH, Month.offset(projectAmount.get(ProjectItemAmount.MONTH), offset))
              .get();
          repository.create(ProjectItemAmount.TYPE, amountValues.toArray());
        }

        for (Glob projectTransfer : repository.findLinkedTo(item, ProjectTransfer.PROJECT_ITEM)) {
          FieldValues transferValues =
            FieldValuesBuilder.initWithoutKeyFields(projectTransfer)
              .set(ProjectTransfer.PROJECT_ITEM, newItem.get(ProjectItem.ID))
              .get();
          repository.create(ProjectTransfer.TYPE, transferValues.toArray());
        }
      }
    }
    finally {
      repository.completeChangeSet();
      dialog.setVisible(false);
    }
  }

  private class ValidateAction extends AbstractAction {
    private ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      String name = DuplicateProjectDialog.this.duplicateProject.get(DuplicateProject.NAME);
      if (Strings.isNullOrEmpty(name)) {
        String errorMessage = Lang.get("projectEdition.error.noProjectName");
        ErrorTip.show(nameField, errorMessage, directory, TipPosition.BOTTOM_LEFT);
        return;
      }

      createDuplicate(name, duplicateProject.get(DuplicateProject.FIRST_MONTH));
    }
  }

  private static class DuplicateSliderAdapter implements MonthSliderAdapter {
    public String getText(Glob duplicateProject, GlobRepository repository) {
      if (duplicateProject == null) {
        return "";
      }
      return Month.getFullLabel(duplicateProject.get(DuplicateProject.FIRST_MONTH));
    }

    public String getMaxText() {
      return "December 2013";
    }

    public int getCurrentMonth(Glob duplicateProject, GlobRepository repository) {
      return duplicateProject.get(DuplicateProject.FIRST_MONTH);
    }

    public void setMonth(Glob duplicateProject, int selectedMonthId, GlobRepository repository) {
      repository.update(duplicateProject.getKey(), DuplicateProject.FIRST_MONTH, selectedMonthId);
    }
  }

  public static class DuplicateProject {
    public static GlobType TYPE;

    @org.globsframework.metamodel.annotations.Key
    public static IntegerField ID;

    public static StringField NAME;
    public static IntegerField FIRST_MONTH;

    static {
      GlobTypeLoader.init(DuplicateProject.class, "duplicateInfo");
    }
  }
}
