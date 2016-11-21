package io.oxigen.quiosgrama.print;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.dao.ProductRequestDao;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fragment.TableRequestFragment;
import io.oxigen.quiosgrama.service.SyncServerService;

/**
 * Created by Alexandre on 02/08/2016.
 *
 */
public class EpsonPrinterThread extends Thread {

    private final String TAG = "PrintService";

    private final HashMap<Request, HashSet<ProductRequest>> mProdReqMap;
    private final Context mContext;
    private final int mDelay;
    public EpsonPrinter epsonPrinter;

    public EpsonPrinterThread(Context context, String printerIp, HashMap<Request, HashSet<ProductRequest>> prodReqMap, int delay) {
        mContext = context;
        epsonPrinter = new EpsonPrinter(context, printerIp);
        mProdReqMap = prodReqMap;
        mDelay = delay;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(mDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(epsonPrinter.printProductRequest(mProdReqMap)){
            sendSuccessfulPrintList();
        }
    }

    private void sendSuccessfulPrintList() {
        if(epsonPrinter.productRequestList != null && !epsonPrinter.productRequestList.isEmpty()){
            for(ProductRequest prodReq : epsonPrinter.productRequestList){
                prodReq.status = ProductRequest.VISUALIZED_STATUS;
                prodReq.productRequestTime = new Date();
                prodReq.syncStatus = KeysContract.NO_SYNCHRONIZED_STATUS_KEY;
            }

            ArrayList<ProductRequest> prodReqList = new ArrayList<>(epsonPrinter.productRequestList);
            ProductRequestDao.insertOrUpdate(mContext, prodReqList);
            mContext.sendBroadcast(new Intent(TableRequestFragment.RECEIVER_FILTER));

            SyncServerService.sendProductRequest(mContext, false);

            Log.d(TAG, "Imprimido");
        }
    }
}


