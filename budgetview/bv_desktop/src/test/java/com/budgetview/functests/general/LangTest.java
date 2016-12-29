package com.budgetview.functests.general;

import com.budgetview.desktop.description.Labels;
import com.budgetview.shared.model.DefaultSeries;
import com.budgetview.utils.Lang;
import junit.framework.TestCase;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TestUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

public class LangTest extends TestCase {

  public static final String ROOT = "/i18n/lang.properties";
  public static final String FRENCH = "/i18n/lang_fr.properties";
  public static final String ENGLISH = "/i18n/lang_en.properties";

  @Test
  public void testFrenchDictionnaryIsComplete() throws Exception {
    SortedSet<Object> diff = diff(ROOT, FRENCH);
    TestUtils.checkEmpty(diff, "Missing entries in " + FRENCH);
  }

  @Test
  public void testEnglishDictionnaryIsComplete() throws Exception {
    SortedSet<Object> diff = diff(ROOT, ENGLISH);
    TestUtils.checkEmpty(diff, "Missing entries in " + ENGLISH);
  }

  private SortedSet<Object> diff(String rootFile, String langFile) throws Exception {
    SortedSet<Object> root = load(rootFile);
    SortedSet<Object> lang = load(langFile);
    root.removeAll(lang);
    return root;
  }

  private static SortedSet<Object> load(String fileName) throws IOException {
    return Files.loadPropertyKeys(fileName, Lang.class);
  }

  @Test
  public void testDefaultSeriesAreAllTranslatedInFrench() throws Exception {
    checkDefaultSeries(FRENCH);
  }

  @Test
  public void testDefaultSeriesAreAllTranslatedInEnglish() throws Exception {
    checkDefaultSeries(ENGLISH);
  }

  private void checkDefaultSeries(String langFile) {
    List<String> missingKeys = new ArrayList<String>();
    Properties properties = Files.loadProperties(DefaultSeries.class, langFile);
    for (DefaultSeries defaultSeries : DefaultSeries.values()) {
      String key = Labels.getKey(defaultSeries);
      Object label = properties.get(key);
      if (label == null || Strings.isNullOrEmpty(label.toString())) {
        missingKeys.add(key);
      }
    }
    if (!missingKeys.isEmpty()) {
      fail("Missing keys: " + missingKeys);
    }
  }
}
