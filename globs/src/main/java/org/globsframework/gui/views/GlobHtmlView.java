package org.globsframework.gui.views;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;

public class GlobHtmlView extends AbstractGlobTextView<GlobHtmlView> {
  private JEditorPane editorPane;
  private String text;

  public static GlobHtmlView init(GlobType type, GlobRepository globRepository,
                                  Directory directory, GlobListStringifier stringifier) {
    return new GlobHtmlView(type, globRepository, directory, stringifier);
  }

  public static GlobHtmlView init(Field field, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(field);
    return init(field.getGlobType(), repository, directory, stringifier);
  }

  private GlobHtmlView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    super(type, repository, directory, stringifier);
    this.editorPane = new JEditorPane();
    this.editorPane.setContentType("text/html");
  }

  public void addHyperlinkListener(HyperlinkListener listener) {
    editorPane.addHyperlinkListener(listener);
  }

  public JEditorPane getComponent() {
    if (!initCompleted) {
      initCompleted = true;
      complete();
      update();
    }
    return editorPane;
  }

  protected void doUpdate(String text) {
    this.text = text;
    this.editorPane.setText(text);
  }

  protected String getText() {
    return text;
  }
}