package org.designup.picsou.gui.components;

import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.utils.AbstractDocumentListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class TextFieldLimit {

  private Highlighter hilight;
  private Highlighter.HighlightPainter painter;
  private final JTextField textField;
  private final int limit;
  private Object lastHandle;

  public static void install(final JTextField textField, final int limit) {
    new TextFieldLimit(textField, limit);
  }

  private TextFieldLimit(JTextField textField, int limit) {
    this.textField = textField;
    this.limit = limit;
    this.hilight = new DefaultHighlighter();
    this.painter = new DefaultHighlighter.DefaultHighlightPainter(Colors.toColor("FFE0E0"));
    textField.setHighlighter(hilight);

    textField.getDocument().addDocumentListener(new DocListener());
  }

  private class DocListener extends AbstractDocumentListener {
    protected void documentChanged(DocumentEvent e) {
      if (lastHandle != null) {
        hilight.removeHighlight(lastHandle);
        lastHandle = null;
      }

      String text = textField.getText();
      if (text.length() <= limit) {
        return;
      }

      try {
        lastHandle = hilight.addHighlight(limit, text.length(), painter);
      }
      catch (BadLocationException e1) {
        throw new RuntimeException(e1);
      }
    }
  }
}
