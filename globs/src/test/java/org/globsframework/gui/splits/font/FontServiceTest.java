package org.globsframework.gui.splits.font;

import junit.framework.TestCase;

import java.awt.*;

import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.InvalidParameter;

public class FontServiceTest extends TestCase {

  private static Font font1 = Fonts.parseFont("Arial,italic,24");
  protected FontService service;

  protected void setUp() throws Exception {
    service = new FontService();
  }

  public void testGetSet() throws Exception {
    service.set("font1", font1);
    assertEquals(font1, service.get("font1"));
  }

  public void testNullKeysNotAllowed() throws Exception {
    try {
      service.get(null);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("empty keys are not allowed", e.getMessage());
    }

    try {
      service.set(null, null);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("empty keys are not allowed", e.getMessage());
    }
  }

  public void testKeyNotFound() throws Exception {
    try {
      service.get("unknown.key");
      fail();
    }
    catch (ItemNotFound e) {
      assertEquals("Font 'unknown.key' not found", e.getMessage());
    }
  }

  public void testLoad() throws Exception {
    service = new FontService(getClass(), "/splits/test_fonts.properties");
    assertEquals(Fonts.parseFont("Arial,bold,18"), service.get("font1"));
  }
}
