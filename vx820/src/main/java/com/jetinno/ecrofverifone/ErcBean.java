package com.jetinno.ecrofverifone;

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by zhangyuncai on 2021/10/8.
 */
public class ErcBean implements Serializable {
    private String cmd;
    private String result;//ok/err
    private String detail;
    private String compile_time;
    private String trans_amount;//退款字段
    private String card_indicator;//退款字段

    public static ErcBean parseJson(String json) {
        ErcBean bean = new ErcBean();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("cmd")) {
                bean.cmd = jsonObject.getString("cmd");
            }
            if (jsonObject.has("result")) {
                bean.result = jsonObject.getString("result");
            }
            if (jsonObject.has("detail")) {
                bean.detail = jsonObject.getString("detail");
            }
            if (jsonObject.has("compile_time")) {
                bean.compile_time = jsonObject.getString("compile_time");
            }
            if (jsonObject.has("trans_amount")) {
                bean.trans_amount = jsonObject.getString("trans_amount");
            }
            if (jsonObject.has("card_indicator")) {
                bean.card_indicator = jsonObject.getString("card_indicator");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    public String getCard_indicator() {
        return card_indicator;
    }

    public String getTrans_amount() {
        return trans_amount;
    }

    public String getCmd() {
        return cmd;
    }

    public boolean isOk() {
        return TextUtils.equals("ok", result);
    }

    public String getCompile_time() {
        return compile_time;
    }

    public String getDetail() {
        return detail;
    }

}
