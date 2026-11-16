package com.budgetview.functests.utils;

import com.budgetview.desktop.Application;
import com.budgetview.desktop.utils.AppExit;
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
     * L'application quitte sur certaines erreurs (ShowDialogAndExitExceptionHandler, ExitAction...) :
     * dans les tests, cela tuerait la JVM de surefire et tous les tests suivants disparaîtraient du
     * rapport. Toutes ses sorties passent par AppExit, qu'on fait lever une exception à la place.
     */
    private static void preventApplicationExit() {
        AppExit.setHandler(new AppExit.Handler() {
            public void exit(int status) {
                throw new IllegalStateException("Sortie de l'application (code " + status + ") pendant un test");
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
