package org.globsframework.gui.splits;

import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;

public class SplitsStyleImportTest extends SplitsTestCase {
  public void testImportFromResource() throws Exception {
    JButton button = new JButton();
    builder.add("btn", button);
    builder.parse(getClass(), "/splits/sampleImportFile.splits");
    assertEquals(Color.RED, button.getForeground());
  }

  public void testImportedResourceNotFound() throws Exception {
    try {
      builder.parse(getClass(), "/unknown.splits");
      fail();
    }
    catch (ResourceAccessFailed e) {
      assertEquals("Resource file '/unknown.splits' not found for class: " + getClass().getName(),
                   e.getMessage());
    }
  }

  public void testImportFromFile() throws Exception {
    String styleFile = TestUtils.getFileName(this, ".styles");
    Files.dumpStringToFile(styleFile,
                           "<splits>" +
                           "  <styles>" +
                           "    <style selector='button' foreground='red'/>\n" +
                           "  </styles>" +
                           "</splits>");

    String file = TestUtils.getFileName(this, ".splits");
    Files.dumpStringToFile(file,
                           "<splits>" +
                           "  <styleImport file='" + styleFile + "'/>" +
                           "  <button ref='btn'/>" +
                           "</splits>");

    JButton button = new JButton();
    builder.add("btn", button);
    builder.parse(new FileReader(file));
    assertEquals(Color.RED, button.getForeground());
  }

  public void testImportedFileNotFound() throws Exception {
    String file = TestUtils.getFileName(this, ".splits");
    Files.dumpStringToFile(file,
                           "<splits>" +
                           "  <styleImport file='/unknown.splits'/>" +
                           "  <button ref='btn'/>" +
                           "</splits>");

    try {
      builder.parse(new FileReader(file));
      fail();
    }
    catch (ItemNotFound e) {
      assertEquals("Could not find file: /unknown.splits", e.getMessage());
    }
  }
}
