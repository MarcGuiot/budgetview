package com.budgetview.analytics.parsing;

import com.budgetview.analytics.model.LogEntry;
import com.budgetview.analytics.model.LogEntryType;
import com.budgetview.analytics.model.UserEvaluationEntry;
import com.budgetview.analytics.model.UserProgressInfoEntry;
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
  private static Pattern USER_PROGRESS_FORMAT_OBFUSCATED =
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ - use info =[ ]*use:[ ]*([0-9]+),[ ]*" +
                    "initialStepsCompleted:[ ]*([a-z]+),[ ]*" +
                    "g:[ ]*([a-z]+)[ ]*, " +
                    "i:[ ]*([a-z]+)[ ]*, " +
                    "j:[ ]*([a-z]+)[ ]*, " +
                    "k:[ ]*([a-z]+)[ ]*, " +
                    "l:[ ]*([a-z]+)[ ]*, " +
                    "m:[ ]*([a-z]+)[ ]*");
  private static Pattern USER_PROGRESS_FORMAT =
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ - use info =[ ]*use:[ ]*([0-9]+),[ ]*" +
                    "initialStepsCompleted:[ ]*([a-z]+),[ ]*" +
                    "importStarted:[ ]*([a-z]+)[ ]*, " +
                    "categorizationSelectionDone:[ ]*([a-z]+)[ ]*, " +
                    "categorizationAreaSelectionDone:[ ]*([a-z]+)[ ]*, " +
                    "firstCategorizationDone:[ ]*([a-z]+)[ ]*, " +
                    "categorizationSkipped:[ ]*([a-z]+)[ ]*, " +
                    "gotoBudgetShown:[ ]*([a-z]+)[ ]*");
  private static Pattern USER_EVALUATION_FORMAT =
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ -.*User evaluation[ ]*:[ ]*([A-z]*).*");

  public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

  public void load(Reader input, GlobRepository repository) {
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
                          parseInt(progressMatcher.group(2)),
                          parseBoolean(progressMatcher.group(3)),
                          parseBoolean(progressMatcher.group(4)),
                          parseBoolean(progressMatcher.group(5)),
                          parseBoolean(progressMatcher.group(6)),
                          parseBoolean(progressMatcher.group(7)),
                          parseBoolean(progressMatcher.group(8)),
                          parseBoolean(progressMatcher.group(9)),
                          repository);
          continue;
        }
        Matcher oldProgressMatcher = USER_PROGRESS_FORMAT_OBFUSCATED.matcher(trimmed);
        if (oldProgressMatcher.matches()) {
          processProgress(parseDate(oldProgressMatcher.group(1)),
                          parseInt(oldProgressMatcher.group(2)),
                          parseBoolean(oldProgressMatcher.group(3)),
                          parseBoolean(oldProgressMatcher.group(4)),
                          parseBoolean(oldProgressMatcher.group(5)),
                          parseBoolean(oldProgressMatcher.group(6)),
                          parseBoolean(oldProgressMatcher.group(7)),
                          parseBoolean(oldProgressMatcher.group(8)),
                          parseBoolean(oldProgressMatcher.group(9)),
                          repository);
          continue;
        }

        Matcher userEvaluationMatcher = USER_EVALUATION_FORMAT.matcher(trimmed);
        if (userEvaluationMatcher.matches()) {
          processUserEvaluation(userEvaluationMatcher.group(1), userEvaluationMatcher.group(2), repository);
          continue;
        }

        System.out.println("COULD NOT PARSE: " + line);
      }
      reader.close();
    }
    catch (Exception e) {
      throw new RuntimeException("Error for line: " + line, e);
    }
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
                      value(LogEntry.EMAIL, parseArg(args, MAIL_FORMAT)));
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

  private Date parseDate(String date) {
    try {
      return DEFAULT_DATE_FORMAT.parse(date);
    }
    catch (ParseException e) {
      throw new RuntimeException("Invalid date format: " + date, e);
    }
  }

  private boolean parseBoolean(String text) {
    return Boolean.parseBoolean(text);
  }

  private int parseInt(String text) {
    return Integer.parseInt(text);
  }

  private String parseArg(String text, Pattern pattern) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return matcher.group(1).trim();
    }
    return null;
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

  private void processProgress(Date date,
                               int count,
                               boolean initialStepsCompleted,
                               boolean importDone,
                               boolean categorizationSelectionDone,
                               boolean categorizationAreaSelectionDone,
                               boolean firstCategorizationDone,
                               boolean categorizationSkipped,
                               boolean gotoBudgetShown,
                               GlobRepository repository) {
    repository.create(UserProgressInfoEntry.TYPE,
                      value(UserProgressInfoEntry.DATE, date),
                      value(UserProgressInfoEntry.COUNT, count),
                      value(UserProgressInfoEntry.INITIAL_STEPS_COMPLETED, initialStepsCompleted),
                      value(UserProgressInfoEntry.IMPORT_STARTED, importDone),
                      value(UserProgressInfoEntry.CATEGORIZATION_SELECTION_DONE, categorizationSelectionDone),
                      value(UserProgressInfoEntry.CATEGORIZATION_AREA_SELECTION_DONE, categorizationAreaSelectionDone),
                      value(UserProgressInfoEntry.FIRST_CATEGORIZATION_DONE, firstCategorizationDone),
                      value(UserProgressInfoEntry.CATEGORIZATION_SKIPPED, categorizationSkipped),
                      value(UserProgressInfoEntry.GOTO_BUDGET_SHOWN, gotoBudgetShown));
  }
}
