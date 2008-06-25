package org.globsframework.gui.views;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.ComponentHolder;

import javax.swing.*;

public class GlobLabelView extends AbstractGlobTextView<GlobLabelView> {
  private JLabel label;

  public static GlobLabelView init(GlobType type, GlobRepository globRepository,
                                   Directory directory, GlobListStringifier stringifier) {
    GlobLabelView view = new GlobLabelView(type, globRepository, directory, stringifier);
    view.update();
    return view;
  }

  private GlobLabelView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    super(type, repository, directory, stringifier);
    this.label = new JLabel();
    this.label.setName(type.getName());
  }

  public JLabel getComponent() {
    return label;
  }

  protected void doUpdate(String text) {
    label.setText(text);
  }

  protected String getText() {
    return label.getText();
  }
}