package org.globsframework.gui.splits;

import javax.swing.*;
import java.awt.*;

public class SplitsStylesTest extends SplitsTestCase {

  public void testTypeSelection() throws Exception {
    checkButtonStyle(
      "<styles>" +
      "  <style selector='button' foreground='#FF0000'/>" +
      "</styles>" +
      "<button ref='btn'/>");
  }

  public void testTypeAndClassSelection() throws Exception {
    checkButtonStyle(
      "<styles>" +
      "  <style selector='button.btnClass' foreground='#FF0000'/>" +
      "</styles>" +
      "<button ref='btn' styleClass='btnClass'/>");
  }

  public void testClassSelection() throws Exception {
    checkButtonStyle(
      "<styles>" +
      "  <style selector='.btnClass' foreground='#FF0000'/>" +
      "</styles>" +
      "<button ref='btn' styleClass='btnClass'/>");
  }

  public void testTypeAndNameSelection() throws Exception {
    checkButtonStyle(
      "<styles>" +
      "  <style selector='button#btn' foreground='#FF0000'/>" +
      "</styles>" +
      "<button ref='btn' styleClass='btnClass'/>");
  }

  public void testNameSelection() throws Exception {
    checkButtonStyle(
      "<styles>" +
      "  <style selector='#btn' foreground='#FF0000'/>" +
      "</styles>" +
      "<button ref='btn' styleClass='btnClass'/>");
  }

  public void testNoSelectorMatch() throws Exception {
    builder.add("btn", aButton);
    Color color = aButton.getForeground();
    JButton button1 = (JButton)parse(
      "<styles>" +
      "  <style selector='unknown' foreground='#FF0000'/>" +
      "</styles>" +
      "<button ref='btn'/>");
    assertEquals(color, button1.getForeground());
  }

  public void testLastSelectorOverridesThePrevious() throws Exception {
    checkButtonStyle(
      "<styles>" +
      "  <style selector='button#btn' foreground='blue'/>" +
      "  <style selector='.class' foreground='#FF0000'/>" +
      "</styles>" +
      "<button ref='btn' styleClass='btnClass'/>");
  }

  public void testContainmentFiltering() throws Exception {
    org.uispec4j.Panel panel = new org.uispec4j.Panel((Container)parse(
      "<styles>" +
      "  <style selector='.myPanel button' foreground='#FF0000'/>" +
      "</styles>" +
      "<column>" +
      "  <panel styleClass='myPanel'>" +
      "    <button name='btn1'/>" +
      "  </panel>" +
      "  <button name='btn2'/>" +
      "</column>"));
    assertThat(panel.getButton("btn1").foregroundEquals("red"));
    assertThat(panel.getButton("btn2").foregroundEquals("black"));
  }

  public void testContainmentFilteringWithExtraLayers() throws Exception {
    org.uispec4j.Panel panel = new org.uispec4j.Panel((Container)parse(
      "<styles>" +
      "  <style selector='.myPanel button' foreground='#FF0000'/>" +
      "</styles>" +
      "<row>" +
      "  <panel styleClass='myPanel'>" +
      "    <column>" +
      "      <scrollPane>" +
      "        <button name='btn1' styleClass='btnClass'/>" +
      "      </scrollPane>" +
      "    </column>" +
      "  </panel>" +
      "  <button name='btn2' styleClass='btnClass'/>" +
      "</row>" +
      ""));
    assertThat(panel.getButton("btn1").foregroundEquals("red"));
    assertThat(panel.getButton("btn2").foregroundEquals("black"));
  }

  public void testSelectorsMustNotBeEmpty() throws Exception {
    checkParsingError(
      "<styles>" +
      "  <style selector='' foreground='#FF0000'/>" +
      "</styles>" +
      "<button name='btn1'/>",
      "A style selector cannot be empty");
  }

  private JButton checkButtonStyle(String xml) throws Exception {
    builder.add("btn", aButton);
    JButton button = (JButton)parse(xml);
    assertEquals(Color.RED, button.getForeground());
    return button;
  }

}
