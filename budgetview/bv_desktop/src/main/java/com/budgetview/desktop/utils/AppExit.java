package com.budgetview.desktop.utils;

/**
 * Point de sortie unique de l'application : tout arrêt de la JVM décidé par l'application passe par
 * {@link #exit(int)} plutôt que par {@code System.exit}. Les tests remplacent le comportement par
 * {@link #setHandler(Handler)}, pour qu'une sortie ne tue pas la JVM de surefire (cela remplace le
 * {@code SecurityManager}, désactivé pour de bon en Java 24).
 */
public class AppExit {

  public interface Handler {
    void exit(int status);
  }

  public static final Handler SYSTEM_EXIT = new Handler() {
    public void exit(int status) {
      System.exit(status);
    }
  };

  private static volatile Handler handler = SYSTEM_EXIT;

  private AppExit() {
  }

  public static void exit(int status) {
    handler.exit(status);
  }

  public static void setHandler(Handler newHandler) {
    handler = newHandler != null ? newHandler : SYSTEM_EXIT;
  }
}
