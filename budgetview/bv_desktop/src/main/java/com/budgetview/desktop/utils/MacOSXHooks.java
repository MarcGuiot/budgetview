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

/**
 * Menus « À propos », « Préférences », « Quitter » de macOS et ouverture de fichiers par le Finder.
 * <p/>
 * Java 9 et plus : {@code java.awt.Desktop} et {@code java.awt.desktop.*}. Java 8 : {@code com.apple.eawt},
 * disparu en Java 9. Les deux API ont les mêmes noms de méthodes ({@code handleAbout}, {@code handlePreferences},
 * {@code handleQuitRequestWith}, {@code openFiles}), d'où un seul {@link InvocationHandler}. Tout passe par
 * réflexion pour que le code compile en cible Java 8.
 */
public class MacOSXHooks implements InvocationHandler {

  private static final MacOSXHooks instance = new MacOSXHooks();

  private Action aboutAction;
  private Action preferencesAction;
  private Action exitAction;
  private final List<OpenFilesHandler> openFilesHandlers = new ArrayList<OpenFilesHandler>();

  /**
   * Barre de menus en haut de l'écran, comme en 5.01 (c'est le PicsouMacLookAndFeel, désactivé depuis, qui
   * la demandait). À appeler avant la création de la première fenêtre. Pas dans les tests : uispec4j
   * remplace le toolkit et n'a pas de barre de menus native.
   */
  public static void useScreenMenuBar() {
    if (GuiUtils.isMacOSX()) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
  }

  public static void install(Action aboutAction, Action preferencesAction, Action exitAction, Directory directory) {
    if (!GuiUtils.isMacOSX()) {
      return;
    }
    instance.aboutAction = aboutAction;
    instance.preferencesAction = preferencesAction;
    instance.exitAction = exitAction;
    try {
      if (!instance.installDesktopHandlers()) {
        instance.installEawtHandlers();
      }
    }
    catch (Throwable ex) {
      System.out.println("Failed to register with OSX: " + ex);
    }
    enableOSXFullscreen(directory.get(JFrame.class));
  }

  /** Java 9+. Renvoie false si l'API n'existe pas (Java 8). */
  private boolean installDesktopHandlers() throws Exception {
    if (!isJava9OrLater()) {
      return false;
    }
    if (!Desktop.isDesktopSupported()) {
      System.out.println("OSX: java.awt.Desktop not supported");
      return true;
    }
    Desktop desktop = Desktop.getDesktop();
    setDesktopHandler(desktop, "APP_ABOUT", "setAboutHandler",
                      Class.forName("java.awt.desktop.AboutHandler"));
    setDesktopHandler(desktop, "APP_PREFERENCES", "setPreferencesHandler",
                      Class.forName("java.awt.desktop.PreferencesHandler"));
    setDesktopHandler(desktop, "APP_QUIT_HANDLER", "setQuitHandler",
                      Class.forName("java.awt.desktop.QuitHandler"));
    setDesktopHandler(desktop, "APP_OPEN_FILE", "setOpenFileHandler",
                      Class.forName("java.awt.desktop.OpenFilesHandler"));
    return true;
  }

  private void setDesktopHandler(Desktop desktop, String actionName, String setterName, Class<?> handlerClass)
    throws Exception {
    Desktop.Action action = Desktop.Action.valueOf(actionName);
    if (!desktop.isSupported(action)) {
      System.out.println("OSX: unsupported desktop action " + actionName);
      return;
    }
    Desktop.class.getMethod(setterName, handlerClass).invoke(desktop, createProxy(handlerClass));
  }

  /** Java 8. */
  private void installEawtHandlers() throws Exception {
    Class<?> application = Class.forName("com.apple.eawt.Application");
    Class<?> quitHandler = Class.forName("com.apple.eawt.QuitHandler");
    Class<?> aboutHandler = Class.forName("com.apple.eawt.AboutHandler");
    Class<?> openFilesHandler = Class.forName("com.apple.eawt.OpenFilesHandler");
    Class<?> preferencesHandler = Class.forName("com.apple.eawt.PreferencesHandler");
    Object appli = application.getConstructor((Class[])null).newInstance((Object[])null);
    Object listener = createProxy(quitHandler, aboutHandler, openFilesHandler, preferencesHandler);
    application.getDeclaredMethod("setQuitHandler", quitHandler).invoke(appli, listener);
    application.getDeclaredMethod("setAboutHandler", aboutHandler).invoke(appli, listener);
    application.getDeclaredMethod("setOpenFileHandler", openFilesHandler).invoke(appli, listener);
    application.getDeclaredMethod("setPreferencesHandler", preferencesHandler).invoke(appli, listener);
  }

  private Object createProxy(Class<?>... interfaces) {
    return Proxy.newProxyInstance(MacOSXHooks.class.getClassLoader(), interfaces, this);
  }

  /** Bouton vert de la fenêtre en plein écran. */
  public static void enableOSXFullscreen(Window window) {
    if (window == null) {
      return;
    }
    if (isJava9OrLater()) {
      // Lu par sun.lwawt.macosx.CPlatformWindow. com.apple.eawt.FullScreenUtilities existe encore,
      // mais java.desktop ne l'exporte plus.
      if (window instanceof RootPaneContainer) {
        ((RootPaneContainer)window).getRootPane().putClientProperty("apple.awt.fullscreenable", Boolean.TRUE);
      }
      return;
    }
    try {
      Class<?> fullScreenUtilities = Class.forName("com.apple.eawt.FullScreenUtilities");
      Method method = fullScreenUtilities.getDeclaredMethod("setWindowCanFullScreen", Window.class, boolean.class);
      method.invoke(fullScreenUtilities, window, Boolean.TRUE);
    }
    catch (Throwable e) {
      System.out.println("Failed to enable OSX fullscreen: " + e);
    }
  }

  private static boolean isJava9OrLater() {
    try {
      Class.forName("java.awt.desktop.AboutHandler");
      return true;
    }
    catch (ClassNotFoundException e) {
      return false;
    }
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String name = method.getName();
    if (method.getDeclaringClass() == Object.class) {
      if ("equals".equals(name)) {
        return proxy == args[0];
      }
      if ("hashCode".equals(name)) {
        return System.identityHashCode(proxy);
      }
      return "MacOSXHooks proxy";
    }
    if ("openFiles".equals(name)) {
      openFiles(method.getParameterTypes()[0], args[0]);
    }
    else if ("handleQuitRequestWith".equals(name)) {
      exitAction.actionPerformed(null);
      // ExitAction termine la JVM. Si elle revient (sortie déjà en cours, ou realExit=false dans les tests),
      // on annule la demande pour que macOS ne reste pas en attente d'une réponse.
      cancelQuit(method.getParameterTypes()[1], args[1]);
    }
    else if ("handleAbout".equals(name)) {
      aboutAction.actionPerformed(null);
    }
    else if ("handlePreferences".equals(name)) {
      preferencesAction.actionPerformed(null);
    }
    else {
      System.out.println("OSX unsupported method: " + name);
    }
    return null;
  }

  // Méthodes lues sur le type déclaré (public) : la classe réelle de l'objet peut être interne au JDK.
  private void openFiles(Class<?> eventType, Object event) {
    if (event == null) {
      return;
    }
    try {
      Object oFiles = eventType.getMethod("getFiles").invoke(event);
      if (oFiles instanceof List) {
        @SuppressWarnings("unchecked")
        List<File> files = (List<File>)oFiles;
        synchronized (openFilesHandlers) {
          for (OpenFilesHandler handler : openFilesHandlers) {
            handler.run(files);
          }
        }
      }
    }
    catch (Exception ex) {
      System.out.println("MacOSXHooks.invoke: Failed to access open files event: " + ex);
    }
  }

  private void cancelQuit(Class<?> responseType, Object quitResponse) {
    if (quitResponse == null) {
      return;
    }
    try {
      responseType.getMethod("cancelQuit").invoke(quitResponse);
    }
    catch (Exception ex) {
      System.out.println("MacOSXHooks: Failed to cancel quit: " + ex);
    }
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
