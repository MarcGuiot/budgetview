package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobListStringifier;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;

public class GlobLabelView extends AbstractGlobTextView {
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
}