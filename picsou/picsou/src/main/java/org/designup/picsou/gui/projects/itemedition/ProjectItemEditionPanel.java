package org.designup.picsou.gui.projects.itemedition;

import org.designup.picsou.gui.components.MonthSlider;
import org.designup.picsou.gui.components.SingleMonthAdapter;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.projects.ProjectItemPanel;
import org.designup.picsou.gui.projects.components.ProjectItemAmountEditor;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class ProjectItemEditionPanel extends ProjectItemPanel {
  protected GlobTextEditor nameField;
  protected ValidateAction validate;
  private ProjectItemAmountEditor itemAmountEditor;

  public ProjectItemEditionPanel(Glob item, GlobRepository parentRepository, Directory directory) {
    super(item, parentRepository, directory);
  }

  protected void addCommonComponents(GlobsPanelBuilder builder, boolean forcePositiveAmounts) {
    validate = new ValidateAction();

    nameField = GlobTextEditor.init(ProjectItem.LABEL, localRepository, directory)
      .forceSelection(itemKey)
      .setValidationAction(validate);
    builder.add("nameField", nameField);

    itemAmountEditor = new ProjectItemAmountEditor(itemKey, forcePositiveAmounts, validate, localRepository, directory);
    builder.add("itemAmountEditor", itemAmountEditor.getPanel());
    disposables.add(itemAmountEditor);
  }

  protected void initEditionFocus() {
    if (Strings.isNullOrEmpty(nameField.getComponent().getText())) {
      GuiUtils.selectAndRequestFocus(nameField.getComponent());
    }
    else {
      itemAmountEditor.requestFocus();
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

    if (!itemAmountEditor.check()) {
      return false;
    }

    return true;
  }
}
