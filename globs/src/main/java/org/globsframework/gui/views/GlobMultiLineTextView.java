package org.globsframework.gui.views;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class GlobMultiLineTextView extends AbstractGlobTextView<GlobMultiLineTextView> {
  private JTextArea textArea;

  public static GlobMultiLineTextView init(GlobType type, GlobRepository repository,
                                           Directory directory, GlobListStringifier stringifier) {
    return new GlobMultiLineTextView(type, repository, directory, stringifier);
  }

  public static GlobMultiLineTextView init(Field field, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(field);
    return init(field.getGlobType(), repository, directory, stringifier);
  }

  private GlobMultiLineTextView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    super(type, repository, directory, stringifier);
    this.textArea = new JTextArea();
    this.textArea.setName(type.getName());
    this.textArea.setEditable(false);
  }

  public JTextArea getComponent() {
    if (!initCompleted) {
      initCompleted = true;
      complete();
      update();
    }
    return textArea;
  }

  protected void doUpdate(String text) {
    textArea.setText(text);
    textArea.setCaretPosition(0);
  }

  protected String getText() {
    return textArea.getText();
  }
}