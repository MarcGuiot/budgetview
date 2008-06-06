package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobListStringifier;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;

public class GlobHtmlView extends AbstractGlobTextView {
  private JEditorPane editorPane;


  public static GlobHtmlView init(GlobType type, GlobRepository globRepository,
                                   Directory directory, GlobListStringifier stringifier) {
    GlobHtmlView view = new GlobHtmlView(type, globRepository, directory, stringifier);
    view.update();
    return view;
  }

  private GlobHtmlView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    super(type, repository, directory, stringifier);
    this.editorPane = new JEditorPane();
    this.editorPane.setContentType("text/html");
    this.editorPane.setName(type.getName());
  }

  public void addHyperlinkListener(HyperlinkListener listener) {
    editorPane.addHyperlinkListener(listener);
}

  public JEditorPane getComponent() {
    return editorPane;
  }

  protected void doUpdate(String text) {
    editorPane.setText(text);
  }
}