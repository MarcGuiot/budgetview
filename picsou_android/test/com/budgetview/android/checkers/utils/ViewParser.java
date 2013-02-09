package com.budgetview.android.checkers.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewParser {
  public static void parse(View view, ViewParserCallback viewParser) {
    crawl(view, viewParser);
    viewParser.complete();
  }

  private static void crawl(View view, ViewParserCallback viewParser) {
    if (view instanceof TextView) {
      viewParser.processText(view.getId(), (TextView)view);
    }
    else if (view instanceof ViewGroup) {
      int id = view.getId();
      ViewGroup group = (ViewGroup)view;
      viewParser.start(id);
      for (int i  = 0; i < group.getChildCount(); i++) {
        crawl(group.getChildAt(i), viewParser);
      }
      viewParser.end(id);
    }
  }
}
