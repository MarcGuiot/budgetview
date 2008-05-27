package org.uispec4j.extension;

import org.uispec4j.TestUtils;
import org.uispec4j.utils.UnitTestCase;

import java.io.File;
import java.io.IOException;

public class ExtensionGeneratorTest extends UnitTestCase {
  private File output;

  protected void setUp() throws Exception {
    super.setUp();
    output = new File(findTargetDirectory(), "tmp/extension.jar");
    output.getParentFile().mkdirs();
    output.delete();
  }

  public void testStandardGenerationUsageWithCustomClass() throws Exception {
    checkStandardGenerationUsage(CustomCountingButton.class);
  }

  public void testStandardGenerationUsageWithDerivedClass() throws Exception {
    checkStandardGenerationUsage(DerivedCountingButton.class);
  }

  public void testRunningTheGenerationOverAnExistingJarReplacesThePanelClass() throws Exception {
    checkRunningTheGenerationOverAnExistingJarReplacesThePanelClass(CustomCountingButton.class);
    checkRunningTheGenerationOverAnExistingJarReplacesThePanelClass(DerivedCountingButton.class);
  }

  public void checkStandardGenerationUsage(Class componentClass) throws Exception {
    ExtensionGenerator.main(new String[]{output.getAbsolutePath(),
                                         "CountingButton:" + componentClass.getName() + ":"
                                         + JCountingButton.class.getName()});
    runCheckerClass(componentClass);
  }

  private void checkRunningTheGenerationOverAnExistingJarReplacesThePanelClass(Class componentClass) throws Exception {
    ExtensionGenerator.main(new String[]{output.getAbsolutePath(),
                                         "MyButton:" + componentClass.getName() + ":"
                                         + JCountingButton.class.getName()});
    ExtensionGenerator.main(new String[]{output.getAbsolutePath(),
                                         "CountingButton:" + componentClass.getName()
                                         + ":" + JCountingButton.class.getName()});
    runCheckerClass(componentClass);
  }

  private void runCheckerClass(Class componentClass) throws IOException, InterruptedException {
      String separator = System.getProperty("path.separator");
      String classpath = output.getAbsolutePath() +
         separator +
         findTargetDirectory() + "/classes" +
         separator +
         findTargetDirectory() + "/test-classes" +
      separator +
      System.getProperty("java.class.path");

    if ("Linux".equalsIgnoreCase(System.getProperty("os.name"))){
      System.out.println("to short " + classpath);
      return;
    }
    Process process =
      Runtime.getRuntime().exec(new String[]{"java",
                                             "-Xmx512m",
                                             "-classpath",
                                             classpath,
                                             GeneratedJarChecker.class.getName(),
                                             componentClass.getName()});
    StreamRecorder output = StreamRecorder.run(process.getInputStream());
    StreamRecorder error = StreamRecorder.run(process.getErrorStream());
    process.waitFor();

    // Workaround for Apple bug
    // http://developer.apple.com/releasenotes/Java/Java50Release4RN/OutstandingIssues/chapter_4_section_3.html
    if (TestUtils.isMacOsX()) {
      if (error.getResult().length() != 0) {
        assertEquals("_-_-_ _:_:_._ java[_] CFLog (_): CFMessagePort: bootstrap_register(): failed _ (_), port = _, name = 'java.ServiceProvider'\n" +
                     "See /usr/include/servers/bootstrap_defs.h for the error codes.\n" +
                     "_-_-_ _:_:_._ java[_] CFLog (_): CFMessagePortCreateLocal(): failed to name Mach port (java.ServiceProvider)",
                     error.getResult().replaceAll("[0-9][\\w]*", "_"));
      }
    }
    else {
      assertEquals("", error.getResult());
    }
    assertEquals("OK", output.getResult());
  }


    public File findTargetDirectory() {
        return findDirectory(getClass(), "target");
    }


    private static File findDirectory(Class baseClass, String directory) {
        String name = "/" + baseClass.getName().replace('.', '/') + ".class";
        String absolutePath = baseClass.getResource(name).getFile();
        return toDirectory(absolutePath, directory);
    }


    private static File toDirectory(String absolutePath, String directory) {
        String path = absolutePath.substring(0, absolutePath.lastIndexOf("target"));
        return new File(path, directory);
    }
}
