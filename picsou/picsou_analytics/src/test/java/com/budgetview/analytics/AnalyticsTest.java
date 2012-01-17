package com.budgetview.analytics;

import com.budgetview.analytics.checker.AnalyticsChecker;
import com.budgetview.analytics.model.LogEntry;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekStat;
import junit.framework.TestCase;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.GlobRepositoryChecker;
import org.globsframework.model.format.GlobPrinter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.globsframework.model.FieldValue.value;

public class AnalyticsTest extends TestCase {
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  private GlobRepository repository;
  private AnalyticsChecker analytics;

  public void setUp() throws Exception {
    repository = GlobRepositoryBuilder.createEmpty();
    analytics = new AnalyticsChecker(this, repository);
  }

  public void testSampleFile() throws Exception {

    analytics.load("log1.txt");

    analytics.checkUser("bernard@wanadoo.fr",
                        value(User.FIRST_DATE, parseDate("20110923")),
                        value(User.LAST_DATE, parseDate("20111007")),
                        value(User.PURCHASE_DATE, parseDate("20110925")),
                        value(User.PING_COUNT, 8),
                        value(User.PREVIOUS_USER, false));
  }

  public void testStandardPurchase() throws Exception {
    analytics.createLog()
      .logNewAnonymous("22 Dec 2011", "xyz1")
      .logPurchase("29 Dec 2011", "marc@free.fr")
      .logKnownAnonymous("31 Dec 2011", "xyz1")
      .logOkForMail("2 Jan 2012", "marc@free.fr", "xyz1", 145)
      .logOkForMail("9 Jan 2012", "marc@free.fr", "xyz1", 146)
      .load();

    analytics.checkUser("marc@free.fr",
                        value(User.FIRST_DATE, parseDate("20111222")),
                        value(User.LAST_DATE, parseDate("20120109")),
                        value(User.PURCHASE_DATE, parseDate("20111229")),
                        value(User.PING_COUNT, 4),
                        value(User.PREVIOUS_USER, false));
  }

  public void testFirstDayOf2012() throws Exception {
    analytics.createLog()
      .logNewAnonymous("1 Jan 2012", "xyz1")
      .logPurchase("1 Jan 2012", "marc@free.fr")
      .logKnownAnonymous("1 Jan 2012", "xyz1")
      .logOkForMail("1 Jan 2012", "marc@free.fr", "xyz1", 145)
      .load();

    analytics.checkUser("marc@free.fr",
                        value(User.FIRST_DATE, parseDate("20120101")),
                        value(User.LAST_DATE, parseDate("20120101")),
                        value(User.PURCHASE_DATE, parseDate("20120101")),
                        value(User.PING_COUNT, 3),
                        value(User.PREVIOUS_USER, false));
  }

  private Date parseDate(String text) throws ParseException {
    return DATE_FORMAT.parse(text);
  }
}
