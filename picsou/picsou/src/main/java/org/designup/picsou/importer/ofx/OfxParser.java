package org.designup.picsou.importer.ofx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfxParser {

  private Pattern headerRegexp = Pattern.compile("^([A-z0-9]+):(.*)$");
  private Pattern containmentTagStartRegexp = Pattern.compile("^<([A-z0-9]+)>$");
  private Pattern containmentTagEndRegexp = Pattern.compile("^</([A-z0-9]+)>$");
  private Pattern contentTagRegexp = Pattern.compile("^<([A-z0-9]+)>(.+)$");

  public void parse(Reader reader, OfxFunctor functor) throws IOException {
    BufferedReader buffer = new BufferedReader(reader);
    while (buffer.ready()) {
      String nextLine = buffer.readLine();
      if (nextLine == null) {
        break;
      }
      String line = nextLine.trim();
      Matcher enterTag = containmentTagStartRegexp.matcher(line);
      if (enterTag.matches()) {
        functor.enterTag(enterTag.group(1));
        continue;
      }
      Matcher leaveTag = containmentTagEndRegexp.matcher(line);
      if (leaveTag.matches()) {
        functor.leaveTag(leaveTag.group(1));
        continue;
      }
      Matcher content = contentTagRegexp.matcher(line);
      if (content.matches()) {
        functor.processTag(content.group(1), content.group(2).trim());
        continue;
      }
      Matcher header = headerRegexp.matcher(line);
      if (header.matches()) {
        functor.processHeader(header.group(1), header.group(2));
        continue;
      }
    }
    functor.end();
  }
}
