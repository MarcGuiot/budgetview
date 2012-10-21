package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.budgetview.shared.model.AccountEntity;
import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.SeriesValues;
import com.budgetview.shared.model.TransactionValues;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.utils.GlobMatchers.*;

public class TransactionListActivity extends Activity {

  public static String MONTH_PARAMETER = "transactionListActivity.parameters.month";
  public static String SERIES_VALUES_PARAMETER = "transactionListActivity.parameters.series";
  public static String ACCOUNT_PARAMETER = "transactionListActivity.parameters.account";

  private int monthId;
  private GlobMatcher matcher;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    monthId = intent.getIntExtra(MONTH_PARAMETER, -1);

    setContentView(R.layout.transaction_list);

    TextView monthText = (TextView)findViewById(R.id.transactionMonthLabel);
    monthText.setText(Text.monthToString(monthId, getResources()));

    GlobRepository repository = ((App)getApplication()).getRepository();
    String sectionLabel = "";
    if (intent.hasExtra(SERIES_VALUES_PARAMETER)) {
      int seriesValuesId = intent.getIntExtra(SERIES_VALUES_PARAMETER, -1);
      Glob seriesValues =
        repository.get(Key.create(SeriesValues.TYPE, seriesValuesId));
      Glob budgetAreaEntity = repository.findLinkTarget(seriesValues, SeriesValues.BUDGET_AREA);
      String budgetAreaLabel = budgetAreaEntity.get(BudgetAreaEntity.LABEL);
      sectionLabel = budgetAreaLabel + " - " + seriesValues.get(SeriesValues.NAME);

      matcher = fieldEquals(TransactionValues.SERIES_VALUES, seriesValuesId);
      Log.d("transactionList", "Series: " + seriesValuesId);
    }
    else if (intent.hasExtra(ACCOUNT_PARAMETER)) {
      int accountId = intent.getIntExtra(ACCOUNT_PARAMETER, -1);
      Glob accountEntity = repository.get(Key.create(AccountEntity.TYPE, accountId));
      sectionLabel = accountEntity.get(AccountEntity.LABEL);

      matcher = and(fieldEquals(TransactionValues.BANK_MONTH, monthId),
                    fieldEquals(TransactionValues.ACCOUNT, accountId));
      Log.d("transactionList", "Account: " + accountId);
    }
    else {
      throw new InvalidParameter("Missing filtering parameter");
    }

    TextView budgetAreaText = (TextView)findViewById(R.id.transactionSectionLabel);
    budgetAreaText.setText(sectionLabel);

    ListView list = (ListView)findViewById(R.id.transactionList);
    list.setAdapter(new TransactionListAdapter());
  }

  private class TransactionListAdapter extends BaseAdapter {

    private GlobList transactionValuesList;

    private TransactionListAdapter() {
      App app = (App)getApplication();
      transactionValuesList =
        app.getRepository()
          .getAll(TransactionValues.TYPE, matcher)
          .sort(TransactionValues.SEQUENCE_NUMBER);
    }

    public int getCount() {
      return transactionValuesList.size();
    }

    public Object getItem(int i) {
      return transactionValuesList.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {

      View view = previousView;
      if (view == null) {
        LayoutInflater inflater = TransactionListActivity.this.getLayoutInflater();
        view = inflater.inflate(R.layout.transaction_block, parent, false);
      }

      final Glob values = transactionValuesList.get(i);
      Double amount = values.get(TransactionValues.AMOUNT);
      setText(view, R.id.transactionLabel, values.get(TransactionValues.LABEL));
      setText(view, R.id.transactionAmount, amount);
      setText(view, R.id.transactionDate, getDate(values));

      if (values.isTrue(TransactionValues.PLANNED)) {
        Views.setTextColor(view, R.id.transactionLabel, R.color.item_label_disabled);
      }

      Views.setColorAmount(view, R.id.transactionAmount, amount);

      return view;
    }

    private String getDate(Glob values) {
      if (values.isTrue(TransactionValues.PLANNED)) {
        return Text.toPlannedOnDayMonthString(values.get(TransactionValues.BANK_DAY),
                                              values.get(TransactionValues.BANK_MONTH),
                                              getResources());
      }
      else {
        return Text.toOnDayMonthString(values.get(TransactionValues.BANK_DAY),
                                       values.get(TransactionValues.BANK_MONTH),
                                       getResources());
      }
    }

    private void setText(View view, int textId, Double value) {
      String text = (value == null) ? "-" : AmountFormat.DECIMAL_FORMAT.format(value);
      setText(view, textId, text);
    }

    private void setText(View view, int textId, String text) {
      TextView textView = (TextView)view.findViewById(textId);
      textView.setText(text);
    }
  }
}