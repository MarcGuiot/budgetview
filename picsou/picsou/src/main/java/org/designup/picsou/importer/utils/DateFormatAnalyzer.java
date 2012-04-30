package org.designup.picsou.importer.utils;

import org.designup.picsou.gui.importer.utils.InvalidFileFormat;
import org.globsframework.utils.exceptions.InvalidData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormatAnalyzer {
  private Date referenceDate;

  public DateFormatAnalyzer(Date referenceDate) {
    this.referenceDate = referenceDate;
  }

  public List<String> parse(Set<String> dates) throws InvalidData {
    dates.remove(null);
    return parse(dates, getAllFormats());
  }

  public List<String> parse(Set<String> dates, List<String> possibleFormat) {
    int maxFirst = 0;
    int maxSecond = 0;
    int maxThird = 0;

    if (dates.size() >= 1) {
      Pattern pattern = Pattern.compile("\\d{8}+");
      Matcher matcher = pattern.matcher(dates.iterator().next());
      if (matcher.matches()) {
        return Collections.singletonList("yyyyMMdd");
      }
    }

    char separator = '/';
    for (String date : dates) {

      String[] items = date.split("[-\\./]");
      if (items.length != 3) {
        throw new InvalidFileFormat("Invalid date: " + date + " - items: " + Arrays.toString(items));
      }
      separator = date.charAt(items[0].length());
      maxFirst = getMax(maxFirst, items, 0);
      maxSecond = getMax(maxSecond, items, 1);
      maxThird = getMax(maxThird, items, 2);
    }

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(referenceDate);
    int maxYear = calendar.get(Calendar.YEAR) + 1;
    if (toYear(maxFirst) > maxYear && maxFirst < 100) {
      possibleFormat.remove("yy/MM/dd");
    }
    if (toYear(maxThird) > maxYear && maxThird < 100) {
      possibleFormat.remove("dd/MM/yyyy");
      possibleFormat.remove("dd/MM/yy");
      possibleFormat.remove("MM/dd/yyyy");
      possibleFormat.remove("MM/dd/yy");
    }

    if (maxFirst > 12) {
      possibleFormat.remove("MM/dd/yy");
      possibleFormat.remove("MM/dd/yyyy");
    }
    if (maxSecond > 12) {
      possibleFormat.remove("dd/MM/yy");
      possibleFormat.remove("dd/MM/yyyy");
      possibleFormat.remove("yy/MM/dd");
      possibleFormat.remove("yyyy/MM/dd");
    }

    if (maxFirst > 31) {
      possibleFormat.remove("dd/MM/yy");
      possibleFormat.remove("dd/MM/yyyy");
    }
    if (maxSecond > 31) {
      possibleFormat.remove("MM/dd/yy");
      possibleFormat.remove("MM/dd/yyyy");
    }
    if (maxThird > 31) {
      possibleFormat.remove("yy/MM/dd");
      possibleFormat.remove("yyyy/MM/dd");
    }

    if (possibleFormat.isEmpty()){
      possibleFormat = getAllFormats();
    }
    for (int i = 0, size = possibleFormat.size(); i < size; i++) {
      possibleFormat.set(i, possibleFormat.get(i).replace('/', separator));
    }
    return possibleFormat;
  }

  public static List<String> getAllFormats() {
    List<String> result = new ArrayList<String>();
    result.add("yy/MM/dd");
    result.add("MM/dd/yy");
    result.add("dd/MM/yy");
    return result;
  }

  private int toYear(int maxFirst) {
    return maxFirst < 100 ? maxFirst < 70 ? 2000 + maxFirst : 1900 + maxFirst : maxFirst;
  }

  private int getMax(int maxFirst, String[] items, int pos) {
    try {
      int value = Integer.parseInt(items[pos]);
      return Math.max(maxFirst, value);
    }
    catch (NumberFormatException e) {
      throw new InvalidData("Invalid date element '" + items[pos] + "' in " + Arrays.toString(items));
    }
  }
}
