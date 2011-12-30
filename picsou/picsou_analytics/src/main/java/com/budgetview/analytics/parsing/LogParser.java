package com.budgetview.analytics.parsing;

import com.budgetview.analytics.model.LogEntryType;
import com.budgetview.analytics.model.LogEntry;
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
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ - thread [0-9]+ msg : ([a-z_]+) (.*)");
  private static Pattern PURCHASE_FORMAT =
    Pattern.compile("INFO ([0-9]+ [A-z]+ [0-9]+) [0-9:,]+ - NewUser : mail : '([A-z-_@\\.]*) VERIFIED");
  private static Pattern IP_FORMAT = Pattern.compile("ip = ([0-9\\.]+)");
  private static Pattern REPO_ID_FORMAT = Pattern.compile("id =[ ]*([A-z0-9/+=]+)");
  private static Pattern MAIL_FORMAT = Pattern.compile("mail =[ ]*([A-z-_@\\.]+)");

  public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

  public static void load(Reader input, GlobRepository repository) {
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
//        System.out.println("COULD NOT PARSE: " + line);
      }
      reader.close();
    }
    catch (Exception e) {
      throw new RuntimeException("Error for line: " + line, e);
    }
  }

  private static void processCommand(String date, String entryType, String args, GlobRepository repository) {
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

  private static void processPurchase(String date, String email, GlobRepository repository) {
    repository.create(LogEntry.TYPE,
                      value(LogEntry.DATE, parseDate(date)),
                      value(LogEntry.ENTRY_TYPE, LogEntryType.PURCHASE.getId()),
                      value(LogEntry.EMAIL, email));
  }

  private static Date parseDate(String date) {
    try {
      return DEFAULT_DATE_FORMAT.parse(date);
    }
    catch (ParseException e) {
      throw new RuntimeException("Invalid date format: " + date, e);
    }
  }

  private static String parseArg(String text, Pattern pattern) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return matcher.group(1).trim();
    }
    return null;
  }

  private static LogEntryType parseEntryType(String command) {
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
