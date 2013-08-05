package org.globsframework.gui.splits;

import java.awt.*;
import java.util.Properties;

public class SplitsConfiguredParametersTest extends SplitsTestCase {
  public void test() throws Exception {

    Properties properties = new Properties();
    properties.setProperty("button.visible", "false");
    configuredPropertiesService.apply(properties);

    org.uispec4j.Panel panel = new org.uispec4j.Panel((Container)parse(
      "<row>" +
      "  <button name='btn1' visible='${button.visible}' styleClass='btnClass'/>" +
      "</row>"));

    assertFalse(panel.getButton("btn1").isVisible());

    Properties newProperties = new Properties();
    newProperties.setProperty("button.visible", "true");
    configuredPropertiesService.apply(newProperties);
    assertTrue(panel.getButton("btn1").isVisible());
  }
}
