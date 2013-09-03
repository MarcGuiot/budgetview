package org.designup.picsou.gui.projects.itemedition;

import org.designup.picsou.gui.components.MonthSlider;
import org.designup.picsou.gui.components.SingleMonthAdapter;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.projects.ProjectItemPanel;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ProjectItemEditionPanel extends ProjectItemPanel {
  protected GlobTextEditor nameField;
  protected JTextField amountEditorField;
  protected GlobNumericEditor monthCountEditor;
  protected ValidateAction validate;

  public ProjectItemEditionPanel(Glob item, GlobRepository parentRepository, Directory directory) {
    super(item, parentRepository, directory);
  }

  protected void addCommonComponents(GlobsPanelBuilder builder) {
    validate = new ValidateAction();

    nameField = GlobTextEditor.init(ProjectItem.LABEL, localRepository, directory)
      .forceSelection(itemKey)
      .setValidationAction(validate);
    builder.add("nameField", nameField);
    disposables.add(nameField);

    MonthSlider monthSlider = new MonthSlider(new SingleMonthAdapter(ProjectItem.MONTH) {
      public String convertToString(Integer monthId) {
        return Month.getFullMonthLabelWith4DigitYear(monthId);
      }
    }, localRepository, directory);
    monthSlider.setKey(itemKey);
    builder.add("monthEditor", monthSlider);
    disposables.add(monthSlider);

    monthCountEditor = GlobNumericEditor.init(ProjectItem.MONTH_COUNT, localRepository, directory)
      .setPositiveNumbersOnly(true)
      .forceSelection(itemKey)
      .setValidationAction(validate);
    builder.add("monthCountEditor", monthCountEditor);
    disposables.add(monthCountEditor);
  }

  protected void initEditionFocus() {
    if (Strings.isNullOrEmpty(nameField.getComponent().getText())) {
      GuiUtils.selectAndRequestFocus(nameField.getComponent());
    }
    else {
      GuiUtils.selectAndRequestFocus(amountEditorField);
    }
  }

  protected boolean check() {
    JTextField nameField = this.nameField.getComponent();
    if (Strings.isNullOrEmpty(nameField.getText())) {
      ErrorTip.showLeft(nameField,
                        Lang.get("projectEdition.error.noItemName"),
                        directory);
      GuiUtils.selectAndRequestFocus(nameField);
      return false;
    }

    Glob projectItem = localRepository.get(itemKey);
    Integer count = projectItem.get(ProjectItem.MONTH_COUNT);
    if (count == null || count < 1) {
      JTextField monthCountField = monthCountEditor.getComponent();
      ErrorTip.showLeft(monthCountField,
                        Lang.get("projectEdition.error.invalidMonthCount"),
                        directory);
      GuiUtils.selectAndRequestFocus(monthCountField);
      return false;
    }

    return true;
  }
}
