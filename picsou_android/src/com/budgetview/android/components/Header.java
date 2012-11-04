package com.budgetview.android.components;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.budgetview.android.R;

public class Header extends LinearLayout {

  private Activity activity;

  public Header(Context context) {
    super(context);
    init(context);
  }

  public Header(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public Header(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    LayoutInflater inflater = (LayoutInflater)
      context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.header, this, true);
    setBackVisible(View.INVISIBLE);
  }

  public void setTitle(String title) {
    TextView button = (TextView)findViewById(R.id.header_title);
    button.setText(title);
  }

  public void setActivity(final Activity activity) {
    this.activity = activity;
    setBackVisible(View.VISIBLE);
    setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        activity.finish();
      }
    });
  }

  private void setBackVisible(int visible) {
    ImageView button = (ImageView)findViewById(R.id.header_back_arrow);
    button.setVisibility(visible);
  }
}
