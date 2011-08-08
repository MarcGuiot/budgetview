package org.designup.picsou.functests;

import junit.framework.TestCase;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;

import java.io.IOException;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

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
    checkEmpty(root, "Missing entries in fr");
  }

  private void checkEmpty(SortedSet<Object> keys, String message) {
    if (!keys.isEmpty()) {
      StringBuilder text = new StringBuilder();
      text.append(message);
      for (Object key : keys) {
        text.append("\n").append(key);
      }
      fail(text.toString());
    }
  }

  private SortedSet<Object> load(String fileName) throws IOException {
    Properties root = new Properties();
    root.load(Files.getStream(Lang.class, fileName));
    TreeSet<Object> result = new TreeSet<Object>();
    result.addAll(root.keySet());
    return result;
  }
}
