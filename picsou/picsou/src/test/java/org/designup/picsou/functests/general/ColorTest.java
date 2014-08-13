package org.designup.picsou.functests.general;

import junit.framework.TestCase;
import org.designup.picsou.model.ColorTheme;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.IOException;
import java.util.SortedSet;

public class ColorTest extends TestCase {

  public void test() throws Exception {

    fail("RM: en cours de redesign V4");

    ColorTheme[] themes = ColorTheme.values();
    for (int i = 1; i < themes.length; i++) {
      SortedSet<Object> reference = load(themes[0]);
      SortedSet<Object> underTest = load(themes[i]);
      reference.removeAll(underTest);
      TestUtils.checkEmpty(reference, "Missing entries in " + themes[i].getColorFilePath());
    }
  }

  private static SortedSet<Object> load(ColorTheme theme) throws IOException {
    return Files.loadPropertyKeys("/" + theme.getColorFilePath(), ColorTheme.class);
  }

}
