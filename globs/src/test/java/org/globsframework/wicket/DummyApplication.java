package org.globsframework.wicket;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.Formats;
import static org.globsframework.model.format.Formats.*;
import org.globsframework.model.format.utils.DefaultDescriptionService;
import org.globsframework.utils.directory.Directory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DummyApplication extends GlobApplication {

  private static DummyApplication currentInstance;
  private GlobRepository repository;

  public DummyApplication() {
    currentInstance = this;
  }

  protected void init() {
    super.init();
    getMarkupSettings().setStripWicketTags(true);
    getDebugSettings().setAjaxDebugModeEnabled(false);
  }

  public static void reset(GlobRepository repository) {
    currentInstance.setRepository(repository);
  }

  private void setRepository(GlobRepository repository) {
    this.repository = repository;
  }

  public GlobRepository getRepository() {
    return repository;
  }

  public static Directory getStaticDirectory() {
    return currentInstance.getDirectory();
  }

  protected DescriptionService createDescriptionService() {
    try {
      InputStream propertiesStream = getClass().getResourceAsStream("description.properties");
      SimpleDateFormat dateFormat = new LenientDateFormat();
      dateFormat.setLenient(true);
      return new DefaultDescriptionService(
        new Formats(dateFormat, DEFAULT_TIMESTAMP_FORMAT, DEFAULT_DECIMAL_FORMAT,
                    DEFAULT_YES_VALUE, DEFAULT_NO_VALUE),
        new PropertyResourceBundle(propertiesStream));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Class getHomePage() {
    return DummyPage.class;
  }

  public Session newSession(Request request, Response response) {
    DummySession session = new DummySession(request, response, this);
    Session.set(session);
    return session;
  }

  private static class LenientDateFormat extends SimpleDateFormat {
    private static Pattern pattern = Pattern.compile("([0-9]+)/([0-9]+)/([0-9]?[0-9]?[0-9])");

    public LenientDateFormat() {
      super("dd/MM/yyyy");
    }

    public Date parse(String text, ParsePosition pos) {
      Matcher matcher = pattern.matcher(text.substring(pos.getIndex()));
      if (matcher.matches()) {
        String year = matcher.group(3);
        String yearPrefix = getYearPrefix(year);
        String newString = matcher.group(1) + "/" + matcher.group(2) + "/" + yearPrefix + year;
        pos.setIndex(pos.getIndex() + text.length());
        return super.parse(newString, new ParsePosition(0));
      }
      return super.parse(text, pos);
    }

    private String getYearPrefix(String year) {
      switch (year.length()) {
        case 1:
          return "200";
        case 2:
          return "20";
        case 3:
          return "2";
        default:
      }
      throw new RuntimeException("Unexpected length for year " + year);
    }
  }
}
