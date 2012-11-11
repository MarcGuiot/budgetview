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
}
