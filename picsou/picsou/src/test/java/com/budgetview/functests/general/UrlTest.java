package com.budgetview.functests.general;

import junit.framework.TestCase;
import com.budgetview.utils.Lang;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;

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

  private Pattern URL_PATTERN = Pattern.compile(".*(http://[.A-z0-9_/#!-]+).*");

  public void testFrench() throws Exception {
    checkUrlsInFile("/i18n/lang_fr.properties");
  }

  public void testEnglish() throws Exception {
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
      fail("URL '" + url + "' not reachable in file: " + file);
    }
  }

  private static Properties load(String fileName) throws IOException {
    return Files.loadProperties(Lang.class, fileName);
  }
}
