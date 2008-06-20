package org.globsframework.gui.views;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;

public class GlobHtmlView extends AbstractGlobTextView<GlobHtmlView> {
  private JEditorPane editorPane;
  private String text;

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
    this.text = text;
    editorPane.setText(text);
  }

  protected String getText() {
    return text;
  }
}