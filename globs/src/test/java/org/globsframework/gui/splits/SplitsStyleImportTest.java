package org.globsframework.gui.splits;

import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import javax.swing.*;
import java.awt.*;

public class SplitsStyleImportTest extends SplitsTestCase {
  public void testImportFromResource() throws Exception {
    JButton button = new JButton();
    builder.add("btn", button);
    builder.setSource(getClass(), "/splits/sampleImportFile.splits").load();
    assertEquals(Color.RED, button.getForeground());
  }

  public void testImportedResourceNotFound() throws Exception {
    try {
      builder.setSource(getClass(), "/unknown.splits").load();
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

    JButton button = new JButton();
    builder.add("btn", button);
    builder
      .setSource("<splits>" +
                 "  <styleImport file='" + styleFile + "'/>" +
                 "  <button ref='btn'/>" +
                 "</splits>")
      .load();
    assertEquals(Color.RED, button.getForeground());
  }

  public void testImportedFileNotFound() throws Exception {
    try {
      builder
        .setSource("<splits>" +
                   "  <styleImport file='/unknown.splits'/>" +
                   "  <button ref='btn'/>" +
                   "</splits>")
        .load();
      fail();
    }
    catch (SplitsException e) {
      checkExceptionCause(e, "Could not find file: /unknown.splits");
    }
  }
}
