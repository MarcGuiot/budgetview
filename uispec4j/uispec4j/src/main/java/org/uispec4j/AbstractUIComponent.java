package org.uispec4j;

import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.testlibrairies.AssertAdapter;
import org.uispec4j.utils.ColorUtils;
import org.uispec4j.utils.KeyUtils;
import org.uispec4j.utils.UIComponentFactory;
import org.uispec4j.utils.Utils;

import javax.swing.*;
import java.awt.*;

/**
 * Base class for UIComponent implementations.
 */
public abstract class AbstractUIComponent implements UIComponent {

  public String toString() {
    return getDescription();
  }

  public final String getDescription() {
    StringBuilder builder = new StringBuilder();
    printDescription(getAwtComponent(), "", builder, false);
    return builder.toString().trim();
  }

  protected void printDescription(Component component, String indent, StringBuilder builder, boolean showVisibleOnly) {
    if (showVisibleOnly && !component.isVisible()) {
      return;
    }

    builder
      .append(indent)
      .append(component.getClass().getSimpleName());
    addAttributes(component, builder);
    builder.append('\n');

    if (!(component instanceof Container)) {
      return;
    }

    if (component instanceof JScrollPane) {
      printDescriptionForChildren(((JScrollPane) component).getViewport(), indent, builder);
      return;
    }

    AbstractUIComponent uiComponent =
      (AbstractUIComponent) UIComponentFactory.createUIComponent(component);
    if (uiComponent != null) {
      uiComponent.printDescriptionForChildren((Container) component, indent, builder);
    }
    else {
      printDescriptionForChildren((Container) component, indent, builder);
    }
  }

  protected void printDescriptionForChildren(Container container, String indent, StringBuilder builder) {
    String newIndent = indent + "  ";
    for (Component child : container.getComponents()) {
      AbstractUIComponent uiComponent =
        (AbstractUIComponent) UIComponentFactory.createUIComponent(child);
      if (uiComponent != null) {
        uiComponent.printDescription(child, newIndent, builder, true);
      }
      else {
        printDescription(child, newIndent, builder, true);
      }
    }
  }

  protected void addAttributes(Component component, StringBuilder builder) {
    addAttribute("name", component.getName(), builder);
  }

  protected void addAttribute(String name, String value, StringBuilder builder) {
    if ((value == null) || value.isEmpty()) {
      return;
    }
    builder.append(' ').append(name).append(":'").append(cleanUpText(value)).append("'");
  }

  private String cleanUpText(String text) {
    text = text.trim()
      .replaceAll("[\n\t]", " ")
      .replaceAll(" [ ]+", " ");
    if (text.startsWith("<html>")) {
      text = "[HTML] " + Utils.cleanupHtml(text).trim();
    }
    if (text.length() > 50) {
      return text.substring(0, 23) + "..." + text.substring(text.length() - 23);
    }
    return text;
  }

  public String getName() {
    return getAwtComponent().getName();
  }

  public String getLabel() {
    return null;
  }

  public Assertion isVisible() {
    return new Assertion() {
      public void check() {
        AssertAdapter.assertTrue(getAwtComponent().isVisible());
      }
    };
  }

  public Assertion isEnabled() {
    return new Assertion() {
      public void check() {
        AssertAdapter.assertTrue(getAwtComponent().isEnabled());
      }
    };
  }

  /**
   * Checks the foreground color of the component. <p/>
   * The color can be given in either hexadecimal ("FF45C0") or human-readable ("red") format.
   *
   * @see <a href="http://www.uispec4j.org/colors">Using colors</a>
   */
  public Assertion foregroundEquals(final String expectedColor) {
    return new Assertion() {
      public void check() {
        Color foreground = getAwtComponent().getForeground();
        if (foreground == null) {
          foreground = Color.BLACK;
        }
        ColorUtils.assertEquals(expectedColor, foreground);
      }
    };
  }

  /**
   * Checks that the foreground color of the component is close to the given value. <p/>
   * The color can be given in either hexadecimal ("FF45C0") or human-readable ("red") format.
   *
   * @see <a href="http://www.uispec4j.org/colors">Using colors</a>
   */
  public Assertion foregroundNear(final String expectedColor) {
    return new Assertion() {
      public void check() {
        Color foreground = getAwtComponent().getForeground();
        if (foreground == null) {
          foreground = Color.BLACK;
        }
        ColorUtils.assertSimilar(expectedColor, foreground);
      }
    };
  }

  /**
   * Checks that the component has no background
   */
  public Assertion backgroundNotSet() {
    return new Assertion() {
      public void check() {
        Color background = getAwtComponent().getBackground();
        AssertAdapter.assertNull("Background should be null but is: " + ColorUtils.getColorDescription(background),
                                 background);
      }
    };
  }

  /**
   * Checks the background color of the component
   * The color can be given in either hexadecimal ("FF45C0") or human-readable ("red") format.
   *
   * @see <a href="http://www.uispec4j.org/colors">Using colors</a>
   */
  public Assertion backgroundEquals(final String expectedColor) {
    return new Assertion() {
      public void check() {
        ColorUtils.assertEquals(expectedColor, getAwtComponent().getBackground());
      }
    };
  }

  /**
   * Checks that the background color of the component is close to the given value. <p/>
   * The color can be given in either hexadecimal ("FF45C0") or human-readable ("red") format.
   *
   * @see <a href="http://www.uispec4j.org/colors">Using colors</a>
   */
  public Assertion backgroundNear(final String expectedColor) {
    return new Assertion() {
      public void check() {
        Color background = getAwtComponent().getBackground();
        if (background == null) {
          background = Color.BLACK;
        }
        ColorUtils.assertSimilar(expectedColor, background);
      }
    };
  }

  private String computeComponentName(Component component) {
    String name = component.getName();
    return (name == null) ? "" : name;
  }

  private boolean isPanelWithNoName(UIComponent component) {
    return ((component instanceof Panel) && computeComponentName(component.getAwtComponent()).equals(""));
  }

  public Panel getContainer() {
    Container parent = getAwtComponent().getParent();
    if (parent == null) {
      return null;
    }
    return new Panel(parent);
  }

  public Panel getContainer(String parentName) {
    Container parent = getAwtComponent().getParent();
    while (parent != null && !parentName.equalsIgnoreCase(parent.getName())) {
      parent = parent.getParent();
    }
    if (parent != null && parentName.equalsIgnoreCase(parent.getName())) {
      return new Panel(parent);
    }
    return null;
  }

  public AbstractUIComponent typeKey(Key key) {
    KeyUtils.enterKeys(getAwtComponent(), key);
    return this;
  }

  public AbstractUIComponent pressKey(Key key) {
    KeyUtils.pressKey(getAwtComponent(), key);
    return this;
  }

  public AbstractUIComponent releaseKey(Key key) {
    KeyUtils.releaseKey(getAwtComponent(), key);
    return this;
  }
}
