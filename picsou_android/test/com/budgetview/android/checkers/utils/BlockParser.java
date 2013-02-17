package com.budgetview.android.checkers.utils;

import android.widget.TextView;

public interface BlockParser {
  void start(int id);
  void end(int id);
  void processText(int id, TextView textView);
  void complete();
}
