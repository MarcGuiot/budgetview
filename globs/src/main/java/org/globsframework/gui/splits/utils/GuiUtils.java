package org.globsframework.gui.splits.utils;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GuiUtils {

  private static boolean debugModeEnabled;

  public static void addShortcut(JRootPane rootPane, String command, Action action) {
    KeyStroke stroke = KeyStroke.getKeyStroke(command);
    addShortcut(rootPane, command, action, stroke);
  }

  public static void addShortcut(JRootPane rootPane, String command, Action action, KeyStroke stroke) {
    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(stroke, command);
    rootPane.getActionMap().put(command, action);
  }

  public static void drawStringUnderlineCharAt(Graphics g, String text, int underlinedIndex, int x, int y) {
    g.drawString(text, x, y);
    if (underlinedIndex >= 0 && underlinedIndex < text.length()) {
      // PENDING: this needs to change.
      FontMetrics fm = g.getFontMetrics();
      int underlineRectX = x + fm.stringWidth(text.substring(0, underlinedIndex));
      int underlineRectY = y;
      int underlineRectWidth = fm.charWidth(text.charAt(underlinedIndex));
      int underlineRectHeight = 1;
      g.fillRect(underlineRectX, underlineRectY + 1, underlineRectWidth, underlineRectHeight);
    }
  }

  static {
    debugModeEnabled = "true".equalsIgnoreCase(System.getProperty("splits.debug.enabled"));
    if (debugModeEnabled) {
      ToolTipManager.sharedInstance().setDismissDelay(1000000);
    }
  }

  public static interface ComponentMatcher {
    boolean matches(Component component);
  }

  private GuiUtils() {
  }

  public static void pressKey(Component component, int keyCode, int modifier) {
    if (component.getKeyListeners().length > 0) {
      KeyEvent event = new KeyEvent(component, KeyEvent.KEY_PRESSED, 0, modifier, keyCode, (char)keyCode);
      for (int i = 0; i < component.getKeyListeners().length; i++) {
        KeyListener keyListener = component.getKeyListeners()[i];
        keyListener.keyPressed(event);
      }
    }

    if (JComponent.class.isInstance(component)) {
      KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifier);
      ActionListener actionForKeyStroke = ((JComponent)component).getActionForKeyStroke(keyStroke);
      if (actionForKeyStroke != null) {
        actionForKeyStroke.actionPerformed(new ActionEvent(component, KeyEvent.KEY_PRESSED, ""));
      }
    }
  }

  public static <T> T getEnclosingComponent(Component component, final Class<T> containerClass) {
    return (T)getEnclosingComponent(component, new ComponentMatcher() {
      public boolean matches(Component component) {
        return containerClass.isInstance(component);
      }
    });
  }

  public static JFrame getEnclosingFrame(Component component) {
    return (JFrame)getEnclosingComponent(component, new ComponentMatcher() {
      public boolean matches(Component component) {
        return JFrame.class.isInstance(component);
      }
    });
  }

  public static Component getEnclosingComponent(Component component, ComponentMatcher matcher) {
    for (Component parent = component; parent != null; parent = parent.getParent()) {
      if (matcher.matches(parent)) {
        return parent;
      }
    }
    return null;
  }

  public static void show(JPanel panel) {
    JFrame frame = new JFrame();
    frame.setContentPane(panel);
    frame.pack();
    frame.setVisible(true);
  }

  public static void show(JComponent component) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(component, BorderLayout.CENTER);
    show(panel);
  }

  /**
   * Shows the window centered on its parent window, or the screen if there is no parent window.
   */
  public static void showCentered(Window window) {
    center(window);
    window.setVisible(true);
  }

  /**
   * Centers the window on its parent window, or the screen if there is no parent window.
   */
  public static void center(Window window) {
    Point origin;
    Dimension parentSize;

    Container parent = window.getParent();
    if ((parent != null) && parent.isShowing()) {
      origin = parent.getLocationOnScreen();
      parentSize = parent.getSize();
    }
    else {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      origin = new Point(0, 0);
      parentSize = toolkit.getScreenSize();
    }

    Dimension windowSize = window.getSize();
    window.setLocation(origin.x + parentSize.width / 2 - windowSize.width / 2,
                       origin.y + parentSize.height / 2 - windowSize.height / 2);
  }

  public static JFrame getFrame(ActionEvent e) {
    if ((e != null) && (e.getSource() instanceof Component)) {
      return getEnclosingFrame((Component)e.getSource());
    }
    return null;
  }

  public static boolean isDebugModeEnabled() {
    return debugModeEnabled;
  }

  public static Font getDefaultLabelFont() {
    Font ref = new JLabel().getFont();
    return ref.deriveFont(Font.PLAIN, ref.getSize());
  }

  public static void initHtmlComponent(JEditorPane editorPane) {
    HTMLEditorKit kit = (HTMLEditorKit)editorPane.getEditorKit();
    StyleSheet css = kit.getStyleSheet();
    css.addRule("H1 -");
    css.addRule("H1 { font-weight:bold; }");
    css.addRule("H2 { font-size:16; }");
    css.addRule("H2 { font-weight:bold; }");
    css.addRule("body, p, li { font:" + getDefaultLabelFont().getFamily() + "; }");
    css.addRule("body, p, li { font-size:" + (getDefaultLabelFont().getSize()) + "; }");
  }

  public static void runInSwingThread(Runnable runnable) {
    try {
      if (SwingUtilities.isEventDispatchThread()) {
        runnable.run();
      }
      else {
        SwingUtilities.invokeLater(runnable);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void opacify(Container container) {
    if (container instanceof JPanel) {
      ((JPanel)container).setOpaque(false);
    }
    Component[] components = container.getComponents();
    for (Component component : components) {
      if (component instanceof Container) {
        opacify((Container)component);
      }
    }
  }
}
