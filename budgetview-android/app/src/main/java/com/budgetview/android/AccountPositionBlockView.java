package com.budgetview.android;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.budgetview.android.components.DailyChartPainter;
import com.budgetview.android.components.DailyChartStyles;
import com.budgetview.android.components.DailyChartView;
import com.budgetview.android.components.DailyDataset;
import com.budgetview.android.utils.AbstractBlock;
import com.budgetview.shared.model.AccountEntity;
import com.budgetview.shared.model.AccountPosition;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class AccountPositionBlockView extends AbstractBlock {
    private final Integer accountEntityId;
    private final int monthId;
    private final FragmentActivity activity;

    public AccountPositionBlockView(Integer accountEntityId, int monthId, FragmentActivity activity) {
        super(R.layout.account_positions_block);
        this.accountEntityId = accountEntityId;
        this.monthId = monthId;
        this.activity = activity;
    }

    protected boolean isProperViewType(View view) {
        return view.findViewById(R.id.dailyChart) != null;
    }

    protected void populateView(View view) {

        DailyChartView chart = (DailyChartView) view.findViewById(R.id.dailyChart);

        App app = (App) activity.getApplication();
        GlobRepository repository = app.getRepository();

        Glob accountEntity = repository.find(Key.create(AccountEntity.TYPE, accountEntityId));
        if (accountEntity == null) {
            view.setVisibility(View.GONE);
            return;
        }

        view.setVisibility(View.VISIBLE);
        Integer positionMonth = accountEntity.get(AccountEntity.POSITION_MONTH);
        Integer positionDay = accountEntity.get(AccountEntity.POSITION_DAY);
        DailyDataset dataset = new DailyDataset(positionMonth, positionDay,
                Text.toOnDayMonthString(positionDay, positionMonth, activity.getResources()));

        final GlobList positions = repository.getAll(AccountPosition.TYPE,
                and(fieldEquals(AccountPosition.ACCOUNT, accountEntityId),
                        fieldEquals(AccountPosition.MONTH, monthId)
                )).sort(AccountPosition.DAY);
        if (positions.isEmpty()) {
            view.setVisibility(View.GONE);
            return;
        }

        view.setVisibility(View.VISIBLE);
        Double[] values = new Double[positions.size()];
        int i = 0;
        for (Glob position : positions) {
            values[i++] = position.get(AccountPosition.POSITION);
        }
        dataset.add(monthId, values, "label", "section", true, false, new boolean[values.length]);

        chart.update(new DailyChartPainter(dataset, new DailyChartStyles(activity.getResources())));
    }
}
