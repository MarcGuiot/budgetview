package com.budgetview.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.budgetview.android.components.GaugeView;
import com.budgetview.android.components.TabPage;
import com.budgetview.android.utils.TransactionSet;
import com.budgetview.shared.gui.gauge.GaugeModel;
import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.SeriesEntity;
import com.budgetview.shared.model.SeriesValues;
import org.globsframework.model.Glob;

public class SeriesBlockView extends LinearLayout {

  public SeriesBlockView(Context context, AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.series_block, this);
  }

  public void update(final int monthId, Glob seriesEntity, final Glob seriesValues, final Activity activity) {

    Views.setText(this, R.id.seriesLabel, seriesEntity.get(SeriesEntity.NAME));

    boolean uncategorized = BudgetAreaEntity.isUncategorized(seriesEntity.get(SeriesEntity.BUDGET_AREA));
    updateAmounts(this, activity, seriesValues, uncategorized);

    final App app = (App)activity.getApplication();
    findViewById(R.id.seriesBlock).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Intent intent = new Intent(activity, TransactionListActivity.class);
        TransactionSet transactionSet =
          new TransactionSet(monthId, seriesValues.get(SeriesValues.SERIES_ENTITY), null, app.getRepository());
        transactionSet.save(intent);
        TabPage.copyDemoMode(activity, intent);
        activity.startActivity(intent);
      }
    });

  }

  private static void updateAmounts(View parentView, Activity activity, Glob seriesValues, boolean uncategorized) {
    final App app = (App)activity.getApplication();
    Glob budgetAreaEntity = app.getRepository().findLinkTarget(seriesValues, SeriesValues.BUDGET_AREA);
    boolean shouldInvert = budgetAreaEntity.get(BudgetAreaEntity.INVERT_AMOUNTS);

    Views.setText(parentView, R.id.seriesActual, seriesValues.get(SeriesValues.AMOUNT), shouldInvert);
    if (!uncategorized) {
      Views.setText(parentView, R.id.seriesPlanned, seriesValues.get(SeriesValues.PLANNED_AMOUNT), shouldInvert);
    }
    else {
      parentView.findViewById(R.id.seriesSeparator).setVisibility(GONE);
      parentView.findViewById(R.id.seriesPlanned).setVisibility(GONE);
    }
    GaugeView gaugeView = (GaugeView)parentView.findViewById(R.id.seriesGauge);
    GaugeModel gaugeModel = gaugeView.getModel();
    gaugeModel.setInvertAll(uncategorized);
    gaugeModel.setValues(seriesValues.get(SeriesValues.AMOUNT, 0.00),
                         seriesValues.get(SeriesValues.PLANNED_AMOUNT, 0.00),
                         seriesValues.get(SeriesValues.OVERRUN_AMOUNT, 0.00),
                         seriesValues.get(SeriesValues.REMAINING_AMOUNT, 0.00),
                         "", false);
  }
}
