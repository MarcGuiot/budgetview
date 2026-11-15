package com.budgetview.functests.utils;

import com.budgetview.desktop.Application;
import com.budgetview.utils.Lang;
import org.globsframework.utils.Log;
import org.globsframework.utils.TestUtils;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.utils.DummyRepaintManager;

import java.io.File;
import java.util.Locale;

public abstract class FunctionalTestCase extends UISpecTestCase {

    static {
        TestUtils.clearTmpDir();
        Locale.setDefault(Lang.ROOT);
        UISpec4J.setWindowInterceptionTimeLimit(30000);
        UISpec4J.setAssertionTimeLimit(1000);
        DummyRepaintManager.init();
        preventApplicationExit();
    }

    /**
     * L'application appelle System.exit() sur certaines erreurs (ShowDialogAndExitExceptionHandler,
     * ExitAction...) : dans les tests, cela tuerait la JVM de surefire et tous les tests suivants
     * disparaîtraient du rapport. On transforme ces appels en exception ; surefire, lui,
     * doit pouvoir quitter normalement en fin de suite.
     */
    private static void preventApplicationExit() {
        System.setSecurityManager(new SecurityManager() {
            public void checkPermission(java.security.Permission perm) {
            }

            public void checkPermission(java.security.Permission perm, Object context) {
            }

            public void checkExit(int status) {
                // Swing appelle aussi checkExit() sans quitter (setDefaultCloseOperation(EXIT_ON_CLOSE)) :
                // on ne bloque que les vrais Runtime.exit() venant de l'application.
                boolean realExit = false;
                for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                    if (element.getClassName().equals("java.lang.Runtime") && element.getMethodName().equals("exit")) {
                        realExit = true;
                    }
                    else if (realExit && element.getClassName().startsWith("com.budgetview.desktop")) {
                        throw new SecurityException("System.exit(" + status + ") appelé par l'application pendant un test");
                    }
                }
            }
        });
    }

    protected void setUp() throws Exception {
        super.setUp();
        Log.enableDebug();
        // L'application force le français au démarrage depuis la 6.00 :
        // on lui demande de garder la langue choisie par le test.
        System.setProperty(Application.LANG_PROPERTY, "keep");
        Locale locale = getDefaultLocale();
        Locale.setDefault(locale);
        Lang.setLocale(locale);
        Log.reset();
//    AWTAutoShutdown.notifyToolkitThreadBusy();
    }

    protected Locale getDefaultLocale() {
        return Lang.ROOT;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
//    dumpMemUsage();
    }

    private void dumpMemUsage() {
        System.gc();
        long freeMem = Runtime.getRuntime().freeMemory();
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        System.out.println("FunctionalTestCase.tearDown " + freeMem + " " + maxMem + " " + totalMem);
    }

    protected static String createPrevaylerRepository() {
        String name = TestUtils.TMP_DIR + "/test_prevayler";
        File file = new File(name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return name;
    }

    public static String getUrl() {
        return createPrevaylerRepository();
    }
}
