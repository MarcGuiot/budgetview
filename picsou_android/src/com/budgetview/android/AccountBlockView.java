package com.budgetview.android;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.budgetview.android.components.TabPage;
import com.budgetview.android.utils.TransactionSet;
import com.budgetview.shared.model.AccountEntity;
import org.globsframework.model.Glob;

public class AccountBlockView extends LinearLayout {

  public AccountBlockView(Context context, AttributeSet attrs) {
    super(context, attrs);

    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.account_block, this);
  }

  public void update(int monthId, Glob accountEntity, FragmentActivity activity) {
    update(this, monthId, accountEntity, activity);
  }

  public static void update(View view, final int monthId, final Glob accountEntity, final FragmentActivity activity) {
    Views.setText(view, R.id.accountLabel, accountEntity.get(AccountEntity.LABEL));
    Double position = accountEntity.get(AccountEntity.POSITION);
    Views.setColoredText(view, R.id.accountPosition, position);
    Views.setText(view, R.id.accountPositionDate, Text.toOnDayMonthString(accountEntity.get(AccountEntity.POSITION_DAY),
                                                                          accountEntity.get(AccountEntity.POSITION_MONTH),
                                                                          activity.getResources()));

    view.findViewById(R.id.accountBlock).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Intent intent = new Intent(activity, TransactionListActivity.class);

        TransactionSet transactionSet =
          new TransactionSet(monthId, null, accountEntity.get(AccountEntity.ID),
                             ((App)activity.getApplication()).getRepository());
        transactionSet.save(intent);
        TabPage.copyDemoMode(activity, intent);
        activity.startActivity(intent);
      }
    });

  }
}
