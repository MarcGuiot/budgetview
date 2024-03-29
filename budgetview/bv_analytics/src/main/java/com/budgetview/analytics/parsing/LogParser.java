package com.budgetview.analytics.parsing;

import com.budgetview.analytics.model.*;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.BufferedReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.globsframework.model.FieldValue.value;

public class LogParser {

  private static Pattern ENTRY_FORMAT =
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ - thread [0-9]+ msg : ([a-z0-9_]+) (.*)");
  private static Pattern PURCHASE_FORMAT =
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ - NewUser : mail : '([A-z0-9-_@\\.]*) VERIFIED");
  private static Pattern IP_FORMAT = Pattern.compile("ip = ([0-9\\.]+)");
  private static Pattern REPO_ID_FORMAT = Pattern.compile("id =[ ]*([A-z0-9/+=]+)");
  private static Pattern MAIL_FORMAT = Pattern.compile("mail =[ ]*([A-z0-9-_@\\.]+)");
  private static Pattern JAR_VERSION_FORMAT = Pattern.compile("jar version =[ ]*([0-9]+)");

  private static Pattern USER_PROGRESS_FORMAT =
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ - use info =[ ]*(.*)[ ]*");

  private static Pattern USER_EVALUATION_FORMAT =
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ -.*User evaluation[ ]*:[ ]*([A-z]*).*");

  private static Pattern[] IGNORED_PATTERNS = new Pattern[]{
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - mail sent.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - Mail to send.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - mail from.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - License activation ok.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - new jar version mail.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - mail :.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - No mail sent.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - Mail sent with new code.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - receive new User[ ]+:.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - NewUser :.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - item_number=.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - Send new activation code.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - code requested for.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - No mail sent (activation failed).*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - item_number.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - thread [0-9]+ msg :.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - duplicate line.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - unknown user.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - Invalidating previous.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - Bad ask for code from.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - init server.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - starting server.*"),
    Pattern.compile("INFO [0-9]+ [A-z]+ [0-9]+ [0-9:,]+ - reason_code=.*"),
  };

  public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

  private GlobRepository repository;
  private Date firstDate;
  private Date lastDate;

  public LogParser(GlobRepository repository) {
    this.repository = repository;
  }

  public void load(Reader input) {
    String line = null;
    try {
      BufferedReader reader = new BufferedReader(input);
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("INFO")) {
          continue;
        }
        String trimmed = line.trim();
        Matcher entryTypeMatcher = ENTRY_FORMAT.matcher(trimmed);
        if (entryTypeMatcher.matches()) {
          processCommand(entryTypeMatcher.group(1), entryTypeMatcher.group(2), entryTypeMatcher.group(3), repository);
          continue;
        }
        Matcher purchaseMatcher = PURCHASE_FORMAT.matcher(trimmed);
        if (purchaseMatcher.matches()) {
          processPurchase(purchaseMatcher.group(1), purchaseMatcher.group(2), repository);
          continue;
        }

        Matcher progressMatcher = USER_PROGRESS_FORMAT.matcher(trimmed);
        if (progressMatcher.matches()) {
          processProgress(parseDate(progressMatcher.group(1)),
                          progressMatcher.group(2),
                          repository);
          continue;
        }

        Matcher userEvaluationMatcher = USER_EVALUATION_FORMAT.matcher(trimmed);
        if (userEvaluationMatcher.matches()) {
          processUserEvaluation(userEvaluationMatcher.group(1), userEvaluationMatcher.group(2), repository);
          continue;
        }

        boolean ignored = false;
        for (Pattern pattern : IGNORED_PATTERNS) {
          Matcher matcher = pattern.matcher(trimmed);
          if (matcher.matches()) {
            ignored = true;
            continue;
          }
        }
        if (!ignored) {
          System.out.println("COULD NOT PARSE: " + line);
        }
      }
      reader.close();
    }
    catch (Exception e) {
      throw new RuntimeException("Error for line: " + line, e);
    }
  }

  public void complete() {
    repository.findOrCreate(LogPeriod.KEY);
    repository.update(LogPeriod.KEY,
                      value(LogPeriod.FIRST_DATE, firstDate),
                      value(LogPeriod.LAST_DATE, lastDate));
  }

  private void processCommand(String date, String entryType, String args, GlobRepository repository) {
    if (entryType.equals("ok_for")) {
      return;
    }
    repository.create(LogEntry.TYPE,
                      value(LogEntry.DATE, parseDate(date)),
                      value(LogEntry.ENTRY_TYPE, parseEntryType(entryType).getId()),
                      value(LogEntry.REPO_ID, parseArg(args, REPO_ID_FORMAT)),
                      value(LogEntry.IP, parseArg(args, IP_FORMAT)),
                      value(LogEntry.EMAIL, parseArg(args, MAIL_FORMAT)),
                      value(LogEntry.JAR_VERSION, toInt(parseArg(args, JAR_VERSION_FORMAT))));
  }

  private void processPurchase(String date, String email, GlobRepository repository) {
    repository.create(LogEntry.TYPE,
                      value(LogEntry.DATE, parseDate(date)),
                      value(LogEntry.ENTRY_TYPE, LogEntryType.PURCHASE.getId()),
                      value(LogEntry.EMAIL, email));
  }

  private void processUserEvaluation(String date, String result, GlobRepository repository) {
    repository.create(UserEvaluationEntry.TYPE,
                      value(UserEvaluationEntry.DATE, parseDate(date)),
                      value(UserEvaluationEntry.SATISFIED, "yes".equalsIgnoreCase(result.trim())));
  }

  private void processProgress(Date date, String content,
                               GlobRepository repository) {

    FieldValuesBuilder values = new FieldValuesBuilder();
    values.set(OnboardingInfoEntry.DATE, date);
    OnboardingParser.parseValues(content.trim(), values);

    repository.create(OnboardingInfoEntry.TYPE, values.get().toArray());
  }

  private Date parseDate(String date) {
    try {
      Date parsedDate = DEFAULT_DATE_FORMAT.parse(date);
      if (firstDate == null || firstDate.after(parsedDate)) {
        firstDate = parsedDate;
      }
      if (lastDate == null || lastDate.before(parsedDate)) {
        lastDate = parsedDate;
      }
      return parsedDate;
    }
    catch (ParseException e) {
      throw new RuntimeException("Invalid date format: " + date, e);
    }
  }

  private String parseArg(String text, Pattern pattern) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return matcher.group(1).trim();
    }
    return null;
  }

  private Integer toInt(String text) {
    if (text == null || text.isEmpty()) {
      return null;
    }
    return Integer.parseInt(text);
  }

  private LogEntryType parseEntryType(String command) {
    if (command.equalsIgnoreCase("known_anonymous")) {
      return LogEntryType.KNOWN_USER;
    }
    else if (command.equalsIgnoreCase("new_anonymous")) {
      return LogEntryType.NEW_USER;
    }
    else if (command.equalsIgnoreCase("compute_license")) {
      return LogEntryType.LICENCE_CHECK;
    }
    else if (command.equalsIgnoreCase("different_code_for")) {
      return LogEntryType.DIFFERENT_CODE;
    }
    throw new InvalidParameter("Unknown command type: " + command);
  }
}
