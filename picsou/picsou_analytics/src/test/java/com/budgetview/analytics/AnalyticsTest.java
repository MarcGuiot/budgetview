package com.budgetview.analytics;

import com.budgetview.analytics.checker.AnalyticsChecker;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.OnboardingInfoEntry;
import junit.framework.TestCase;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;

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
      .logPurchase("29 Dec 2011", "marc1@free.fr")
      .logKnownAnonymous("31 Dec 2011", "xyz1")
      .logOkForMail("2 Jan 2012", "marc1@free.fr", "xyz1", 145)
      .logOkForMail("9 Jan 2012", "marc1@free.fr", "xyz1", 146)
      .load();

    analytics.checkUser("marc1@free.fr",
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

  public void testOldUserProgressParsing() throws Exception {
    analytics.createLog()
      .logOldUseInfo("1 Jan 2012", 1, true, false, true, false, true, false, true)
      .load();

    analytics.checkUseInfo(parseDate("20120101"),
                           value(OnboardingInfoEntry.INITIAL_STEPS_COMPLETED, true),
                           value(OnboardingInfoEntry.IMPORT_STARTED, false),
                           value(OnboardingInfoEntry.CATEGORIZATION_SELECTION_DONE, true),
                           value(OnboardingInfoEntry.FIRST_CATEGORIZATION_DONE, true),
                           value(OnboardingInfoEntry.CATEGORIZATION_SKIPPED, false),
                           value(OnboardingInfoEntry.GOTO_BUDGET_SHOWN, true));
  }

  public void testUserProgressParsing() throws Exception {
    analytics.createLog()
      .logUseInfo("1 Jan 2012", 1, true, false, true, false, true, false, true)
      .load();

    analytics.checkUseInfo(parseDate("20120101"),
                           value(OnboardingInfoEntry.INITIAL_STEPS_COMPLETED, true),
                           value(OnboardingInfoEntry.IMPORT_STARTED, false),
                           value(OnboardingInfoEntry.CATEGORIZATION_SELECTION_DONE, true),
                           value(OnboardingInfoEntry.FIRST_CATEGORIZATION_DONE, true),
                           value(OnboardingInfoEntry.CATEGORIZATION_SKIPPED, false),
                           value(OnboardingInfoEntry.GOTO_BUDGET_SHOWN, true));
  }

  public void testUserEvaluation() throws Exception {
    analytics.createLog()
      // 6 returning users 
      .logNewAnonymous("3 Jan 2012", "xyz1")
      .logKnownAnonymous("3 Jan 2012", "xyz1")
      .logNewAnonymous("4 Jan 2012", "xyz2")
      .logKnownAnonymous("4 Jan 2012", "xyz2")
      .logNewAnonymous("4 Jan 2012", "xyz3")
      .logKnownAnonymous("4 Jan 2012", "xyz3")
      .logNewAnonymous("4 Jan 2012", "xyz4")
      .logKnownAnonymous("4 Jan 2012", "xyz4")
      .logNewAnonymous("5 Jan 2012", "xyz5")
      .logKnownAnonymous("5 Jan 2012", "xyz5")
      .logNewAnonymous("5 Jan 2012", "xyz6")
      .logKnownAnonymous("5 Jan 2012", "xyz6")
      // 3 one-time visitors
      .logNewAnonymous("3 Jan 2012", "xyz7")
      .logNewAnonymous("4 Jan 2012", "xyz8")
      .logNewAnonymous("5 Jan 2012", "xyz9")
      // 3 feedback
      .logUserEvaluation("3 Jan 2012", true)
      .logUserEvaluation("4 Jan 2012", true)
      .logUserEvaluation("5 Jan 2012", false)
      // other weeks - ignored
      .logNewAnonymous("1 Jan 2012", "xyz1")
      .logKnownAnonymous("1 Jan 2012", "xyz1")
      .logNewAnonymous("9 Jan 2012", "xyz1")
      .logKnownAnonymous("9 Jan 2012", "xyz1")
      .logUserEvaluation("1 Jan 2012", false)
      .load();

//    analytics.checkWeekPerf(201201,
//                            value(WeekPerfStat.EVALUATIONS_RATIO, 50.0),
//                            value(WeekPerfStat.EVALUATIONS_RESULT, 66.7));

  }

  private Date parseDate(String text) throws ParseException {
    return DATE_FORMAT.parse(text);
  }
}
