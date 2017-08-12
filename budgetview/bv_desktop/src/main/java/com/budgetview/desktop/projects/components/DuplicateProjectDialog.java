package com.budgetview.desktop.projects.components;

import com.budgetview.desktop.components.MonthSlider;
import com.budgetview.desktop.components.MonthSliderAdapter;
import com.budgetview.desktop.components.dialogs.CancelAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.components.tips.ErrorTip;
import com.budgetview.desktop.components.tips.TipPosition;
import com.budgetview.desktop.model.ProjectStat;
import com.budgetview.desktop.projects.utils.ProjectPeriodSliderAdapter;
import com.budgetview.model.Month;
import com.budgetview.model.Project;
import com.budgetview.model.util.TypeLoader;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
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
    duplicateProject = privateRepository.create(ProjectDuplicate.TYPE,
                                                value(ProjectDuplicate.FIRST_MONTH, initialFirstMonth));

    nameField = GlobTextEditor.init(ProjectDuplicate.NAME, privateRepository, directory)
      .forceSelection(duplicateProject.getKey())
      .getComponent();
    builder.add("nameField", nameField);

    MonthSlider monthSlider = new MonthSlider(new DuplicateSliderAdapter(), privateRepository, directory);
    builder.add("firstMonth", monthSlider);
    monthSlider.setKey(duplicateProject.getKey());

    JPanel panel = builder.load();

    dialog = PicsouDialog.create(this, owner, true, directory);
    dialog.addPanelWithButtons(panel, new ValidateAction(), new CancelAction(dialog));
  }

  public void show() {
    dialog.pack();
    GuiUtils.selectAndRequestFocus(nameField);
    dialog.showCentered();
    builder.dispose();
  }

  public void createDuplicate(String newProjectName, Integer duplicateFirstMonth) {

    int monthOffset = Month.distance(initialFirstMonth, duplicateFirstMonth);

    try {
      Project.duplicate(project, newProjectName, monthOffset, repository);
    }
    finally {
      dialog.setVisible(false);
    }
  }

  private class ValidateAction extends AbstractAction {
    private ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      String name = DuplicateProjectDialog.this.duplicateProject.get(ProjectDuplicate.NAME);
      if (Strings.isNullOrEmpty(name)) {
        String errorMessage = Lang.get("projectEdition.error.noProjectName");
        ErrorTip.show(nameField, errorMessage, directory, TipPosition.BOTTOM_LEFT);
        return;
      }

      createDuplicate(name, duplicateProject.get(ProjectDuplicate.FIRST_MONTH));
    }
  }

  private static class DuplicateSliderAdapter implements MonthSliderAdapter {
    public String getText(Glob duplicateProject, GlobRepository repository) {
      if (duplicateProject == null) {
        return "";
      }
      return Month.getFullLabel(duplicateProject.get(ProjectDuplicate.FIRST_MONTH), true);
    }

    public String getMaxText() {
      return "December 2013";
    }

    public int getCurrentMonth(Glob duplicateProject, GlobRepository repository) {
      return duplicateProject.get(ProjectDuplicate.FIRST_MONTH);
    }

    public void setMonth(Glob duplicateProject, int selectedMonthId, GlobRepository repository) {
      repository.update(duplicateProject.getKey(), ProjectDuplicate.FIRST_MONTH, selectedMonthId);
    }
  }

  public static class ProjectDuplicate {
    public static GlobType TYPE;

    @org.globsframework.metamodel.annotations.Key
    public static IntegerField ID;

    public static StringField NAME;
    public static IntegerField FIRST_MONTH;

    static {
      TypeLoader.init(ProjectDuplicate.class, "duplicateInfo");
    }
  }
}
