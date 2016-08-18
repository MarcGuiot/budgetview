package com.budgetview.server.cloud.stub;

public class BudgeaSamples {

  public static String firstWebhookCall(int userId) {
    return "{" +
           "  'connections':[" +
           "    {" +
           "      'id_user':" + userId + "," +
           "      'subscriptions':[" +
           "      ]," +
           "      'id_bank':40," +
           "      'last_update':'2016-08-10 17:44:28'," +
           "      'expire':null," +
           "      'accounts':[" +
           "        {" +
           "          'last_update':'2016-08-10 17:44:26'," +
           "          'formatted_balance':'9346,71 €'," +
           "          'name':'Compte chèque'," +
           "          'transactions':[" +
           "            {" +
           "              'comment':null," +
           "              'webid':null," +
           "              'date':'2016-08-10'," +
           "              'simplified_wording':'GREEN ET GREEN PARIS'," +
           "              'id':23573," +
           "              'category':{" +
           "                'id_user':null," +
           "                'name':'Indéfini'," +
           "                'accountant_account':null," +
           "                'id_parent_category_in_menu':null," +
           "                'color':'D7D3BC'," +
           "                'id_parent_category':null," +
           "                'refundable':false," +
           "                'id_logo':null," +
           "                'income':null," +
           "                'name_displayed':'Indéfini'," +
           "                'hidden':false," +
           "                'id':9998" +
           "              }," +
           "              'original_value':null," +
           "              'commission':null," +
           "              'original_wording':'FACTURE CB GREEN ET GREEN PARIS'," +
           "              'id_account':614," +
           "              'id_cluster':null," +
           "              'last_update':'2016-08-10 17:44:26'," +
           "              'original_currency':null," +
           "              'state':'parsed'," +
           "              'new':true," +
           "              'type':'card'," +
           "              'id_category':9998," +
           "              'deleted':null," +
           "              'nopurge':false," +
           "              'formatted_value':'-96,02 €'," +
           "              'rdate':'2016-08-09'," +
           "              'coming':false," +
           "              'active':true," +
           "              'application_date':'2016-08-09'," +
           "              'date_scraped':'2016-08-10 17:44:26'," +
           "              'country':null," +
           "              'value':-96.02," +
           "              'documents_count':0," +
           "              'stemmed_wording':'GREEN ET GREEN PARIS'," +
           "              'wording':'GREEN ET GREEN PARIS'" +
           "            }" +
           "          ]," +
           "          'deleted':null," +
           "          'id_connection':266," +
           "          'original_name':'Compte chèque'," +
           "          'number':'3002900000'," +
           "          'display':true," +
           "          'currency':{" +
           "            'symbol':'€'," +
           "            'prefix':false," +
           "            'id':'EUR'" +
           "          }," +
           "          'iban':'FR7613696539985300290000088'," +
           "          'coming':null," +
           "          'investments':[" +
           "          ]," +
           "          'balance':9346.71," +
           "          'type':'checking'," +
           "          'id':614" +
           "        }," +
           "        {" +
           "          'last_update':'2016-08-10 17:44:27'," +
           "          'formatted_balance':'998,64 €'," +
           "          'name':'Livret A'," +
           "          'transactions':[" +
           "            {" +
           "              'comment':null," +
           "              'webid':null," +
           "              'date':'2016-08-10'," +
           "              'simplified_wording':'GREEN ET GREEN PARIS'," +
           "              'id':23576," +
           "              'category':{" +
           "                'id_user':null," +
           "                'name':'Indéfini'," +
           "                'accountant_account':null," +
           "                'id_parent_category_in_menu':null," +
           "                'color':'D7D3BC'," +
           "                'id_parent_category':null," +
           "                'refundable':false," +
           "                'id_logo':null," +
           "                'income':null," +
           "                'name_displayed':'Indéfini'," +
           "                'hidden':false," +
           "                'id':9998" +
           "              }," +
           "              'original_value':null," +
           "              'commission':null," +
           "              'original_wording':'FACTURE CB GREEN ET GREEN PARIS'," +
           "              'id_account':617," +
           "              'id_cluster':null," +
           "              'last_update':'2016-08-10 17:44:27'," +
           "              'original_currency':null," +
           "              'state':'parsed'," +
           "              'new':true," +
           "              'type':'card'," +
           "              'id_category':9998," +
           "              'deleted':null," +
           "              'nopurge':false," +
           "              'formatted_value':'-96,02 €'," +
           "              'rdate':'2016-08-09'," +
           "              'coming':false," +
           "              'active':true," +
           "              'application_date':'2016-08-09'," +
           "              'date_scraped':'2016-08-10 17:44:27'," +
           "              'country':null," +
           "              'value':-96.02," +
           "              'documents_count':0," +
           "              'stemmed_wording':'GREEN ET GREEN PARIS'," +
           "              'wording':'GREEN ET GREEN PARIS'" +
           "            }" +
           "          ]," +
           "          'deleted':null," +
           "          'id_connection':266," +
           "          'original_name':'Livret A'," +
           "          'number':'3002900001'," +
           "          'display':true," +
           "          'currency':{" +
           "            'symbol':'€'," +
           "            'prefix':false," +
           "            'id':'EUR'" +
           "          }," +
           "          'iban':'FR7613696539985300290000185'," +
           "          'coming':null," +
           "          'investments':[" +
           "          ]," +
           "          'balance':998.64," +
           "          'type':'savings'," +
           "          'id':617" +
           "        }," +
           "        {" +
           "          'number':'3002900002'," +
           "          'currency':{" +
           "            'symbol':'€'," +
           "            'prefix':false," +
           "            'id':'EUR'" +
           "          }," +
           "          'diff':31.59," +
           "          'id':620," +
           "          'formatted_balance':'1509,28 €'," +
           "          'id_connection':266," +
           "          'original_name':'Assurance Vie'," +
           "          'last_update':'2016-08-10 17:44:27'," +
           "          'prev_diff_percent':0.502," +
           "          'transactions':[" +
           "          ]," +
           "          'deleted':null," +
           "          'iban':'FR7613696539985300290000282'," +
           "          'coming':null," +
           "          'valuation':1509.28," +
           "          'name':'Assurance Vie'," +
           "          'type':'lifeinsurance'," +
           "          'calculated':[" +
           "            'diff'," +
           "            'diff_percent'," +
           "            'prev_diff'," +
           "            'prev_diff_percent'," +
           "            'valuation'" +
           "          ]," +
           "          'prev_diff':504.73," +
           "          'investments':[" +
           "            {" +
           "              'code':'FR0010330902'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.2105," +
           "              'diff':4.28," +
           "              'unitvalue':43.39," +
           "              'id':1564," +
           "              'original_unitprice':null," +
           "              'id_account':620," +
           "              'label':'Agressor PEA'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':317.65," +
           "              'id_security':4," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':0.083," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':24.40," +
           "              'quantity':7.3210," +
           "              'unitprice':42.80," +
           "              'diff_percent':0.014" +
           "            }," +
           "            {" +
           "              'code':'LU0304955437'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.1057," +
           "              'diff':17.20," +
           "              'unitvalue':29.60," +
           "              'id':1567," +
           "              'original_unitprice':null," +
           "              'id_account':620," +
           "              'label':'EDGEWOOD L SEL US SEL GW'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':159.53," +
           "              'id_security':7," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':-0.68," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':-338.38," +
           "              'quantity':5.3900," +
           "              'unitprice':26.41," +
           "              'diff_percent':0.121" +
           "            }," +
           "            {" +
           "              'code':'FR0010773036'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.0985," +
           "              'diff':14.88," +
           "              'unitvalue':26.11," +
           "              'id':1570," +
           "              'original_unitprice':null," +
           "              'id_account':620," +
           "              'label':'EDR GLOBAL CONVERTIBLES'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':148.61," +
           "              'id_security':10," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':-0.005," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':-0.76," +
           "              'quantity':5.6920," +
           "              'unitprice':23.50," +
           "              'diff_percent':0.111" +
           "            }," +
           "            {" +
           "              'code':'FR0010479931'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.0537," +
           "              'diff':3.30," +
           "              'unitvalue':75.80," +
           "              'id':1573," +
           "              'original_unitprice':null," +
           "              'id_account':620," +
           "              'label':'EDR India'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':81.03," +
           "              'id_security':13," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':0.808," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':36.22," +
           "              'quantity':1.0690," +
           "              'unitprice':72.71," +
           "              'diff_percent':0.042" +
           "            }," +
           "            {" +
           "              'code':'XXEUROSSIMA'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.2274," +
           "              'diff':null," +
           "              'unitvalue':null," +
           "              'id':1576," +
           "              'original_unitprice':null," +
           "              'id_account':620," +
           "              'label':'Fonds en euros (Eurossima)'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':343.15," +
           "              'id_security':16," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':38.443," +
           "              'calculated':[" +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':334.45," +
           "              'quantity':null," +
           "              'unitprice':null," +
           "              'diff_percent':null" +
           "            }," +
           "            {" +
           "              'code':'LU0119345287'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.3043," +
           "              'diff':-8.07," +
           "              'unitvalue':155.65," +
           "              'id':1579," +
           "              'original_unitprice':null," +
           "              'id_account':620," +
           "              'label':'Pioneer Funds Euroland E'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':459.31," +
           "              'id_security':19," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':42.702," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':448.80," +
           "              'quantity':2.9510," +
           "              'unitprice':158.38," +
           "              'diff_percent':-0.017" +
           "            }" +
           "          ]," +
           "          'balance':1509.28," +
           "          'display':true," +
           "          'diff_percent':0.021" +
           "        }," +
           "        {" +
           "          'number':'3002900003'," +
           "          'currency':{" +
           "            'symbol':'€'," +
           "            'prefix':false," +
           "            'id':'EUR'" +
           "          }," +
           "          'diff':-249.15," +
           "          'id':623," +
           "          'formatted_balance':'7642,80 €'," +
           "          'id_connection':266," +
           "          'original_name':'Comptes titres'," +
           "          'last_update':'2016-08-10 17:44:28'," +
           "          'prev_diff_percent':0.28," +
           "          'transactions':[" +
           "          ]," +
           "          'deleted':null," +
           "          'iban':'FR7613696539985300290000379'," +
           "          'coming':null," +
           "          'valuation':7642.80," +
           "          'name':'Comptes titres'," +
           "          'type':'market'," +
           "          'calculated':[" +
           "            'diff'," +
           "            'diff_percent'," +
           "            'prev_diff'," +
           "            'prev_diff_percent'," +
           "            'valuation'" +
           "          ]," +
           "          'prev_diff':1672.29," +
           "          'investments':[" +
           "            {" +
           "              'code':'FR0010330902'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.5731," +
           "              'diff':-22.89," +
           "              'unitvalue':1448.01," +
           "              'id':1582," +
           "              'original_unitprice':null," +
           "              'id_account':623," +
           "              'label':'Agressor PEA'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':4380.23," +
           "              'id_security':4," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':0.658," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':1738.59," +
           "              'quantity':3.0250," +
           "              'unitprice':1455.58," +
           "              'diff_percent':-0.005" +
           "            }," +
           "            {" +
           "              'code':'LU0304955437'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.2988," +
           "              'diff':-70.39," +
           "              'unitvalue':284.98," +
           "              'id':1585," +
           "              'original_unitprice':null," +
           "              'id_account':623," +
           "              'label':'EDGEWOOD L SEL US SEL GW'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':2283.80," +
           "              'id_security':7," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':-0.02," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':-46.41," +
           "              'quantity':8.0140," +
           "              'unitprice':293.76," +
           "              'diff_percent':-0.03" +
           "            }," +
           "            {" +
           "              'code':'FR0010773036'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.0896," +
           "              'diff':-83.23," +
           "              'unitvalue':70.01," +
           "              'id':1588," +
           "              'original_unitprice':null," +
           "              'id_account':623," +
           "              'label':'EDR GLOBAL CONVERTIBLES'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':685.14," +
           "              'id_security':10," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':4.573," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':562.20," +
           "              'quantity':9.7860," +
           "              'unitprice':78.52," +
           "              'diff_percent':-0.108" +
           "            }," +
           "            {" +
           "              'code':'FR0010479931'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.0008," +
           "              'diff':-72.03," +
           "              'unitvalue':0.63," +
           "              'id':1591," +
           "              'original_unitprice':null," +
           "              'id_account':623," +
           "              'label':'EDR India'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':5.87," +
           "              'id_security':13," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':-0.99," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':-607.13," +
           "              'quantity':9.3790," +
           "              'unitprice':8.31," +
           "              'diff_percent':-0.925" +
           "            }," +
           "            {" +
           "              'code':'XXEUROSSIMA'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.0264," +
           "              'diff':null," +
           "              'unitvalue':null," +
           "              'id':1594," +
           "              'original_unitprice':null," +
           "              'id_account':623," +
           "              'label':'Fonds en euros (Eurossima)'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':201.43," +
           "              'id_security':16," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':6.469," +
           "              'calculated':[" +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':174.46," +
           "              'quantity':null," +
           "              'unitprice':null," +
           "              'diff_percent':null" +
           "            }," +
           "            {" +
           "              'code':'LU0119345287'," +
           "              'prev_vdate':'2016-08-09'," +
           "              'original_valuation':null," +
           "              'portfolio_share':0.0113," +
           "              'diff':-0.61," +
           "              'unitvalue':29.70," +
           "              'id':1597," +
           "              'original_unitprice':null," +
           "              'id_account':623," +
           "              'label':'Pioneer Funds Euroland E'," +
           "              'type':null," +
           "              'description':''," +
           "              'original_unitvalue':null," +
           "              'original_currency':null," +
           "              'original_diff':null," +
           "              'id_type':null," +
           "              'valuation':86.33," +
           "              'id_security':19," +
           "              'vdate':'2016-08-10'," +
           "              'prev_diff_percent':-0.634," +
           "              'calculated':[" +
           "                'diff_percent'," +
           "                'prev_diff_percent'" +
           "              ]," +
           "              'prev_diff':-149.42," +
           "              'quantity':2.9070," +
           "              'unitprice':29.91," +
           "              'diff_percent':-0.007" +
           "            }" +
           "          ]," +
           "          'balance':7642.80," +
           "          'display':true," +
           "          'diff_percent':-0.032" +
           "        }" +
           "      ]," +
           "      'error':null," +
           "      'active':true," +
           "      'id':266," +
           "      'bank':{" +
           "        'id_category':null," +
           "        'code':null," +
           "        'name':'Connecteur de test'," +
           "        'capabilities':'set([])'," +
           "        'beta':false," +
           "        'hidden':false," +
           "        'id':40," +
           "        'charged':false" +
           "      }" +
           "    }" +
           "  ]," +
           "  'total':1," +
           "  'user':{" +
           "    'invites':[" +
           "    ]," +
           "    'signin':'2016-08-07 15:38:46'," +
           "    'platform':'sharedAccess'," +
           "    'alert_settings':{" +
           "      'type':'transactions'," +
           "      'balance_min2':0.00," +
           "      'balance_min1':500.00," +
           "      'balance_max':10000.00," +
           "      'enabled':true," +
           "      'date_range':null," +
           "      'income_max':500.00," +
           "      'value_type':'flat'," +
           "      'accounts':null," +
           "      'resume_enabled':true," +
           "      'apply':null," +
           "      'expense_max':500.00," +
           "      'id':407" +
           "    }," +
           "    'config':{" +
           "      'biapi.last_push':'2016-08-08 14:09:26'" +
           "    }," +
           "    'id':" + userId +
           "  }" +
           "}";
  }
}
