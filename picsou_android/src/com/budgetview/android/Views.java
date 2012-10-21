package com.budgetview.android;

import android.view.View;
import android.widget.TextView;

public class Views {
  public static void setColorAmount(View view, int componentId, double amount) {

    int colorId = amount < 0 ? R.color.amount_negative : R.color.amount_positive;
    setTextColor(view, componentId, colorId);
  }

  public static void setTextColor(View view, int componentId, int colorId) {
    int color = view.getResources().getColor(colorId);
    TextView textView = (TextView)view.findViewById(componentId);
    textView.setTextColor(color);
  }
}
