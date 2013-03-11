package com.budgetview.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.utils.exceptions.InvalidParameter;

public class Views {

  public static void setColoredText(View view, int textId, Double value) {
    setText(view, textId, AmountFormat.DECIMAL_FORMAT.format(value));
    setColorAmount(view, textId, value);
  }

  public static void setText(View view, int textId, Double value) {
    String text = (value == null) ? "-" : AmountFormat.DECIMAL_FORMAT.format(value);
    setText(view, textId, text);
  }

  public static void setText(View view, int textId, CharSequence text) {
    TextView textView = (TextView)view.findViewById(textId);
    if (textView == null) {
      throw new InvalidParameter("Resource " + textId + " not found in view");
    }
    textView.setText(text);
  }

  private static void setColorAmount(View view, int componentId, double amount) {

    int colorId = amount < 0 ? R.color.amount_negative : R.color.amount_positive;
    setTextColor(view, componentId, colorId);
  }

  public static void setTextColor(View view, int componentId, int colorId) {
    int color = view.getResources().getColor(colorId);
    TextView textView = (TextView)view.findViewById(componentId);
    textView.setTextColor(color);
  }

  public static void showAlert(Context context, int messageId) {
    showAlert(context, messageId, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
  }

  public static void showAlert(final Context context, int messageId, DialogInterface.OnClickListener listener) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage(context.getResources().getText(messageId))
      .setCancelable(false)
      .setPositiveButton("OK", listener);
    AlertDialog alert = builder.create();
    alert.show();
  }

  public static void setDate(View view, int transaction_date, Integer monthId, Integer day, Resources resources) {
    setText(view, transaction_date, Text.toOnDayMonthString(day, monthId, resources));
  }
}
