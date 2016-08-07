package com.budgetview.cloud.functests;

import com.budgetview.cloud.functests.utils.CloudTestCase;

public class BudgeaCallbackTest extends CloudTestCase {

  public void test() throws Exception {
    budgea.callWebhook("{\n" +
                       "   \"connections\" : [\n" +
                       "      {\n" +
                       "         \"accounts\" : [\n" +
                       "            {\n" +
                       "               \"balance\" : 7481.01,\n" +
                       "               \"currency\" : {\n" +
                       "                  \"symbol\" : \"€\",\n" +
                       "                  \"id\" : \"EUR\",\n" +
                       "                  \"prefix\" : false\n" +
                       "               },\n" +
                       "               \"deleted\" : null,\n" +
                       "               \"display\" : true,\n" +
                       "               \"formatted_balance\" : \"7 481,01 €\",\n" +
                       "               \"iban\" : \"FR76131048379405300290000016\",\n" +
                       "               \"id\" : 17,\n" +
                       "               \"id_connection\" : 7,\n" +
                       "               \"investments\" : [\n" +
                       "                  {\n" +
                       "                     \"code\" : \"FR0010330902\",\n" +
                       "                     \"description\" : \"\",\n" +
                       "                     \"diff\" : -67.86,\n" +
                       "                     \"id\" : 55,\n" +
                       "                     \"id_account\" : 19,\n" +
                       "                     \"id_type\" : 1,\n" +
                       "                     \"label\" : \"Agressor PEA\",\n" +
                       "                     \"portfolio_share\" : 0.48,\n" +
                       "                     \"prev_diff\" : 2019.57,\n" +
                       "                     \"quantity\" : 7.338,\n" +
                       "                     \"type\" : {\n" +
                       "                        \"color\" : \"AABBCC\",\n" +
                       "                        \"id\" : 1,\n" +
                       "                        \"name\" : \"Fonds action\"\n" +
                       "                     },\n" +
                       "                     \"unitprice\" : 488.98,\n" +
                       "                     \"unitvalue\" : 479.73,\n" +
                       "                     \"valuation\" : 3520.28\n" +
                       "                  }\n" +
                       "               ],\n" +
                       "               \"last_update\" : \"2015-07-04 15:17:30\",\n" +
                       "               \"name\" : \"Compte chèque\",\n" +
                       "               \"number\" : \"3002900000\",\n" +
                       "               \"transactions\" : [\n" +
                       "                  {\n" +
                       "                     \"active\" : true,\n" +
                       "                     \"application_date\" : \"2015-06-17\",\n" +
                       "                     \"coming\" : false,\n" +
                       "                     \"comment\" : null,\n" +
                       "                     \"commission\" : null,\n" +
                       "                     \"country\" : null,\n" +
                       "                     \"date\" : \"2015-06-18\",\n" +
                       "                     \"date_scraped\" : \"2015-07-04 15:17:30\",\n" +
                       "                     \"deleted\" : null,\n" +
                       "                     \"documents_count\" : 0,\n" +
                       "                     \"formatted_value\" : \"-16,22 €\",\n" +
                       "                     \"id\" : 309,\n" +
                       "                     \"id_account\" : 17,\n" +
                       "                     \"id_category\" : 9998,\n" +
                       "                     \"id_cluster\" : null,\n" +
                       "                     \"last_update\" : \"2015-07-04 15:17:30\",\n" +
                       "                     \"new\" : true,\n" +
                       "                     \"original_currency\" : null,\n" +
                       "                     \"original_value\" : null,\n" +
                       "                     \"original_wording\" : \"FACTURE CB HALL'S BEER\",\n" +
                       "                     \"rdate\" : \"2015-06-17\",\n" +
                       "                     \"simplified_wording\" : \"HALL'S BEER\",\n" +
                       "                     \"state\" : \"parsed\",\n" +
                       "                     \"stemmed_wording\" : \"HALL'S BEER\",\n" +
                       "                     \"type\" : \"card\",\n" +
                       "                     \"value\" : -16.22,\n" +
                       "                     \"wording\" : \"HALL'S BEER\"\n" +
                       "                  }\n" +
                       "               ],\n" +
                       "               \"type\" : \"checking\"\n" +
                       "            }\n" +
                       "         ],\n" +
                       "         \"bank\" : {\n" +
                       "            \"id_weboob\" : \"ing\",\n" +
                       "            \"charged\" : true,\n" +
                       "            \"name\" : \"ING Direct\",\n" +
                       "            \"id\" : 7,\n" +
                       "            \"hidden\" : false\n" +
                       "         },\n" +
                       "         \"error\" : null,\n" +
                       "         \"expire\" : null,\n" +
                       "         \"id\" : 7,\n" +
                       "         \"id_user\" : 7,\n" +
                       "         \"id_bank\" : 41,\n" +
                       "         \"last_update\" : \"2015-07-04 15:17:31\"\n" +
                       "      }\n" +
                       "   ],\n" +
                       "   \"total\" : 1,\n" +
                       "   \"user\" : {\n" +
                       "      \"signin\" : \"2015-07-04 15:17:29\",\n" +
                       "      \"id\" : 7,\n" +
                       "      \"platform\" : \"sharedAccess\"\n" +
                       "   }\n" +
                       "}");
  }
}
