package com.jetinno.ecrofverifone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by zhangyuncai on 2021/6/17.
 * 多元卡支付
 */
public class EcrWrapper {
    static {
        System.loadLibrary("ecrLib");
    }

    private final static String TAG = "EcrWrapper";
    private final static String FILE_NAME = "card";
    private final static String TOP_CHECKOUT_TIME = "time";
    /*VISA、M/C、JCB、CUP、AMEX、Discover_Card、
CK_YOYO、CK_iPass、CK_iCash、CK_HappyCash、
Twin_Card、Other_Card*/
    private static EcrWrapper _instance;

    public static EcrWrapper getInstance() {
        if (_instance == null) {
            synchronized (EcrWrapper.class) {
                if (_instance == null) {
                    _instance = new EcrWrapper();
                }
            }
        }
        return _instance;
    }

    private String cardIndicator = "";//当前交易卡具体类型
    private String payTypeName = "";//当前交易卡类型

    private Context context = null;
    private String sdkVersion = null;//sdk 版本
    private Vx820InfoListener vx820InfoListener = null;
    private Vx820PayListener vx820PayListener = null;
    private Handler mainHandler = null;

    public Handler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    ///获取sdk版本
    public String getSdkVersion() {
        return sdkVersion;
    }

    ///获取结账时间
    public long getTopCheckoutTime() {
        if (context != null) {
            return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getLong(TOP_CHECKOUT_TIME, -1);
        }
        return -1;
    }

    public void setVx820InfoListener(Vx820InfoListener vx820InfoListener) {
        this.vx820InfoListener = vx820InfoListener;
    }

    public void setVx820PayListener(Vx820PayListener vx820PayListener) {
        this.vx820PayListener = vx820PayListener;
    }

    ///保存结账时间
    private void updateTopCheckoutTime() {
        if (context != null) {
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit().putLong(TOP_CHECKOUT_TIME, new Date().getTime()).commit();
        }
    }

    /*
        1，2 表示已初始化过了
        0 表示初始化成功
        -1 表示设备路径参数为空
        -2 表示波特率不正确
        -3 表示奇偶校验参数为空
        -4 表示打开串口失败
        -5 表示获取串口信息失败
        -6 表示设备串口信息失败
        -7 表示启动串口任务失败
         */
    public native int ecrInit(String devPath, int baudRate, String parity);

    public native void ecrFini();

    public native int ecrAction(String json);

    ///判断当前交易类型
    public boolean equalsCardIndicator(String payType) {
        return TextUtils.equals(cardIndicator, payType);
    }

    public void unBind() {
        ecrFini();
    }

    public void bindCard(Context context) {
        this.context = context;
        int ecrInit = EcrWrapper.getInstance().ecrInit("/dev/ttyS1", 9600, "8N1");
        logOrder(String.format("ecrInit:%d", ecrInit));

        //开启定时结账,默认每天23:59分结账
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        currentHour = 23;
        currentMinute = 59;
        calendar.set(Calendar.HOUR_OF_DAY, currentHour);
        calendar.set(Calendar.MINUTE, currentMinute + 1);
        Intent intent = new Intent(context, CardCheckoutCast.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    /**
     * @param amount
     * @param timeout
     * @param vx820Type
     * @return 0 表示异步任务投递成功
     * -1 表示 json 参数为空
     * -2 表示接口库还未成功初始化
     * -3 表示 json 参数格式不正确
     * -4 表示 json 参数中命令参数为空
     * -5 表示 json 参数中，命令为”CC”时，交易卡类型不正确
     * -6 表示 json 参数中，整数价格值不正确
     * -7 表示串口任务正在执行中，请稍后再发起请求
     */
    public int payOrder(double amount, int timeout, VX820Type vx820Type) {
        cardIndicator = vx820Type.getIndicator();
        payTypeName = vx820Type.getPayTypeName();
        Map<String, String> map = new HashMap<>();
        map.put("cmd", "CC");
        map.put("xxx_indicator", vx820Type.getIndicator());
        map.put("price", amount + "");
        JSONObject jsonObject = new JSONObject(map);
        String json = jsonObject.toString();
        int action = getInstance().ecrAction(json);
        logOrder(String.format("下单支付指令:%s", json));
        return action;
    }

    ///结账
    public int checkout() {
        Map<String, String> map = new HashMap<>();
        map.put("cmd", "CK");
        JSONObject jsonObject = new JSONObject(map);
        String json = jsonObject.toString();
        int action = getInstance().ecrAction(json);
        printLog(String.format("结账指令:%s", json));
        return action;
    }

    public static void ecrCbJsonS(String cmd, byte[] buf) {
        getInstance().ecrCbJsonS2(cmd, buf);
    }

    public void ecrCbJsonS2(String cmd, byte[] buf) {
        String jsonStr = null;
        try {
            jsonStr = new String(buf, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logOrder(e.getMessage());
        }
        String print = String.format("cmd:%s,buf:%s", cmd, jsonStr);
        printLog(String.format("多元卡回调;内容:%s", print));
        if (TextUtils.isEmpty(cmd) || TextUtils.isEmpty(jsonStr)) {
            return;
        }
        ErcBean ercBean = ErcBean.parseJson(jsonStr);
        if (ercBean == null) {
            return;
        }

        switch (cmd) {
            case "sdk_version"://cmd:sdk_version buf: {"cmd":"sdk_version","compile_time":"2021-06-16 17:27"}
                sdkVersion = ercBean.getCompile_time();
                printLog(String.format("多元卡支付so库版本号:%s", ercBean.getCompile_time()));
                break;
            case "CC":
                if (ercBean.isOk()) {
                    switch (ercBean.getDetail()) {
                        case "start":
                            printLog("交易开始");
                            if (vx820PayListener != null) {
                                getMainHandler().post(() -> {
                                    vx820PayListener.onStart(cardIndicator, payTypeName);
                                });
                            }
                            break;
                        case "ack":
                            printLog("交易确认");
                            if (vx820PayListener != null) {
                                getMainHandler().post(() -> {
                                    vx820PayListener.onProgress(cardIndicator, payTypeName);
                                });
                            }
                            break;
                        case "Approval":
                            if (!TextUtils.isEmpty(ercBean.getCard_indicator())) {//设置当前交易类型
                                cardIndicator = ercBean.getCard_indicator();
                            }
                            printLog("交易成功,cardIndicator:" + cardIndicator);
                            if (vx820PayListener != null) {
                                getMainHandler().post(() -> {
                                    vx820PayListener.onSuccess(cardIndicator, payTypeName);
                                });
                            }
                            break;
                        case "end":
                            printLog("交易结束");
                            break;
                    }
                } else {
                    printLog("交易失败");
                    if (vx820PayListener != null) {
                        getMainHandler().post(() -> {
                            vx820PayListener.onFail(cardIndicator, payTypeName, ercBean.getDetail() == null ? "" : ercBean.getDetail());
                        });
                    }
                }
                break;
            case "RF"://退款
                if (ercBean.isOk()) {
                    switch (ercBean.getDetail()) {
                        case "start":
                            printLog("退款开始");
                            break;
                        case "ack":
                            printLog("退款确认");
                            break;
                        case "Approval":
                            printLog(String.format("退款成功,金额:%s", ercBean.getTrans_amount() + ""));
                            break;
                        case "end":
                            printLog("退款结束");
                            break;
                    }
                } else {
                    printLog("退款失败");
                }
                break;
            case "CK":
                if (ercBean.isOk()) {
                    switch (ercBean.getDetail()) {
                        case "start":
                            printLog("结账开始");
                            break;
                        case "ack":
                            printLog("结账确认");
                            break;
                        case "Approval":
                            printLog("结账成功");
                            getInstance().updateTopCheckoutTime();
                            if (vx820InfoListener != null) {
                                getMainHandler().post(() -> {
                                    vx820InfoListener.callBack();
                                });
                            }
                            break;
                        case "end":
                            printLog("结账结束");
                            break;
                    }
                } else {
                    printLog("结账失败");
                }
                break;
        }

    }

    ///订单支付监听
    private void payOrderListener(final boolean start, final boolean progress, final boolean success, final String errMsg) {
//        PayHelper.onVX820State(payTypeName, start, progress, success, errMsg);
    }

    private void printLog(String msg) {
        Log.i(TAG, msg);
    }

    private void logOrder(String msg) {
//        LogUtils.logOrder(TAG, msg);
        if (context == null) {
            return;
        }
        Intent intent = new Intent(VxConstant.JETINNO_COFFEE_LOG_ACTION);
        intent.putExtra(VxConstant.JETINNO_COFFEE_LOG_MESSAGE, msg);
        context.sendBroadcast(intent);
    }
}
