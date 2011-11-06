package org.designup.picsou.importer;

import org.designup.picsou.importer.ofx.OfxFunctor;
import org.designup.picsou.importer.ofx.OfxParser;
import org.designup.picsou.importer.utils.TypedInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Obfuscator {
  static Set<String> stringTagToObfuscate = new HashSet<String>();
  static Set<String> doubleTagToObfuscate = new HashSet<String>();
  private Random random = new Random();
  private Map<String, String> knownString = new HashMap<String, String>();
  private Set<String> generatedString = new HashSet<String>();

  static {
    addStringToObfuscate("NAME", "MEMO", "ACCTID");
    addDoubleToObfuscate("TRNAMT", "BALAMT");
  }

  public String apply(TypedInputStream stream) {
    BankFileType type = stream.getType();
    final StringBuffer buf = new StringBuffer();
    if (type == BankFileType.OFX) {
      transformOfx(stream, buf);
    }
    else {
      transformQif(stream, buf);
    }
    return buf.toString();
  }

  private void transformOfx(TypedInputStream stream, final StringBuffer buf) {
    OfxParser parser = new OfxParser();
    try {
      parser.parse(stream.getBestProbableReader(), new OfxFunctor() {
        public void processHeader(String key, String value) {
          buf.append(key)
            .append(":")
            .append(value)
            .append("\n");
        }

        public void enterTag(String tag) {
          buf.append("<")
            .append(tag)
            .append(">")
            .append("\n");
        }

        public void leaveTag(String tag) {
          buf.append("</")
            .append(tag)
            .append(">")
            .append("\n");
        }

        public void processTag(String tag, String content) {
          buf
            .append("<")
            .append(tag)
            .append(">");
          if (doubleTagToObfuscate.contains(tag)) {
            buf.append(getDouble(content));
          }
          else if (stringTagToObfuscate.contains(tag)) {
            buf.append(getStringContent(content));
          }
          else {
            buf.append(content);
          }
          buf.append("\n");
        }

        public void end() {
        }
      });
    }
    catch (IOException e) {
    }
  }

  private void transformQif(TypedInputStream stream, StringBuffer buf) {
    try {
      BufferedReader reader = new BufferedReader(stream.getBestProbableReader());
      String s = reader.readLine();
      while (s != null) {
        if (s.startsWith("T")) {
          buf.append("T")
            .append(generateNum(s.substring(1).trim()));
        }
        else if (s.startsWith("P") || s.startsWith("N") || s.startsWith("L") || s.startsWith("M")) {
          buf.append(s.charAt(0));
          buf.append(generate(s).substring(1).trim());
        }
        else {
          buf.append(s);
        }
        buf.append("\n");
        s = reader.readLine();
      }
    }
    catch (IOException e) {
    }
  }

  private String getDouble(String content) {
    if (knownString.containsKey(content)) {
      return knownString.get(content);
    }
    String str = generateNum(content);
    while (generatedString.contains(str)) {
      str = generateNum(content);
    }
    knownString.put(content, str);
    generatedString.add(str);
    return str;
  }

  private String generateNum(String content) {
    StringBuffer buffer = new StringBuffer();
    int countDigit = 0;
    for (int i = 0; i < content.length(); i++) {
      if (Character.isDigit(content.charAt(i))) {
        countDigit++;
      }
      else {
        if (countDigit != 0) {
          appendRandomNumber(buffer, countDigit);
          countDigit = 0;
        }
        buffer.append(content.charAt(i));
      }
    }
    if (countDigit != 0) {
      appendRandomNumber(buffer, countDigit);
    }
    return buffer.toString();
  }

  private void appendRandomNumber(StringBuffer buffer, int countDigit) {
    if (countDigit > 4) {
      buffer.append(Integer.toString(random.nextInt(1000) + 1));
    }
    else {
      buffer.append(Integer.toString(random.nextInt(((int)Math.pow(10, countDigit)))));
    }
  }

  private String getStringContent(String line) {
    String[] strings = line.split(" ");
    StringBuffer buffer = new StringBuffer();
    for (String content : strings) {
      buffer.append(getStr(content));
      buffer.append(" ");
    }
    return buffer.toString().trim();
  }

  private String getStr(String str) {
    String s = knownString.get(str);
    if (s == null) {
      char c = 'a';
      char c1 = '1';
      int count = 0;
      while (true) {
        String s1 = generate(str);
        if (generatedString.contains(s1)) {
          if (count == 26) {
            generatedString.add(s1);
            knownString.put(str, s1);
            return s1;
          }
          else {
            c = (char)(c + 1);
            c1 = (char)(c1 + 1);
            if (c1 > '9') {
              c1 = '1';
            }
            count++;
          }
        }
        else {
          generatedString.add(s1);
          knownString.put(str, s1);
          return s1;
        }
      }
    }
    return s;
  }

  private String generate(String content) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (Character.isSpaceChar(c)) {
        buffer.append(' ');
      }
      else if (Character.isDigit(c)) {
        buffer.append((char)('1' + random.nextInt(8)));
      }
      else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
        buffer.append((char)('a' + random.nextInt(25)));
      }
      else {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }

  static void addStringToObfuscate(String... str) {
    for (String s : str) {
      stringTagToObfuscate.add(s.toUpperCase());
      stringTagToObfuscate.add(s.toLowerCase());
    }
  }

  static void addDoubleToObfuscate(String... str) {
    for (String s : str) {
      doubleTagToObfuscate.add(s.toUpperCase());
      doubleTagToObfuscate.add(s.toLowerCase());
    }
  }
}
