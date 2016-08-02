package com.budgetview.desktop.utils;

import com.budgetview.desktop.startup.components.OpenFilesHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class MacOSXHooks implements InvocationHandler {

  private static final MacOSXHooks instance = new MacOSXHooks();

  private Action aboutAction;
  private Action preferencesAction;
  private Action exitAction;
  private Directory directory;
  private final List<OpenFilesHandler> openFilesHandlers = new ArrayList<>();

  public static void install(Action aboutAction, Action preferencesAction, Action exitAction, Directory directory) {
    if (!GuiUtils.isMacOSX()) {
      return;
    }
    instance.run(aboutAction, preferencesAction, exitAction, directory);
  }

  public void run(Action aboutAction, Action preferencesAction, Action exitAction, Directory directory) {
    this.aboutAction = aboutAction;
    this.preferencesAction = preferencesAction;
    this.exitAction = exitAction;
    this.directory = directory;
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
        if (args[0] != null) {
          try {
            Object oFiles = args[0].getClass().getMethod("getFiles").invoke(args[0]);
            if (oFiles instanceof List) {
              List<File> files = (List<File>) oFiles;
              synchronized (openFilesHandlers) {
                for (OpenFilesHandler handler : openFilesHandlers) {
                  handler.run(files);
                }
              }
            }
          }
          catch (ReflectiveOperationException | SecurityException | IllegalArgumentException ex) {
            System.out.println("MacOSXHooks.invoke: Failed to access open files event: " + ex);
          }
        }
        break;
      case "handleQuitRequestWith":
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

  public static void addOpenDocumentListener(OpenFilesHandler handler) {
    synchronized (instance.openFilesHandlers) {
      instance.openFilesHandlers.add(handler);
    }
  }

  public static void removeOpenDocumentListener(OpenFilesHandler handler) {
    synchronized (instance.openFilesHandlers) {
      instance.openFilesHandlers.remove(handler);
    }
  }
}
