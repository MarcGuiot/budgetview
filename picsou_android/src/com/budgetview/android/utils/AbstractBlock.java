package com.budgetview.android.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.budgetview.android.Block;

public abstract class AbstractBlock implements Block {
  private int blockResourceId;

  public AbstractBlock(int blockResourceId) {
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

  protected abstract void populateView(View view);

  protected abstract boolean isProperViewType(View view);
}
