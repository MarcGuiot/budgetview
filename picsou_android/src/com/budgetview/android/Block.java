package com.budgetview.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface Block {
  View getView(LayoutInflater inflater, View previousView, ViewGroup parent);
}
