package org.globsframework.gui.splits.font;

import junit.framework.TestCase;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;

public class FontsTest extends TestCase {
  public void testFontParsing() throws Exception {
    checkFont("Arial,italic,24", "Arial", Font.ITALIC, 24);
    if (System.getProperty("os.name").equalsIgnoreCase("Linux")) {
      checkFont("Times,bold,8", "Dialog", Font.BOLD, 8);
      checkFont("Courier,plain,12", "Monospaced", Font.PLAIN, 12);
    }
    else {
      checkFont("Times,bold,8", "Times", Font.BOLD, 8);
      checkFont("Courier,plain,12", "Courier", Font.PLAIN, 12);
    }
  }

  public void testFontParsingErrors() throws Exception {
    checkFontParsingError("azeaze");
    checkFontParsingError("blah,blah,blah");
    checkFontParsingError("blah,12,12");
    checkFontParsingError("Arial,blah,12", "Unknown font style 'blah' - should be one of plain|bold|italic");
  }

  public void testFontLocator() throws Exception {
    FontService service = new FontService();
    Font font = Fonts.parseFont("Arial,plain,10");
    service.set("font1", font);
    assertEquals(font, Fonts.parseFont("$font1", service));
    assertEquals(font, Fonts.parseFont("Arial,plain,10"));
  }

  public void testMissingFontLocator() throws Exception {
    try {
      Fonts.parseFont("$font", null);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("Cannot resolve font '$font' with no FontLocator", e.getMessage());
    }
  }
  
  private void checkFont(String desc, String fontName, int style, int size) {
    Font font = Fonts.parseFont(desc);
    checkFont(font, fontName, style, size);
  }

  public static void checkFont(Font font, String fontName, int style, int size) {
    assertEquals(fontName, font.getFamily());
    assertEquals(style, font.getStyle());
    assertEquals(size, font.getSize());
  }

  private void checkFontParsingError(String desc) {
    checkFontParsingError(desc, Fonts.FONT_ERROR_MESSAGE);
  }

  private void checkFontParsingError(String desc, String message) {
    try {
      Fonts.parseFont(desc);
      fail();
    }
    catch (InvalidFormat e) {
      assertEquals(message, e.getMessage());
    }
  }
}
