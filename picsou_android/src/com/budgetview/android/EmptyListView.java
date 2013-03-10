package com.budgetview.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.budgetview.shared.model.AccountEntity;
import org.globsframework.model.Glob;

public class EmptyListView extends LinearLayout {
  public EmptyListView(Context context, AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.empty_list, this);
  }

  public void update(int stringResourceId) {
    Views.setText(this, R.id.emptyListMessage, getResources().getText(stringResourceId));
  }
}