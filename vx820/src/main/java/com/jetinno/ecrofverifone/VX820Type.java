package com.jetinno.ecrofverifone;

/**
 * Created by zhangyuncai on 2021/9/27.
 */
public enum VX820Type {
    mdb_card("N","mdb_card"),//信用卡
    mdb_elec("E","mdb_elec"),//电子卡
    mdb_nfc("C","mdb_nfc");//NFC支付卡

    private String indicator = null;
    private String payTypeName = null;

    VX820Type(String indicator,String payTypeName) {
        this.indicator = indicator;
        this.payTypeName = payTypeName;
    }

    public String getIndicator() {
        return indicator;
    }

    public String getPayTypeName() {
        return payTypeName;
    }
}
