package io.oxigen.quiosgrama.print;

import android.content.Context;
import android.util.Log;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import io.oxigen.quiosgrama.R;

/**
 *
 * Created by Alexandre on 21/08/2016.
 */
public class EpsonPrinterHelp {

    private static final String TAG = "PrintService";
    private static final int TIMEOUT = 5000;
    public static final int LINE_LENGTH = 32;

    private final Context mContext;
    private final String mPrinterIp;

    public EpsonPrinterHelp(Context context, String printerIp){
        mContext = context;
        mPrinterIp = "TCP:" + printerIp;
    }

    public Printer initializeObject() {
        try {
            Printer printer = new Printer(Printer.TM_T20, Printer.MODEL_ANK, mContext);
            printer.setReceiveEventListener(receiveListener);

            return printer;
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return null;
        }
    }

    public void finalizeObject(Printer printer) {
        printer.clearCommandBuffer();
        printer.setReceiveEventListener(null);
    }

    public boolean connectPrinter(final Printer printer) {
        boolean isBeginTransaction = false;

        if (printer == null) {
            return false;
        }

        try {
            Log.d(TAG, "Conectando com a impressora " + mPrinterIp + "...");
            printer.connect(mPrinterIp, TIMEOUT);
            Log.d(TAG, "Conectado");
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        try {
            printer.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "beginTransaction", mContext);
        }

        if (!isBeginTransaction) {
            try {
                printer.disconnect();
            }
            catch (Epos2Exception e) {
                Log.d(TAG, "Erro ao desconectar");
                ShowMsg.showException(e, "disconnect", mContext);
                return false;
            }
        }

        return true;
    }

    public boolean printData(Printer printer, boolean connect) {
        if (connect && !connectPrinter(printer)) {
            return false;
        }

        PrinterStatusInfo status = printer.getStatus();

        if (!isPrintable(status)) {
            ShowMsg.showMsg(makeErrorMessage(status));
            try {
                printer.disconnect();
            }
            catch (Exception ex) {
                Log.d(TAG, "Erro ao desconectar");
                ShowMsg.showException(ex, "disconnect", mContext);
            }
            return false;
        }

        try {
            printer.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "sendData", mContext);
            try {
                printer.disconnect();
            }
            catch (Exception ex) {
                Log.d(TAG, "Erro ao desconectar");
                ShowMsg.showException(ex, "disconnect", mContext);
            }
            return false;
        }

        return true;
    }

    public boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }

        return true;
    }

    public void disconnectPrinter(Printer printer) {
        try {
            printer.endTransaction();
        }
        catch (Exception e) {
            ShowMsg.showException(e, "endTransaction", mContext);
        }

        try {
            printer.disconnect();
        }
        catch ( Exception e) {
            ShowMsg.showException(e, "disconnect", mContext);
        }

        finalizeObject(printer);
    }

    private ReceiveListener receiveListener = new ReceiveListener() {
        @Override
        public void onPtrReceive(final Printer printer, int code, PrinterStatusInfo status, String s) {
            ShowMsg.showResult(code, makeErrorMessage(status), mContext);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    disconnectPrinter(printer);
                }
            }).start();
        }
    };

    public String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_autocutter);
            msg += mContext.getResources().getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += mContext.getResources().getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getResources().getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += mContext.getResources().getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getResources().getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += mContext.getResources().getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getResources().getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += mContext.getResources().getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    public static class ShowMsg {
        public static void showException(Exception e, String method, Context mContext) {
            String msg;
            if (e instanceof Epos2Exception) {
                msg = String.format(
                        "%s\n\t%s\n%s\n\t%s",
                        mContext.getString(R.string.title_err_code),
                        getEposExceptionText(((Epos2Exception) e).getErrorStatus()),
                        mContext.getString(R.string.title_err_method),
                        method);
            }
            else {
                msg = e.toString();
            }
            show(msg);
        }

        public static void showResult(int code, String errMsg, Context mContext) {
            String msg;
            if (errMsg.isEmpty()) {
                msg = String.format(
                        "\t%s\n\t%s\n",
                        mContext.getString(R.string.title_msg_result),
                        getCodeText(code));
            }
            else {
                msg = String.format(
                        "\t%s\n\t%s\n\n\t%s\n\t%s\n",
                        mContext.getString(R.string.title_msg_result),
                        getCodeText(code),
                        mContext.getString(R.string.title_msg_description),
                        errMsg);
            }
            show(msg);
        }

        public static void showMsg(String msg) {
            show(msg);
        }

        private static void show(String msg) {
            Log.d(TAG, msg);
        }

        private static String getEposExceptionText(int state) {
            String returnText;
            switch (state) {
                case    Epos2Exception.ERR_PARAM:
                    returnText = "ERR_PARAM";
                    break;
                case    Epos2Exception.ERR_CONNECT:
                    returnText = "ERR_CONNECT";
                    break;
                case    Epos2Exception.ERR_TIMEOUT:
                    returnText = "ERR_TIMEOUT";
                    break;
                case    Epos2Exception.ERR_MEMORY:
                    returnText = "ERR_MEMORY";
                    break;
                case    Epos2Exception.ERR_ILLEGAL:
                    returnText = "ERR_ILLEGAL";
                    break;
                case    Epos2Exception.ERR_PROCESSING:
                    returnText = "ERR_PROCESSING";
                    break;
                case    Epos2Exception.ERR_NOT_FOUND:
                    returnText = "ERR_NOT_FOUND";
                    break;
                case    Epos2Exception.ERR_IN_USE:
                    returnText = "ERR_IN_USE";
                    break;
                case    Epos2Exception.ERR_TYPE_INVALID:
                    returnText = "ERR_TYPE_INVALID";
                    break;
                case    Epos2Exception.ERR_DISCONNECT:
                    returnText = "ERR_DISCONNECT";
                    break;
                case    Epos2Exception.ERR_ALREADY_OPENED:
                    returnText = "ERR_ALREADY_OPENED";
                    break;
                case    Epos2Exception.ERR_ALREADY_USED:
                    returnText = "ERR_ALREADY_USED";
                    break;
                case    Epos2Exception.ERR_BOX_COUNT_OVER:
                    returnText = "ERR_BOX_COUNT_OVER";
                    break;
                case    Epos2Exception.ERR_BOX_CLIENT_OVER:
                    returnText = "ERR_BOX_CLIENT_OVER";
                    break;
                case    Epos2Exception.ERR_UNSUPPORTED:
                    returnText = "ERR_UNSUPPORTED";
                    break;
                case    Epos2Exception.ERR_FAILURE:
                    returnText = "ERR_FAILURE";
                    break;
                default:
                    returnText = String.format("%d", state);
                    break;
            }
            return returnText;
        }

        private static String getCodeText(int state) {
            String returnText;
            switch (state) {
                case Epos2CallbackCode.CODE_SUCCESS:
                    returnText = "PRINT_SUCCESS";
                    break;
                case Epos2CallbackCode.CODE_PRINTING:
                    returnText = "PRINTING";
                    break;
                case Epos2CallbackCode.CODE_ERR_AUTORECOVER:
                    returnText = "ERR_AUTORECOVER";
                    break;
                case Epos2CallbackCode.CODE_ERR_COVER_OPEN:
                    returnText = "ERR_COVER_OPEN";
                    break;
                case Epos2CallbackCode.CODE_ERR_CUTTER:
                    returnText = "ERR_CUTTER";
                    break;
                case Epos2CallbackCode.CODE_ERR_MECHANICAL:
                    returnText = "ERR_MECHANICAL";
                    break;
                case Epos2CallbackCode.CODE_ERR_EMPTY:
                    returnText = "ERR_EMPTY";
                    break;
                case Epos2CallbackCode.CODE_ERR_UNRECOVERABLE:
                    returnText = "ERR_UNRECOVERABLE";
                    break;
                case Epos2CallbackCode.CODE_ERR_FAILURE:
                    returnText = "ERR_FAILURE";
                    break;
                case Epos2CallbackCode.CODE_ERR_NOT_FOUND:
                    returnText = "ERR_NOT_FOUND";
                    break;
                case Epos2CallbackCode.CODE_ERR_SYSTEM:
                    returnText = "ERR_SYSTEM";
                    break;
                case Epos2CallbackCode.CODE_ERR_PORT:
                    returnText = "ERR_PORT";
                    break;
                case Epos2CallbackCode.CODE_ERR_TIMEOUT:
                    returnText = "ERR_TIMEOUT";
                    break;
                case Epos2CallbackCode.CODE_ERR_JOB_NOT_FOUND:
                    returnText = "ERR_JOB_NOT_FOUND";
                    break;
                case Epos2CallbackCode.CODE_ERR_SPOOLER:
                    returnText = "ERR_SPOOLER";
                    break;
                case Epos2CallbackCode.CODE_ERR_BATTERY_LOW:
                    returnText = "ERR_BATTERY_LOW";
                    break;
                default:
                    returnText = String.format("%d", state);
                    break;
            }
            return returnText;
        }
    }
}
