package com.budgetview.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.utils.exceptions.InvalidParameter;

public abstract class Block {

  private int blockResourceId;

  public Block(int blockResourceId) {
    this.blockResourceId = blockResourceId;
  }

  public View getView(LayoutInflater inflater, View previousView, ViewGroup parent) {
    View view = previousView;
    if (view == null || !isProperViewType(view)) {
      view = inflater.inflate(blockResourceId, parent, false);
    }
    populateView(view);
    return view;
  }

  protected abstract boolean isProperViewType(View view);

  protected abstract void populateView(View view);

  protected void setText(View view, int textId, Double value) {
    setText(view, textId, AmountFormat.DECIMAL_FORMAT.format(value));
  }

  protected void setText(View view, int textId, CharSequence text) {
    TextView textView = (TextView)view.findViewById(textId);
    if (textView == null) {
      throw new InvalidParameter("Resource " + textId + " not found in view");
    }
    textView.setText(text);
  }
}
