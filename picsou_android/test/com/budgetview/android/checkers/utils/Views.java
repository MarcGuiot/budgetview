package com.budgetview.android.checkers.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import junit.framework.Assert;
import org.globsframework.utils.Strings;
import org.robolectric.Robolectric;

public class Views {
  public static void parse(View view, BlockParser viewParser) {
    recursiveParse(view, viewParser);
    viewParser.complete();
  }

  private static void recursiveParse(View view, BlockParser viewParser) {
    if (view instanceof TextView) {
      viewParser.processText(view.getId(), (TextView)view);
    }
    else if (view instanceof ViewGroup) {
      int id = view.getId();
      ViewGroup group = (ViewGroup)view;
      viewParser.start(id);
      for (int i = 0; i < group.getChildCount(); i++) {
        recursiveParse(group.getChildAt(i), viewParser);
      }
      viewParser.end(id);
    }
  }

  public static void clickBlockWithTextView(View view, int blockId, int textViewId, String text) {
    boolean found = recursiveClickBlockWithTextView(view, blockId, textViewId, text);
    if (!found) {
      Assert.fail("No text found with text '" + text + "' in:\n" + Views.toString(view));
    }
  }

  private static boolean recursiveClickBlockWithTextView(View view, int blockId, int textViewId, String text) {
    if (view.getId() == blockId) {
      return recursiveClickBlockWithTextView(view, view, textViewId, text);
    }
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup)view;
      for (int i = 0; i < group.getChildCount(); i++) {
        boolean found = recursiveClickBlockWithTextView(group.getChildAt(i), blockId, textViewId, text);
        if (found) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean recursiveClickBlockWithTextView(View view, View blockView, int textViewId, String text) {
    if ((view.getId() == textViewId) && (view instanceof TextView)) {
      TextView textView = (TextView)view;
      if (textView.getText().equals(text)) {
        Robolectric.shadowOf(blockView).performClick();
        return true;
      }
    }
    else if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup)view;
      for (int i = 0; i < group.getChildCount(); i++) {
        boolean found = recursiveClickBlockWithTextView(group.getChildAt(i), blockView, textViewId, text);
        if (found) {
          return true;
        }
      }
    }
    return false;
  }

  public static String toString(View view) {
    StringBuilder builder = new StringBuilder();
    write(view, builder, 0);
    return builder.toString();
  }

  private static void write(View view, StringBuilder builder, int indent) {
    builder.append(Strings.repeat("  ", indent))
      .append(view.getClass().getSimpleName());
    writeParams(view, builder);
    builder.append('\n');
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup)view;
      for (int i = 0; i < group.getChildCount(); i++) {
        write(group.getChildAt(i), builder, indent + 1);
      }
    }
  }

  private static void writeParams(View view, StringBuilder builder) {
    if (view instanceof TextView) {
      builder.append(" [").append(((TextView)view).getText()).append("]");
    }
    else if (view instanceof TabHost) {
      int currentTab = ((TabHost)view).getCurrentTab();
      builder.append(" [tab=").append(currentTab).append("]");
    }
  }

  public static void dumpText(View view) {
    parse(view, new BlockParser() {

      int indent = 0;

      public void start(int id) {
        indent += 1;
      }

      public void end(int id) {
        indent -= 1;
      }

      public void processText(int id, TextView textView) {
        System.out.println(Strings.repeat(" ", indent) + textView.getText());
      }

      public void complete() {
      }
    });
  }
}
