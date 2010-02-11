package org.globsframework.gui.splits.utils;

import org.globsframework.utils.exceptions.ResourceAccessFailed;
import sun.security.action.GetPropertyAction;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;

public class GuiUtils {

  private static boolean debugModeEnabled;
  public static final boolean IS_MACOSX;
  public static final boolean IS_LINUX;
  public static final boolean IS_WINDOWS;
  public static final boolean IS_VISTA;
  public static final boolean IS_OPEN_JDK;

  private static final String MAC_PLATFORM_ID = "Mac OS X";
  private static final String LINUX_PLATFORM_ID = "Linux";
  private static final String WINDOWS_PLATFORM_ID = "Windows";

  static {
    String os = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
    IS_MACOSX = os.contains(MAC_PLATFORM_ID);
    IS_LINUX = os.contains(LINUX_PLATFORM_ID);
    IS_WINDOWS = os.contains(WINDOWS_PLATFORM_ID);
    IS_VISTA = IS_WINDOWS && os.toLowerCase().contains("vista");

    String vm = (String)AccessController.doPrivileged(new GetPropertyAction("java.vm.name"));
    IS_OPEN_JDK = vm.contains("OpenJDK");
  }

  public static void addShortcut(JRootPane rootPane, String command, Action action) {
    KeyStroke stroke = KeyStroke.getKeyStroke(command);
    addShortcut(rootPane, command, action, stroke);
  }

  public static void addShortcut(JRootPane rootPane, String command, Action action, KeyStroke stroke) {
    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(stroke, command);
    rootPane.getActionMap().put(command, action);
  }

  public static void removeShortcut(JRootPane rootPane, String command, KeyStroke stroke) {
    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.remove(stroke);
    rootPane.getActionMap().remove(command);
  }

  public static void drawUnderlineCharAt(Graphics g, String text, int underlinedIndex, int x, int y) {
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

  public static boolean isMacOSX() {
    return IS_MACOSX;
  }

  public static boolean isLinux() {
    return IS_LINUX;
  }

  public static boolean isOpenJDK() {
    return IS_OPEN_JDK;
  }

  public static boolean isWindows() {
    return IS_WINDOWS;
  }

  public static boolean isVista() {
    return IS_VISTA;
  }

  public static int getCtrlModifier() {
    return isMacOSX() ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK;
  }

  public static KeyStroke ctrl(int key) {
    return KeyStroke.getKeyStroke(key, GuiUtils.getCtrlModifier());
  }

  public static void copyTextToClipboard(String text) {
    StringSelection selection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, selection);
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

  public static JComponent getEnclosingValidateRoot(Component component) {
    return (JComponent)getEnclosingComponent(component, new ComponentMatcher() {
      public boolean matches(Component component) {
        return JComponent.class.isInstance(component) && ((JComponent)component).isValidateRoot();
      }
    });
  }

  public static void revalidate(final JComponent component) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        component.revalidate();
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
      Insets screenInsets = getScreenInsets(window);
      origin = new Point(screenInsets.left,  screenInsets.top);
      parentSize = getMaxSize(window);
    }

    Dimension windowSize = window.getSize();
    window.setLocation(origin.x + parentSize.width / 2 - windowSize.width / 2,
                       origin.y + parentSize.height / 2 - windowSize.height / 2);
  }

  public static void setSizeWithinScreen(JFrame frame, int preferredWidth, int preferredHeight) {
    Dimension screenSize = getMaxSize(frame);
    frame.setSize(new Dimension(Math.min(preferredWidth, screenSize.width),
                                Math.min(preferredHeight, screenSize.height)));
  }

  private static Dimension getMaxSize(Window frame) {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    GraphicsConfiguration config = frame.getGraphicsConfiguration();
    Dimension availableScreenSize = toolkit.getScreenSize();
    Insets insets = toolkit.getScreenInsets(config);
    availableScreenSize.width -= (insets.left + insets.right);
    availableScreenSize.height -= (insets.top + insets.bottom);
    return availableScreenSize;
  }

  private static Insets getScreenInsets(Window window) {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    GraphicsConfiguration config = window.getGraphicsConfiguration();
    return toolkit.getScreenInsets(config);
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

  public static JEditorPane createReadOnlyHtmlComponent() {
    JEditorPane editorPane = new JEditorPane();
    initReadOnlyHtmlComponent(editorPane);
    return editorPane;
  }

  public static JEditorPane createReadOnlyHtmlComponent(String text) {
    JEditorPane editorPane = createReadOnlyHtmlComponent();
    editorPane.setText(text);
    return editorPane;
  }

  public static JEditorPane initReadOnlyHtmlComponent(JEditorPane editorPane) {
    editorPane.setEditable(false);
    editorPane.setOpaque(false);
    initHtmlComponent(editorPane);
    return editorPane;
  }

  public static void initHtmlComponent(JEditorPane editorPane) {
    editorPane.setContentType("text/html");
    editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    HTMLEditorKit kit = (HTMLEditorKit)editorPane.getEditorKit();
    StyleSheet css = kit.getStyleSheet();
    css.addRule("h1 { font-size:16;font-weight:bold; }");
    css.addRule("h2 { font-size:14;font-weight:bold; }");
    css.addRule("p  { margin-top:4px; margin-bottom:4px;margin-left:0;margin-right:0;}");
    css.addRule("table { border:none; }");
    css.addRule("td { vertical-align:top;}");
  }

  public static void loadCssResource(String path, JEditorPane htmlEditor, Class referenceClass) {
    InputStream is = referenceClass.getResourceAsStream(path);
    if (is == null) {
      return;
    }

    HTMLEditorKit editorKit = (HTMLEditorKit)htmlEditor.getEditorKit();
    StyleSheet styleSheet= editorKit.getStyleSheet();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      styleSheet.loadRules(reader, null);
      reader.close();
    }
    catch (IOException e) {
      throw new ResourceAccessFailed("Couldn't find resource file: " + path, e);
    }
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
