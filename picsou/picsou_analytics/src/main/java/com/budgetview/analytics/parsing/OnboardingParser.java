package com.budgetview.analytics.parsing;

import com.budgetview.analytics.model.OnboardingInfoEntry;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.FieldValuesBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Obfuscation
//  g => importStarted:[ ]*([a-z]+)[ ]*, " +
//  i => categorizationSelectionDone:[ ]*([a-z]+)[ ]*, " +
//  j => categorizationAreaSelectionDone:[ ]*([a-z]+)[ ]*, " +
//  k => firstCategorizationDone:[ ]*([a-z]+)[ ]*, " +
//  l => categorizationSkipped:[ ]*([a-z]+)[ ]*, " +
//  m => gotoBudgetShown:[ ]*([a-z]+)[ ]*");

public class OnboardingParser {

  private static Pattern USE =
    Pattern.compile(".*use:[ ]*([0-9]+)[, ].*");

  private static Pattern USES =
    Pattern.compile(".*uses:[ ]*([0-9]+)[, ].*");

  private static Pattern INITIAL_STEPS_COMPLETED =
    Pattern.compile(".*initialStepsCompleted:[ ]*([a-z]+)[, ].*");

  private static Pattern IMPORT_STARTED =
    Pattern.compile(".*importStarted:[ ]*([a-z]+)[, ].*");

  private static Pattern CATEGORIZATION_SELECTION_DONE =
    Pattern.compile(".*categorizationSelectionDone:[ ]*([a-z]+)[, ].*");

  private static Pattern FIRST_CATEGORIZATION_DONE =
    Pattern.compile(".*firstCategorizationDone:[ ]*([a-z]+)[, ].*");

  private static Pattern CATEGORIZATION_SKIPPED =
    Pattern.compile(".*categorizationSkipped:[ ]*([a-z]+)[, ].*");

  private static Pattern GOTO_BUDGET_SHOWN =
    Pattern.compile(".*gotoBudgetShown:[ ]*([a-z]+)[, ].*");

  public static void parseValues(String content, FieldValuesBuilder values) {
    parseInt(USES, OnboardingInfoEntry.COUNT, content, values);
    parseInt(USE, OnboardingInfoEntry.COUNT, content, values);
    parseBoolean(INITIAL_STEPS_COMPLETED, OnboardingInfoEntry.INITIAL_STEPS_COMPLETED, content, values);
    parseBoolean(IMPORT_STARTED, OnboardingInfoEntry.IMPORT_STARTED, content, values);
    parseBoolean(CATEGORIZATION_SELECTION_DONE, OnboardingInfoEntry.CATEGORIZATION_SELECTION_DONE, content, values);
    parseBoolean(FIRST_CATEGORIZATION_DONE, OnboardingInfoEntry.FIRST_CATEGORIZATION_DONE, content, values);
    parseBoolean(CATEGORIZATION_SKIPPED, OnboardingInfoEntry.CATEGORIZATION_SKIPPED, content, values);
    parseBoolean(GOTO_BUDGET_SHOWN, OnboardingInfoEntry.GOTO_BUDGET_SHOWN, content, values);
  }

  public static void parseBoolean(Pattern pattern, BooleanField field, String content, FieldValuesBuilder values) {
    Matcher matcher = pattern.matcher(content);
    if (matcher.matches()) {
      values.set(field, Boolean.parseBoolean(matcher.group(1)));
    }
  }

  public static void parseInt(Pattern pattern, IntegerField field, String content, FieldValuesBuilder values) {
    Matcher matcher = pattern.matcher(content);
    if (matcher.matches()) {
      values.set(field, Integer.parseInt(matcher.group(1)));
    }
  }
}
