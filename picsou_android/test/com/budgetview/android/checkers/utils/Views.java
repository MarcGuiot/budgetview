package com.budgetview.android.checkers.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import junit.framework.Assert;
import org.robolectric.Robolectric;

import java.io.*;

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
      for (int i  = 0; i < group.getChildCount(); i++) {
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
      for (int i  = 0; i < group.getChildCount(); i++) {
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
      for (int i  = 0; i < group.getChildCount(); i++) {
        boolean found = recursiveClickBlockWithTextView(group.getChildAt(i), blockView, textViewId, text);
        if (found) {
          return true;
        }
      }
    }
    return false;
  }

  private static String toString(View view) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream stream = new PrintStream(baos);
    Robolectric.shadowOf(view).dump(stream, 5);
    try {
      return baos.toString("UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
