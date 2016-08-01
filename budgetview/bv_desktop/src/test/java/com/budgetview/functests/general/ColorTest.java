package com.budgetview.functests.general;

import com.budgetview.model.ColorTheme;
import junit.framework.TestCase;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.SortedSet;

public class ColorTest extends TestCase {

  @Test
  public void test() throws Exception {
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
