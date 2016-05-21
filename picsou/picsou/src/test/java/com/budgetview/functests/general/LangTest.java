package com.budgetview.functests.general;

import com.budgetview.utils.Lang;
import junit.framework.TestCase;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.IOException;
import java.util.SortedSet;

public class LangTest extends TestCase {

  private SortedSet<Object> root;
  private SortedSet<Object> french;
  private SortedSet<Object> english;

  public void setUp() throws Exception {
    root = load("/i18n/lang.properties");
    french = load("/i18n/lang_fr.properties");
    english = load("/i18n/lang_en.properties");
  }

  public void testFrenchDictionnaryIsComplete() throws Exception {
    root.removeAll(french);
    TestUtils.checkEmpty(root, "Missing entries in fr");
  }

  public void testEnglishDictionnaryIsComplete() throws Exception {
    root.removeAll(english);
    TestUtils.checkEmpty(root, "Missing entries in en");
  }

  private static SortedSet<Object> load(String fileName) throws IOException {
    return Files.loadPropertyKeys(fileName, Lang.class);
  }
}
