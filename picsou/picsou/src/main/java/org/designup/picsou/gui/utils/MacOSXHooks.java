package org.designup.picsou.gui.utils;

import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MacOSXHooks implements InvocationHandler {


  private final Action aboutAction;
  private final Action preferencesAction;
  private final Action exitAction;
  private final Directory directory;

  public static void install(Action aboutAction, Action preferencesAction, Action exitAction, Directory directory) {
    if (!GuiUtils.isMacOSX()) {
      return;
    }
    MacOSXHooks macOSXHooks = new MacOSXHooks(aboutAction, preferencesAction, exitAction, directory);
    macOSXHooks.run();
  }

  private MacOSXHooks(Action aboutAction, Action preferencesAction, Action exitAction, Directory directory) {

    this.aboutAction = aboutAction;
    this.preferencesAction = preferencesAction;
    this.exitAction = exitAction;
    this.directory = directory;
  }

  public void run() {
    try {
      Class<?> application = Class.forName("com.apple.eawt.Application");
      Class<?> quitHandler = Class.forName("com.apple.eawt.QuitHandler");
      Class<?> aboutHandler = Class.forName("com.apple.eawt.AboutHandler");
      Class<?> openFilesHandler = Class.forName("com.apple.eawt.OpenFilesHandler");
      Class<?> preferencesHandler = Class.forName("com.apple.eawt.PreferencesHandler");
      Object appli = application.getConstructor((Class[]) null).newInstance((Object[]) null);
      Object listener = Proxy.newProxyInstance(MacOSXHooks.class.getClassLoader(), new Class<?>[]{
        quitHandler, aboutHandler, openFilesHandler, preferencesHandler}, this);
      application.getDeclaredMethod("setQuitHandler", quitHandler).invoke(appli, listener);
      application.getDeclaredMethod("setAboutHandler", aboutHandler).invoke(appli, listener);
      application.getDeclaredMethod("setOpenFileHandler", openFilesHandler).invoke(appli, listener);
      application.getDeclaredMethod("setPreferencesHandler", preferencesHandler).invoke(appli, listener);
      enableOSXFullscreen(directory.get(JFrame.class));
    }
    catch (ReflectiveOperationException | SecurityException | IllegalArgumentException ex) {
      System.out.println("Failed to register with OSX: " + ex);
    }
  }

  public void enableOSXFullscreen(Window window) {
    if (window == null) {
      return;
    }
    try {
      // http://stackoverflow.com/a/8693890/2257172
      Class<?> fullScreenUtilities = Class.forName("com.apple.eawt.FullScreenUtilities");
      Method method = fullScreenUtilities.getDeclaredMethod("setWindowCanFullScreen", new Class[]{Window.class, boolean.class});
      method.invoke(fullScreenUtilities, window, Boolean.TRUE);
    }
    catch (Throwable e) {
      System.out.println("Failed to register with OSX: " + e);
    }
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    switch (method.getName()) {
      case "openFiles":
//        if (args[0] != null) {
//          try {
//            Object oFiles = args[0].getClass().getMethod("getFiles").invoke(args[0]);
//            if (oFiles instanceof List) {
//              Main.worker.submit(new OpenFileTask((List<File>) oFiles, null) {
//                @Override
//                protected void realRun() throws SAXException, IOException, OsmTransferException {
//                  // Wait for JOSM startup is advanced enough to load a file
//                  while (Main.parent == null || !Main.parent.isVisible()) {
//                    try {
//                      Thread.sleep(25);
//                    }
//                    catch (InterruptedException e) {
//                      Main.warn(e);
//                    }
//                  }
//                  super.realRun();
//                }
//              });
//            }
//          }
//          catch (ReflectiveOperationException | SecurityException | IllegalArgumentException ex) {
//            Main.warn("Failed to access open files event: " + ex);
//          }
//        }
        break;
      case "handleQuitRequestWith":
//        boolean closed = handleExit(false, 0);
//        if (args[1] != null) {
//          args[1].getClass().getDeclaredMethod(closed ? "performQuit" : "cancelQuit").invoke(args[1]);
//        }
        exitAction.actionPerformed(null);
        break;
      case "handleAbout":
        aboutAction.actionPerformed(null);
        break;
      case "handlePreferences":
        preferencesAction.actionPerformed(null);
        break;
      default:
        System.out.println("OSX unsupported method: " + method.getName());
    }
    return null;
  }

  public static void addOpenDocumentListener(AbstractAction action) {
  }

  public static void removeOpenDocumentListener(AbstractAction action) {
  }
}
