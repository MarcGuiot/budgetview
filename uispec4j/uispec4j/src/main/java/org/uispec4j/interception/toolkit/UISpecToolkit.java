package org.uispec4j.interception.toolkit;

import org.uispec4j.UISpec4J;
import sun.awt.LightweightFrame;
import sun.awt.datatransfer.DataTransferer;

import javax.swing.*;
import java.awt.*;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.peer.*;

/**
 * Mock toolkit used for intercepting displayed frames and dialogs.<p>
 * You can set it up by calling the {@link #setUp()} method.
 *
 * @see <a href="http://www.uispec4j.org/intercepting-windows">Intercepting windows</a>
 */
public class UISpecToolkit extends ToolkitDelegate {
  static final String SYSTEM_PROPERTY = "awt.toolkit";
  static final String UNIX_SYSTEM_DEFAULT_VALUE = "sun.awt.motif.MToolkit";
  static final String WINDOWS_SYSTEM_DEFAULT_VALUE = "sun.awt.windows.WToolkit";

  private static String awtToolkit;

  public UISpecToolkit() {
    setUp();
  }

  /**
   * @see UISpec4J#init
   * @deprecated Do not call this one directly anymore - use {@link UISpec4J#init} instead
   */
  public static void setUp() {
    if (underlyingToolkit != null) {
      return;
    }
    awtToolkit = System.getProperty(SYSTEM_PROPERTY);
    if (awtToolkit == null) {
      setAwtToolkitProperty();
    }
    buildUnderlyingToolkit(awtToolkit);
    System.setProperty(SYSTEM_PROPERTY, UISpecToolkit.class.getName());
  }

  /**
   * Sets the <code>awt.toolkit</code> to its initial value.
   * <p>This method will only work properly if the toolkit has not yet been instanciated by Swing.
   */
  public static void restoreAwtToolkit() {
    System.setProperty(SYSTEM_PROPERTY, awtToolkit);
  }

  public static UISpecToolkit instance() {
    Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
    if (!(defaultToolkit instanceof UISpecToolkit)) {
      fail("You must call UISpec4J.init() before using it");
    }
    return (UISpecToolkit)defaultToolkit;
  }

  protected LightweightPeer createComponent(Component target) {
    if (target instanceof JPopupMenu) {
      UISpecDisplay.instance().setCurrentPopup((JPopupMenu)target);
    }
    return super.createComponent(target);
  }

  public FramePeer createFrame(Frame target) {
    return new UISpecFramePeer(target);
  }

  public FramePeer createLightweightFrame(LightweightFrame lightweightFrame) throws HeadlessException {
    return  new UISpecFramePeer(lightweightFrame);
  }

  public DialogPeer createDialog(Dialog target) throws HeadlessException {
    if (!(target instanceof JDialog)) {
      throw new InterceptionInternalError("Dialogs of type '"
                                          + target.getClass().getName() + "' are not supported.");
    }
    return new UISpecDialogPeer((JDialog)target);
  }

  // java 1.6

//  protected DesktopPeer createDesktopPeer(Desktop target) throws HeadlessException {
//    return null;
//  }
//
//  public TrayIconPeer createTrayIcon(TrayIcon icon) throws HeadlessException, AWTException {
//    return null;
//  }
//
//  public SystemTrayPeer createSystemTray(SystemTray tray) {
//    return null;
//  }
//
//  public boolean isTraySupported() {
//    return false;
//  }

  public WindowPeer createWindow(Window target) throws HeadlessException {
    return new UISpecWindowPeer(target);
  }

  public CanvasPeer createCanvas(Canvas target) {
    return Empty.NULL_CANVAS_PEER;
  }

  public PanelPeer createPanel(Panel target) {
    return Empty.NULL_PANEL_PEER;
  }

  public RobotPeer createRobot(Robot robot, GraphicsDevice device) throws AWTException, HeadlessException {
    return Empty.NULL_ROBOT;
  }

  public DataTransferer getDataTransferer() {
    return Empty.NULL_DATA_TRANSFERER;
  }

  public KeyboardFocusManagerPeer getKeyboardFocusManagerPeer() throws HeadlessException {
    return null;
  }

  public KeyboardFocusManagerPeer createKeyboardFocusManagerPeer(KeyboardFocusManager manager) throws HeadlessException {
    return null;
  }

  protected int getScreenWidth() {
    return 0;
  }

  protected int getScreenHeight() {
    return 0;
  }

  protected MouseInfoPeer getMouseInfoPeer() {
    return Empty.NULL_MOUSE_INFO;
  }

  protected boolean syncNativeQueue() {
    return false;
  }

  public void grab(Window window) {
  }

  public void ungrab(Window window) {
  }

  public boolean isDesktopSupported() {
    return false;
  }

  public boolean isWindowOpacityControlSupported() {
    return false;
  }

  public boolean isWindowShapingSupported() {
    return false;
  }

  public boolean isWindowTranslucencySupported() {
    return false;
  }

  protected boolean syncNativeQueue(long l) {
    return false;
  }

  private static void fail(String msg) {
    throw new InterceptionInternalError(msg);
  }

  private static void setAwtToolkitProperty() {
    try {
      Class.forName(WINDOWS_SYSTEM_DEFAULT_VALUE);
      awtToolkit = WINDOWS_SYSTEM_DEFAULT_VALUE;
    }
    catch (ClassNotFoundException e) {
      try {
        Class.forName(UNIX_SYSTEM_DEFAULT_VALUE);
        awtToolkit = UNIX_SYSTEM_DEFAULT_VALUE;
      }
      catch (ClassNotFoundException e1) {
        throw new AWTError("Unable to locate AWT Toolkit");
      }
    }
  }

  private static void buildUnderlyingToolkit(String awtToolkit) {
    try {
      underlyingToolkit = (Toolkit)Class.forName(awtToolkit).newInstance();
    }
    catch (Exception e) {
      throw new AWTError("Unable to load AWT Toolkit: " + awtToolkit + " - "
                         + e.getLocalizedMessage());
    }
  }

  public InputMethodDescriptor getInputMethodAdapterDescriptor() throws AWTException {
    return null;
  }
}
