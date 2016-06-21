package com.budgetview.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.budgetview.shared.model.AccountEntity;

import org.globsframework.model.Glob;

public class AccountSummaryBlockView extends LinearLayout {
    public AccountSummaryBlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.account_summary_block, this);
    }

    public void update(Glob accountEntity) {
        if (accountEntity == null) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        Views.setText(this, R.id.positionAmount, accountEntity.get(AccountEntity.POSITION));
        Views.setText(this, R.id.positionDate, Text.toOnDayMonthString(accountEntity.get(AccountEntity.POSITION_DAY),
                accountEntity.get(AccountEntity.POSITION_MONTH),
                getResources()));
    }
}
