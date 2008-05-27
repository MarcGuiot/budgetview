package org.crossbowlabs.globs.gui.actions;

import org.crossbowlabs.globs.gui.utils.StringInputDialog;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeUtils;
import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.FieldValue;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.utils.GlobMatchers;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.crossbowlabs.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CreateGlobAction extends AbstractAction {
  private GlobType type;
  private Directory directory;
  private GlobRepository repository;
  private StringField namingField;

  public CreateGlobAction(String actionName, GlobType type, GlobRepository repository, Directory directory) {
    super(actionName);
    this.type = type;
    this.directory = directory;
    this.namingField = GlobTypeUtils.findNamingField(type);
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    if (namingField == null) {
      repository.create(type);
      return;
    }

    JDialog dialog = getDialog(e);
    StringInputDialog stringInput = new StringInputDialog(dialog, null,
                                                          getTitle(), getInputLabel(), getOkLabel(), getCancelLabel(),
                                                          directory) {
      protected void validate(String name) {
        validateName(type, namingField, name, repository);
      }
    };

    dialog.setModal(true);
    GuiUtils.showCentered(dialog);

    String name = stringInput.getSelectedName();
    if (name != null) {
      doCreate(type, namingField, name, repository);
    }
  }

  public JDialog getDialog(ActionEvent e) {
    return new JDialog(GuiUtils.getFrame(e));
  }

  /**
   * Override this to change the way the object is created.
   */
  protected Glob doCreate(GlobType type, StringField namingField, String name, GlobRepository repository) {
    return repository.create(type, value(namingField, name));
  }

  /**
   * Override this to check the entered name. The default implementation checks that the name is unique. The
   * displayed message will be that of the {@link InvalidParameter} exception thrown when the name is
   * rejected.
   */
  protected void validateName(GlobType type, StringField namingField, String name, GlobRepository repository)
    throws InvalidParameter {
    if (repository.getAll(type, GlobMatchers.fieldEquals(namingField, name)).size() > 0) {
      throw new InvalidParameter(getNameAlreadyExistsMessage(name));
    }
  }

  /**
   * Override this to change the dialog title. Default is "Creation".
   */
  protected String getTitle() {
    return "Creation";
  }

  /**
   * Override this to change the label in front of the text field. Default is "Name".
   */
  protected String getInputLabel() {
    return "Name";
  }

  /**
   * Override this to change the "OK" button label.
   */
  protected String getOkLabel() {
    return "OK";
  }

  /**
   * Override this to change the "Cancel" button label.
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
