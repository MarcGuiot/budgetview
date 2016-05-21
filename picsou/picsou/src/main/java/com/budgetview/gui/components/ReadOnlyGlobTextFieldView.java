package com.budgetview.gui.components;

import org.globsframework.gui.views.AbstractGlobTextView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ReadOnlyGlobTextFieldView extends AbstractGlobTextView<ReadOnlyGlobTextFieldView> {

  private JTextField textField = new JTextField();

  public static ReadOnlyGlobTextFieldView init(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    return new ReadOnlyGlobTextFieldView(type, repository, directory, stringifier);
  }

  public static ReadOnlyGlobTextFieldView init(LinkField link, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(link);
    return new ReadOnlyGlobTextFieldView(link.getGlobType(), repository, directory, stringifier);
  }

  public ReadOnlyGlobTextFieldView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    super(type, repository, directory, stringifier);
    textField.setEditable(false);
  }

  protected void doUpdate(String text) {
    textField.setText(text);
  }

  protected String getText() {
    return textField.getText();
  }

  public JTextField getComponent() {
    if (!initCompleted) {
      initCompleted = true;
      complete();
      update();
    }
    return textField;
  }
}
