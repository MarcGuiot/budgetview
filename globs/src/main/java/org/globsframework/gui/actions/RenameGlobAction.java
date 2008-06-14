package org.globsframework.gui.actions;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.StringInputDialog;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RenameGlobAction extends AbstractAction implements GlobSelectionListener {
  private GlobType type;
  private StringField namingField;
  private Directory directory;
  private GlobRepository repository;
  private Glob currentObject;

  public RenameGlobAction(String actionName, StringField namingField, GlobRepository repository, Directory directory) {
    super(actionName);
    this.directory = directory;
    this.namingField = namingField;
    this.type = namingField.getGlobType();
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, type);
    setEnabled(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList objs = selection.getAll(type);
    this.currentObject = objs.size() == 1 ? objs.get(0) : null;
    if (currentObject != null && !accept(currentObject)) {
      currentObject = null;
    }
    setEnabled(currentObject != null);
  }

  /**
   * Override this to specify what objects can be renamed (default is all).
   */
  protected boolean accept(Glob glob) {
    return true;
  }

  public void actionPerformed(ActionEvent e) {
    JDialog dialog = getDialog(e);
    StringInputDialog stringInputDialog =
      new StringInputDialog(dialog,
                            currentObject.get(namingField),
                            getTitle(), getInputLabel(), getOkLabel(), getCancelLabel(),
                            directory) {
        protected void validate(String name) {
          validateName(type, namingField, name, repository);
        }
      };

    dialog.setModal(true);
    GuiUtils.showCentered(dialog);

    String newName = stringInputDialog.getSelectedName();
    if (newName != null) {
      repository.update(currentObject.getKey(), namingField, newName);
    }
  }

  public JDialog getDialog(ActionEvent e) {
    return new JDialog(GuiUtils.getFrame(e));
  }

  /**
   * Override this to check the entered name. The default implementation checks that the name is unique. The
   * displayed message will be that of the {@link org.globsframework.utils.exceptions.InvalidParameter} exception thrown when the name is
   * rejected.
   */
  protected void validateName(GlobType type, StringField namingField, String name, GlobRepository repository)
    throws InvalidParameter {
    GlobList objectsWithSameName = repository.getAll(type, GlobMatchers.fieldEquals(namingField, name));
    objectsWithSameName.remove(currentObject);
    if (objectsWithSameName.size() > 0) {
      throw new InvalidParameter(getNameAlreadyExistsMessage(name));
    }
  }

  /**
   * Override this to change the dialog title. Default is "Creation".
   */
  protected String getTitle() {
    return "Rename";
  }

  /**
   * Override this to change the label in front of the text field. Default is "Name".
   */
  protected String getInputLabel() {
    return "Name";
  }

  /**
   * Override this to change the "OK" button label . Default is "OK".
   */
  protected String getOkLabel() {
    return "OK";
  }

  /**
   * Override this to change the "Cancel" button label. Default is "Cancel".
   */
  protected String getCancelLabel() {
    return "Cancel";
  }

  /**
   * Override this to change the message displayed when the name is already used.
   */
  protected String getNameAlreadyExistsMessage(String name) {
    return "'" + name + "' already exists";
  }
}
