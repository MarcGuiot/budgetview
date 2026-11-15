package org.globsframework.gui.splits.font;

import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.uispec4j.UISpecTestCase;

import java.awt.*;

public class FontsTest extends UISpecTestCase {
  public void testFontParsing() throws Exception {
    checkFont("Arial,italic,24", "Arial", Font.ITALIC, 24);
    // Java remplace une police absente par « Dialog », ou « Monospaced » pour Courier : l'attente dépend des
    // polices installées, pas du système (ni Times ni Courier ne sont vues par le JDK 8 sur ce Mac ARM).
    checkFont("Times,bold,8", installedOr("Times", "Dialog"), Font.BOLD, 8);
    checkFont("Courier,plain,12", installedOr("Courier", "Monospaced"), Font.PLAIN, 12);
  }

  private static String installedOr(String family, String fallback) {
    for (String installed : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
      if (installed.equals(family)) {
        return family;
      }
    }
    return fallback;
  }

  public void testFontParsingErrors() throws Exception {
    checkFontParsingError("azeaze");
    checkFontParsingError("blah,blah,blah");
    checkFontParsingError("blah,12,12");
    checkFontParsingError("Arial,blah,12", "Unknown font style 'blah' - should be one of plain|bold|italic");
  }

  public void testDerivedFont() throws Exception {
    Font font = Fonts.parseFont("-,italic,44");
    assertEquals(Fonts.DEFAULT_LABEL_FONT.getFamily(), font.getFamily());
    assertEquals(Font.ITALIC, font.getStyle());
    assertEquals(44, font.getSize());
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
