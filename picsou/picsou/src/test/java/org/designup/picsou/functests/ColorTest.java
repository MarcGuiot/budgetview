package org.designup.picsou.functests;

import junit.framework.TestCase;
import org.designup.picsou.model.ColorTheme;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.IOException;
import java.util.SortedSet;

public class ColorTest extends TestCase {

  public void test() throws Exception {
    ColorTheme[] themes = ColorTheme.values();
    for (int i = 1; i < themes.length; i++) {
      SortedSet<Object> reference = load(themes[0]);
      System.out.println("ColorTest.test: " + reference);
      SortedSet<Object> underTest = load(themes[i]);
      reference.removeAll(underTest);
      TestUtils.checkEmpty(reference, "Missing entries in " + themes[i].getFilePath());
    }
  }

  private static SortedSet<Object> load(ColorTheme theme) throws IOException {
    return Files.loadPropertyKeys("/" + theme.getFilePath(), ColorTheme.class);
  }

}
