package org.uispec4j;

import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.dependency.InternalAssert;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Wrapper for JTextComponent/JLabel components.
 */
public class TextBox extends AbstractUIComponent {
  public static final String TYPE_NAME = "textBox";
  public static final Class[] SWING_CLASSES = {JTextComponent.class, JLabel.class};

  private Handler handler;

  public TextBox(JTextComponent textComponent) {
    this.handler = TextBoxHandlerForHtmlTextComponent.init(textComponent);
    if (handler == null) {
      this.handler = new TextBoxHandlerForRawTextComponent(textComponent);
    }
  }

  public TextBox(JLabel label) {
    this.handler = new TextBoxHandlerForLabel(label);
  }

  public String getDescriptionTypeName() {
    return TYPE_NAME;
  }

  public Component getAwtComponent() {
    return handler.getAwtComponent();
  }

  /**
   * Simulates pressing a key while the focus is in the text box.<br>
   * Warning: the default cursor position is 0.
   */
  public void pressKey(Key key) {
    handler.pressKey(key);
  }

  /**
   * Replaces the text box contents and simulates pressing the Enter key.
   */
  public void setText(String text) {
    handler.setText(text);
  }

  /**
   * Inserts text at the given position without pressing Enter.
   */
  public void insertText(String text, int position) {
    handler.insertText(text, position);
  }

  /**
   * Inserts text at the given position without pressing Enter.
   */
  public void appendText(String text) {
    handler.appendText(text);
  }

  /**
   * Clears the text without validating. Use <code>setText("")</code> to achieve the same effect with validation.
   */
  public void clear() {
    handler.clear();
  }

  public String getText() {
    return handler.getText();
  }

  public Assertion textIsEmpty() {
    return handler.textIsEmpty();
  }

  /**
   * Checks the displayed text in cases where HTML text is used. This is
   * different from {@link #textIsEmpty()} in that whitespaces, carriage return
   * and other formatting adjustments are ignored.
   */
  public Assertion htmlEquals(String html) {
    return handler.htmlEquals(html);
  }

  /**
   * Checks that the text box contains a number of substrings, in a given order.
   * This method is useful for checking key information in the displayed string,
   * without being too dependent on the actual wording.
   */
  public Assertion textContains(final String[] orderedTexts) {
    return new Assertion() {
      public void check() {
        String actual = handler.getText();
        int index = 0;
        for (String text : orderedTexts) {
          index = actual.indexOf(text, index);
          if (index < 0) {
            if (actual.indexOf(text) < 0) {
              InternalAssert.fail("The component text does not contain '" + text + "' " +
                                  "- actual content is:" + actual);
            }
            else {
              InternalAssert.fail("The component text does not contain '" + text + "' at the expected position " +
                                  "- actual content is:" + actual);
            }
          }
          else {
            index += text.length();
          }
        }
      }
    };
  }

  public Assertion textEquals(String text) {
    return handler.textEquals(text);
  }

  public Assertion textContains(String text) {
    return handler.textContains(text);
  }

  public Assertion textDoesNotContain(String text) {
    return handler.textDoesNotContain(text);
  }

  public Assertion isEditable() {
    return handler.isEditable();
  }

  /**
   * Checks the icon displayed by the component. Please note that equals()
   * not being defined for Icon implementations, you will have to provide a pointer
   * to the actual Icon instance that is used in the production code. This make
   * this method mostly suited to unit testing.
   */
  public Assertion iconEquals(Icon icon) {
    return handler.iconEquals(icon);
  }

  /**
   * Simulates a click on an hyperlink given a part of the link text.
   * An exception will be thrown if zero or more than one hyperlinks are
   * found with this text.
   */
  public void clickOnHyperlink(String link) {
    handler.clickOnHyperlink(link);
  }

  /**
   * @see #clickOnHyperlink(String)
   */
  public Trigger triggerClickOnHyperlink(final String name) {
    return new Trigger() {
      public void run() throws Exception {
        clickOnHyperlink(name);
      }
    };
  }

  interface Handler {
    Component getAwtComponent();

    void setText(String text);

    void insertText(String text, int position);

    void appendText(String text);

    void clear();

    String getText();

    Assertion textIsEmpty();

    Assertion textEquals(String text);

    Assertion textContains(String text);

    Assertion textDoesNotContain(String text);

    Assertion isEditable();

    void clickOnHyperlink(String link);

    void pressKey(Key key);

    Assertion iconEquals(Icon icon);

    Assertion htmlEquals(String html);
  }
}
