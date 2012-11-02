package com.budgetview.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

  public static void showError(Activity activity, int messageId) {
    showError(activity, messageId, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
  }

  public static void showError(final Activity activity, int messageId, DialogInterface.OnClickListener listener) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setMessage(activity.getResources().getText(messageId))
      .setCancelable(false)
      .setPositiveButton("OK", listener);
    AlertDialog alert = builder.create();
    alert.show();
  }

}
