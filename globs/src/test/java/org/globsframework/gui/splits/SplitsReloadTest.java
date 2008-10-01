package org.globsframework.gui.splits;

import com.jidesoft.swing.JideSplitPane;

import javax.swing.*;

public class SplitsReloadTest extends SplitsTestCase {
  public void test() throws Exception {
    JButton button1 = new JButton();
    JLabel label1 = new JLabel();
    builder.add("button1", button1);
    builder.add("label1", label1);

    JideSplitPane splitPane = builder.setSource(
        "<splits>" +
        "  <horizontalSplit>" +
        "    <label ref='label1' text='label1Before'/>" +
        "    <button ref='button1' text='button1Before'/>" +
        "  </horizontalSplit>" +
        "</splits>")
        .load();
    assertEquals(2, splitPane.getPaneCount());
    assertSame(label1, splitPane.getPaneAt(0));
    assertEquals("label1Before", label1.getText());
    assertSame(button1, splitPane.getPaneAt(1));
    assertEquals("button1Before", button1.getText());

    JPanel panel = builder.setSource(
      "<splits>" +
      "  <column>" +
      "    <label ref='label1' text='label1After'/>" +
      "    <button ref='button1' text='button1After'/>" +
      "  </column>" +
      "</splits>")
      .load();
    assertSame(label1, panel.getComponent(0));
    assertEquals("label1After", label1.getText());
    assertSame(button1, panel.getComponent(1));
    assertEquals("button1After", button1.getText());

    assertEquals(0, splitPane.getPaneCount());
  }

  public void testContainedBuilders() throws Exception {
    builder.setSource("<splits>" +
                      "  <panel>" +
                      "    <component ref='subBuilder'/>" +
                      "  </panel>" +
                      "</splits>");

    SplitsBuilder subBuilder = SplitsBuilder.init(directory);
    subBuilder.setSource("<splits>" +
                         "  <button text='btn'/>" +
                         "</splits>");
    builder.add("subBuilder", subBuilder);

    JPanel firstPanel = builder.load();
    assertEquals(1, firstPanel.getComponentCount());
    assertTrue(firstPanel.getComponent(0) instanceof JButton);
    assertEquals("btn", ((JButton)firstPanel.getComponent(0)).getText());

    subBuilder.setSource("<splits>" +
                         "  <label text='lbl'/>" +
                         "</splits>");
    JPanel secondPanel = builder.load();
    assertEquals(1, secondPanel.getComponentCount());
    assertTrue(secondPanel.getComponent(0) instanceof JLabel);
    assertEquals("lbl", ((JLabel)secondPanel.getComponent(0)).getText());
  }

  public void testContainedBuildersWithAnonymousComponents() throws Exception {
    builder.setSource("<splits>" +
                      "  <panel>" +
                      "    <component ref='subBuilder'/>" +
                      "  </panel>" +
                      "</splits>");

    SplitsBuilder subBuilder = SplitsBuilder.init(directory);
    subBuilder.setSource("<splits>" +
                         "  <panel name='subBuilder'>" +
                         "    <button text='btn'/>" +
                         "  </panel>" +
                         "</splits>");
    builder.add("subBuilder", subBuilder);

    JPanel firstPanel = builder.load();
    JPanel firstSubPanel = (JPanel)firstPanel.getComponent(0);
    assertEquals(1, firstSubPanel.getComponentCount());
    assertEquals("btn", ((JButton)firstSubPanel.getComponent(0)).getText());

    subBuilder.setSource("<splits>" +
                         "  <panel name='subBuilder'>" +
                         "    <label text='lbl'/>" +
                         "  </panel>" +
                         "</splits>");
    JPanel secondPanel = builder.load();
    JPanel secondSubPanel = (JPanel)secondPanel.getComponent(0);
    assertEquals(1, secondSubPanel.getComponentCount());
    assertEquals("lbl", ((JLabel)secondSubPanel.getComponent(0)).getText());
  }
}
