package com.jetinno.ecrofverifone;

/**
 * Created by zhangyuncai.
 * Date: 2022/11/9
 */
public interface Vx820PayListener {
    /**
     * 支付开始
     *
     * @param cardIndicator 多元卡大概类型,例如信用卡,电子票证
     * @param payTypeName VX820Type
     */
    void onStart(String cardIndicator,String payTypeName);

    /**
     * 支付中
     *
     * @param cardIndicator 多元卡大概类型,例如信用卡,电子票证
     * @param payTypeName VX820Type
     */
    void onProgress(String cardIndicator,String payTypeName);

    /**
     * 支付成功 {"cmd":"CC","result":"ok","detail":"Approval","trans_amount":"1.0","card_indicator":"CK_iPass","card_type":"iPASS"}
     *
     * @param cardIndicator 多元卡详细类型,例如电子票证中的iPass:CK_iPass
     * @param payTypeName VX820Type
     */
    void onSuccess(String cardIndicator,String payTypeName);

    /**
     * 支付失败
     *
     * @param cardIndicator 多元卡大概类型,例如信用卡,电子票证
     * @param payTypeName VX820Type
     * @param errMsg        支付失败消息
     */
    void onFail(String cardIndicator,String payTypeName, String errMsg);
}
