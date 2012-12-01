package org.globsframework.gui.splits.utils;

import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import sun.security.action.GetPropertyAction;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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

  public static void selectAndRequestFocus(JTextField editor) {
    editor.requestFocusInWindow();
    editor.selectAll();
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

  public static <T> T getEnclosingComponent(Component component, final Class<T> containerClass, final String name) {
    return (T)getEnclosingComponent(component, new ComponentMatcher() {
      public boolean matches(Component component) {
        return containerClass.isInstance(component) && Utils.equal(name, component.getName());
      }
    });
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

  public static JDialog getEnclosingDialog(Component component) {
    return (JDialog)getEnclosingComponent(component, new ComponentMatcher() {
      public boolean matches(Component component) {
        return JDialog.class.isInstance(component);
      }
    });
  }

  public static JFrame getEnclosingWindow(Component component) {
    return (JFrame)getEnclosingComponent(component, new ComponentMatcher() {
      public boolean matches(Component component) {
        return Window.class.isInstance(component);
      }
    });
  }

  public static boolean isChild(Container container, Component child) {
    for (Component parent = child; parent != null; parent = parent.getParent()) {
      if (parent.equals(container)) {
        return true;
      }
    }
    return false;
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

    Dimension maxSize = getMaxSize(window);
    Container parent = window.getParent();
    if ((parent != null) && parent.isShowing()) {
      origin = parent.getLocationOnScreen();
      parentSize = parent.getSize();
    }
    else {
      Insets screenInsets = getScreenInsets(window);
      origin = new Point(screenInsets.left, screenInsets.top);
      parentSize = maxSize;
    }

    Dimension windowSize = window.getSize();
    if (windowSize.width > maxSize.width){
      windowSize.width = maxSize.width;
    }
    if (windowSize.height > maxSize.height){
      windowSize.height = maxSize.height;
    }
    window.setBounds(origin.x + parentSize.width / 2 - windowSize.width / 2,
                     origin.y + parentSize.height / 2 - windowSize.height / 2,
                     windowSize.width, windowSize.height);
  }

  public static void showFullSize(Window window) {
    Dimension screenSize = getMaxSize(window);
    window.setSize(screenSize);
    window.setVisible(true);
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
    css.addRule("h2 { font-size:14;font-weight:bold; margin-bottom:0px; }");
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
    StyleSheet styleSheet = editorKit.getStyleSheet();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      styleSheet.loadRules(reader, null);
      reader.close();
    }
    catch (IOException e) {
      throw new ResourceAccessFailed("Couldn't find resource file: " + path, e);
    }
  }

  public static void scrollToTop(JScrollPane scrollPane) {
    final JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
    verticalScrollBar.setValue(verticalScrollBar.getMinimum());
  }

  public static void scrollToTop(JEditorPane editor) {
    editor.setSelectionStart(0);
    editor.setSelectionEnd(0);
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

  public static String toString(JComponent component) {
    if (component == null) {
      return null;
    }
    StringBuilder builder = new StringBuilder();
    String name = component.getClass().getSimpleName();
    builder.append(name);
    builder.append("['");
    builder.append(component.getName());
    builder.append("'");

    String text = getText(component);
    if (Strings.isNotEmpty(text)) {
      builder.append("', '").append(text).append("'");
    }
    builder.append("]");

    return builder.toString();
  }

  public static String getText(JComponent component) {

    if (component instanceof JLabel) {
      return ((JLabel)component).getText();
    }
    else if (component instanceof AbstractButton) {
      return ((AbstractButton)component).getText();
    }
    return "";
  }
  
  public static boolean isRightClick(MouseEvent event) {
    int modifiers = event.getModifiers();
    return (modifiers & MouseEvent.BUTTON3_MASK) != 0;
  }

  public static Icon scaleDown(Icon icon, int maxWidth, int maxHeight) {
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    if (maxWidth > w && maxHeight > h) {
      return icon;
    }

    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = (Graphics2D)image.getGraphics();
    icon.paintIcon(null, g2d, 0, 0);
    g2d.dispose();

    double hRatio = (double)maxWidth / (double)w;
    double vRatio = (double)maxHeight / (double)h;
    double ratio = Math.min(hRatio, vRatio);

    double targetWidth = ratio * w;
    double targetHeight = ratio * h;

    final BufferedImage scaledImage =
      getScaledInstance(image, (int)targetWidth, (int)targetHeight,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);

    return new Icon() {
      public void paintIcon(Component component, Graphics graphics, int x, int y) {
        graphics.drawImage(scaledImage, x, y, null);
      }

      public int getIconWidth() {
        return scaledImage.getWidth();
      }

      public int getIconHeight() {
        return scaledImage.getHeight();
      }
    };
  }

  /**
   * From http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
   * Convenience method that returns a scaled instance of the
   * provided {@code BufferedImage}.
   *
   * @param img           the original image to be scaled
   * @param targetWidth   the desired width of the scaled instance,
   *                      in pixels
   * @param targetHeight  the desired height of the scaled instance,
   *                      in pixels
   * @param hint          one of the rendering hints that corresponds to
   *                      {@code RenderingHints.KEY_INTERPOLATION} (e.g.
   *                      {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
   *                      {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
   *                      {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
   * @param higherQuality if true, this method will use a multi-step
   *                      scaling technique that provides higher quality than the usual
   *                      one-step technique (only useful in downscaling cases, where
   *                      {@code targetWidth} or {@code targetHeight} is
   *                      smaller than the original dimensions, and generally only when
   *                      the {@code BILINEAR} hint is specified)
   * @return a scaled version of the original {@code BufferedImage}
   */
  private static BufferedImage getScaledInstance(BufferedImage img,
                                                 int targetWidth,
                                                 int targetHeight,
                                                 Object hint,
                                                 boolean higherQuality) {
    int type = (img.getTransparency() == Transparency.OPAQUE) ?
               BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
    BufferedImage ret = img;
    int w, h;
    if (higherQuality) {
      // Use multi-step technique: start with original size, then
      // scale down in multiple passes with drawImage()
      // until the target size is reached
      w = img.getWidth();
      h = img.getHeight();
    }
    else {
      // Use one-step technique: scale directly from original
      // size to target size with a single drawImage() call
      w = targetWidth;
      h = targetHeight;
    }

    do {
      if (higherQuality && w > targetWidth) {
        w /= 2;
        if (w < targetWidth) {
          w = targetWidth;
        }
      }

      if (higherQuality && h > targetHeight) {
        h /= 2;
        if (h < targetHeight) {
          h = targetHeight;
        }
      }

      BufferedImage tmp = new BufferedImage(w, h, type);
      Graphics2D g2 = tmp.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
      g2.drawImage(ret, 0, 0, w, h, null);
      g2.dispose();

      ret = tmp;
    }
    while (w > targetWidth || h > targetHeight);

    return ret;
  }
}
