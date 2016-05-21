package com.budgetview.importer;

import com.budgetview.importer.ofx.OfxFunctor;
import com.budgetview.importer.ofx.OfxParser;
import com.budgetview.importer.utils.TypedInputStream;
import org.globsframework.utils.Log;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Obfuscator {
  private static Set<String> stringTagToObfuscate = new HashSet<String>();
  private static Set<String> doubleTagToObfuscate = new HashSet<String>();
  private Map<String, String> knownStrings = new HashMap<String, String>();
  private Set<String> generatedNumbers = new HashSet<String>();
  private int currentGeneratedNumber = 1000000;
  private char currentRandomChar = 'a';
  private char currentRandomDigit = '0';

  static {
    addStringsToObfuscate("NAME", "MEMO", "ACCTID", "CHECKNUM");
    addNumbersToObfuscate("TRNAMT", "BALAMT");
  }

  public String apply(TypedInputStream stream) {
    BankFileType type = stream.getType();
    final StringBuilder builder = new StringBuilder();
    switch (type) {
      case OFX:
        transformOfx(stream, builder);
        break;
      case QIF:
        transformQif(stream, builder);
        break;
      default:
        throw new InvalidParameter("Unexpected file type: " + type);
    }
    return builder.toString();
  }

  private void transformOfx(TypedInputStream stream, final StringBuilder builder) {
    OfxParser parser = new OfxParser();
    try {
      parser.parse(stream.getBestProbableReader(), new OfxFunctor() {
        public void processHeader(String key, String value) {
          builder.append(key)
            .append(":")
            .append(value)
            .append("\n");
        }

        public void enterTag(String tag) {
          builder.append("<")
            .append(tag)
            .append(">")
            .append("\n");
        }

        public void leaveTag(String tag) {
          builder.append("</")
            .append(tag)
            .append(">")
            .append("\n");
        }

        public void processTag(String tag, String content) {
          builder
            .append("<")
            .append(tag)
            .append(">");
          if (doubleTagToObfuscate.contains(tag)) {
            builder.append(getObfuscatedNumber(content));
          }
          else if (stringTagToObfuscate.contains(tag)) {
            builder.append(getStringContent(content));
          }
          else {
            builder.append(content);
          }
          builder.append("\n");
        }

        public void end() {
        }
      });
    }
    catch (IOException e) {
      Log.write("OFX obfuscation error", e);
    }
  }

  private void transformQif(TypedInputStream stream, StringBuilder builder) {
    try {
      BufferedReader reader = new BufferedReader(stream.getBestProbableReader());
      String line = reader.readLine();
      while (line != null) {
        if (line.startsWith("T") || line.startsWith("$")) {
          builder
            .append(line.charAt(0))
            .append(generateNumber());
        }
        else if (line.startsWith("P") || line.startsWith("N") || line.startsWith("L") || line.startsWith("M")) {
          builder.append(line.charAt(0));
          builder.append(generateString(line).substring(1).trim());
        }
        else {
          builder.append(line);
        }
        builder.append("\n");
        line = reader.readLine();
      }
    }
    catch (IOException e) {
      Log.write("QIF obfuscation error", e);
    }
  }

  private String getObfuscatedNumber(String content) {
    if (knownStrings.containsKey(content)) {
      return knownStrings.get(content);
    }
    String number = generateNumber();
    while (generatedNumbers.contains(number)) {
      number = generateNumber();
    }
    knownStrings.put(content, number);
    generatedNumbers.add(number);
    return number;
  }

  private String generateNumber() {
    String result = Integer.toString(currentGeneratedNumber);
    currentGeneratedNumber += 1;
    return result;
  }

  private String getStringContent(String line) {
    String[] strings = line.split(" ");
    StringBuilder buffer = new StringBuilder();
    for (String content : strings) {
      buffer.append(getString(content));
      buffer.append(" ");
    }
    return buffer.toString().trim();
  }

  private String getString(String content) {
    String obfuscatedString = knownStrings.get(content);
    if (obfuscatedString != null) {
      return obfuscatedString;
    }

    int count = 0;
    while (true) {
      String newGenerated = generateString(content);
      if (generatedNumbers.contains(newGenerated)) {
        if (count == 26) {
          generatedNumbers.add(newGenerated);
          knownStrings.put(content, newGenerated);
          return newGenerated;
        }
        else {
          count++;
        }
      }
      else {
        generatedNumbers.add(newGenerated);
        knownStrings.put(content, newGenerated);
        return newGenerated;
      }
    }
  }

  private String generateString(String content) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (Character.isSpaceChar(c)) {
        builder.append(' ');
      }
      else if (Character.isDigit(c)) {
        builder.append(getRandomDigit());
      }
      else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
        builder.append(getRandomChar());
      }
      else {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  private char getRandomChar() {
    char result = currentRandomChar;
    currentRandomChar += 1;
    if (currentRandomChar > 'z') {
      currentRandomChar = 'a';
    }
    return result;
  }

  private char getRandomDigit() {
    char result = currentRandomDigit;
    currentRandomDigit += 1;
    if (currentRandomDigit > '9') {
      currentRandomDigit = '0';
    }
    return result;
  }

  static void addStringsToObfuscate(String... strings) {
    for (String string : strings) {
      stringTagToObfuscate.add(string.toUpperCase());
      stringTagToObfuscate.add(string.toLowerCase());
    }
  }

  static void addNumbersToObfuscate(String... numbers) {
    for (String number : numbers) {
      doubleTagToObfuscate.add(number.toUpperCase());
      doubleTagToObfuscate.add(number.toLowerCase());
    }
  }
}
