package com.budgetview.shared.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AmountFormat {
  public static final DecimalFormat DECIMAL_FORMAT =
    new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
}
