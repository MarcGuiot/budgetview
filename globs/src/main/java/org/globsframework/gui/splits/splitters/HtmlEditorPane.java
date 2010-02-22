package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Strings;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Manages a JEditorPane configured to display HTML with a standard font and opacity disabled.
 */
public class HtmlEditorPane extends DefaultComponent<JEditorPane> {
  private String listenerName;
  private boolean useImageLocator;

  public HtmlEditorPane(SplitProperties properties,
                        Splitter[] subSplitters,
                        boolean acceptsSubSplitters) {
    super(JEditorPane.class, "htmlEditorPane", properties, subSplitters, acceptsSubSplitters);

    listenerName = properties.get("hyperlinkListener");
    useImageLocator = Boolean.TRUE.equals(properties.getBoolean("useImageLocator"));
  }

  protected String[] getExcludedParameters() {
    return new String[]{"hyperlinkListener", "useImageLocator"};
  }

  protected void postCreateComponent(JEditorPane editor, SplitsContext context) {
    GuiUtils.initReadOnlyHtmlComponent(editor);

    if (listenerName != null) {
      HyperlinkListener hyperlinkListener = context.getHyperlinkListener(listenerName);
      editor.addHyperlinkListener(hyperlinkListener);
    }

    if (useImageLocator) {
      String text = editor.getText();
      setLocatorEditorKit(editor, context.getDirectory().get(ImageLocator.class));
      editor.setText(text);
    }
  }

  private void setLocatorEditorKit(JEditorPane component, ImageLocator locator) {
    final LocalViewFactory factory = new LocalViewFactory(locator);

    component.setEditorKit(new HTMLEditorKit() {
      public ViewFactory getViewFactory() {
        return factory;
      }
    });
  }

  private class LocalViewFactory extends HTMLEditorKit.HTMLFactory {
    private ImageLocator locator;

    public LocalViewFactory(ImageLocator locator) {
      this.locator = locator;
    }

    public View create(Element elem) {
      Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
      if (o instanceof HTML.Tag) {
        HTML.Tag kind = (HTML.Tag)o;
        if (kind == HTML.Tag.IMG) {
          return new LocalImageView(elem);
        }
      }
      return super.create(elem);
    }

    private class LocalImageView extends View {
      private Image localImage;

      public LocalImageView(Element elem) {
        super(elem);
        AttributeSet attributes = elem.getAttributes();
        String name = Strings.toString(attributes.getAttribute(HTML.Attribute.SRC));
        this.localImage = locator.get(name).getImage();
        Dictionary cache = new Hashtable() {
          public Object get(Object key) {
            return localImage;
          }
        };
        getDocument().putProperty("imageCache", cache);
      }

      public float getPreferredSpan(int axis) {
        switch (axis) {
          case View.X_AXIS:
            return localImage.getWidth(null);
          case View.Y_AXIS:
            return localImage.getHeight(null);
          default:
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
      }

      public void paint(Graphics g, Shape a) {
        Image image = localImage;

        Rectangle rect = (a instanceof Rectangle) ? (Rectangle)a :
                         a.getBounds();

        g.drawImage(image, rect.x, rect.y, null);
      }

      public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        if ((pos >= p0) && (pos <= p1)) {
          Rectangle r = a.getBounds();
          if (pos == p1) {
            r.x += r.width;
          }
          r.width = 0;
          return r;
        }
        return null;
      }

      public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        Rectangle alloc = (Rectangle)a;
        if (x < alloc.x + alloc.width) {
          bias[0] = Position.Bias.Forward;
          return getStartOffset();
        }
        bias[0] = Position.Bias.Backward;
        return getEndOffset();

      }
    }
  }

}
