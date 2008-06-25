package org.globsframework.gui.splits;

import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import javax.swing.*;
import java.io.FileReader;

public class SplitsReloadTest extends SplitsTestCase {
  public void test() throws Exception {
    JButton button1 = new JButton();
    JLabel label1 = new JLabel();
    builder.add("button1", button1);
    builder.add("label1", label1);

    String file1 = TestUtils.getFileName(this);
    Files.dumpStringToFile(file1,
                           "<splits>" +
                           "  <horizontalSplit>" +
                           "    <label ref='label1' text='label1Before'/>" +
                           "    <button ref='button1' text='button1Before'/>" +
                           "  </horizontalSplit>" +
                           "</splits>");

    JSplitPane splitPane = (JSplitPane)builder.doParse(new FileReader(file1));
    assertSame(label1, splitPane.getLeftComponent());
    assertEquals("label1Before", label1.getText());
    assertSame(button1, splitPane.getRightComponent());
    assertEquals("button1Before", button1.getText());

    String file2 = TestUtils.getFileName(this);
    Files.dumpStringToFile(file2,
                           "<splits>" +
                           "  <column>" +
                           "    <label ref='label1' text='label1After'/>" +
                           "    <button ref='button1' text='button1After'/>" +
                           "  </column>" +
                           "</splits>");

    JPanel panel = (JPanel)builder.doParse(new FileReader(file2));
    assertSame(label1, panel.getComponent(0));
    assertEquals("label1After", label1.getText());
    assertSame(button1, panel.getComponent(1));
    assertEquals("button1After", button1.getText());

    assertNull(splitPane.getRightComponent());
    assertNull(splitPane.getLeftComponent());
  }
}
