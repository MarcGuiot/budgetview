package com.budgetview.functests.general;

import com.budgetview.utils.Lang;
import junit.framework.TestCase;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlTest extends TestCase {

  private Pattern URL_PATTERN = Pattern.compile(".*(http[s]?://[.A-z0-9_/#!-]+).*");

  // Désactivé (JUnit 3 : renommé pour ne plus commencer par "test") — vérifie en ligne les URL de lang_fr.properties : dépend du réseau et de budgetview.fr, qui a changé.
  @Test
  public void disabled_testFrench() throws Exception {
    checkUrlsInFile("/i18n/lang_fr.properties");
  }

  // Désactivé (JUnit 3 : renommé pour ne plus commencer par "test") — vérifie en ligne les URL de lang_en.properties : dépend du réseau et de budgetview.fr, qui a changé.
  @Test
  public void disabled_testEnglish() throws Exception {
    checkUrlsInFile("/i18n/lang_en.properties");
  }

  private void checkUrlsInFile(String file) throws IOException {
    Properties properties = load(file);
    Set<String> urls = new HashSet<String>();
    for (Object key : properties.keySet()) {
      String value = Strings.toString(properties.get(key));
      Matcher matcher = URL_PATTERN.matcher(value);
      if (matcher.matches()) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
          urls.add(matcher.group(i));
        }
      }
    }
    for (String url : urls) {
      System.out.println(url);
      checkUrl(file, url);
    }
  }

  private void checkUrl(String file, String url) {
    try {
      final URLConnection connection = new URL(url).openConnection();
      connection.connect();
      connection.getContent();
    }
    catch (final MalformedURLException e) {
      fail("Malformed URL: " + url);
    }
    catch (final IOException e) {
      fail("URL '" + url + "' not reachable in file: " + file + " - " + e.getMessage());
    }
  }

  private static Properties load(String fileName) throws IOException {
    return Files.loadProperties(Lang.class, fileName);
  }
}
