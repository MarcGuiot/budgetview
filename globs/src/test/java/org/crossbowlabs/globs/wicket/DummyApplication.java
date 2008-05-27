package org.crossbowlabs.globs.wicket;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.Formats;
import static org.crossbowlabs.globs.model.format.Formats.DEFAULT_DECIMAL_FORMAT;
import static org.crossbowlabs.globs.model.format.Formats.DEFAULT_NO_VALUE;
import static org.crossbowlabs.globs.model.format.Formats.DEFAULT_TIMESTAMP_FORMAT;
import static org.crossbowlabs.globs.model.format.Formats.DEFAULT_YES_VALUE;
import org.crossbowlabs.globs.model.format.utils.DefaultDescriptionService;
import wicket.Session;

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

  public Session newSession() {
    DummySession session = new DummySession(this);
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
