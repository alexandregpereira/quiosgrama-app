package io.oxigen.quiosgrama.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.oxigen.quiosgrama.Destination;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.Push;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.Table;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.print.EpsonPrinter;
import io.oxigen.quiosgrama.print.EpsonPrinterThread;
import io.oxigen.quiosgrama.util.AndroidUtil;

/**
 *
 * Created by Alexandre on 23/06/2016.
 */
public class PrintService extends IntentService{

    public static final int PRINT_REQUEST = 10;
    public static final int PRINT_BILL = 11;
    public static final int PRINT_REPORT = 12;

    private static final String TAG = "PrintService";

    private String mPrinterIp;
    QuiosgramaApp app;
    private HashSet<ProductRequest> mProdReqList;
    private Bundle mBundle;

    public PrintService(){
        super(PrintService.class.getName());
    }

    public PrintService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        app = (QuiosgramaApp) getApplicationContext();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrinterIp = preferences.getString(getResources().getString(R.string.preference_printer_ip_key),
                getResources().getString(R.string.preference_printer_ip_default));

        mBundle = intent.getExtras();
        int method = mBundle.getInt(KeysContract.METHOD_KEY);

        EpsonPrinter epsonPrinter = new EpsonPrinter(this, mPrinterIp, mBundle);
        switch (method){
            case PRINT_REQUEST:
                printRequest();
                break;

            case PRINT_BILL:
                epsonPrinter.printBill();
                break;

            case PRINT_REPORT:
                epsonPrinter.printReport();
                break;
        }

        Log.d(TAG, "Finalizado");
    }

    private void printRequest() {
        if(!runPrintReceiptSequence()){
            Log.d(TAG, "Não imprimido");
        }
    }

    private boolean runPrintReceiptSequence() {
        Log.d(TAG, "Iniciado");
        mProdReqList = getProductRequestByStatusSent();
        if(mProdReqList != null && !mProdReqList.isEmpty()) {
            HashMap<Destination, HashMap<Request, HashSet<ProductRequest>>> prodReqDestinationMap = divideByRequestAndDestination(mProdReqList);

            ExecutorService executorService = Executors.newCachedThreadPool();
            ArrayList<String> printerIpRepeatedList = new ArrayList<>();
            ArrayList<EpsonPrinterThread> threadList = new ArrayList<>();
            int delay = 0;
            for(Map.Entry<Destination, HashMap<Request, HashSet<ProductRequest>>> entryDestination: prodReqDestinationMap.entrySet()) {
                String printerIp = entryDestination.getKey().printerIp;
                if(printerIpRepeatedList.contains(printerIp)){
                    delay += 2000;
                }
                printerIpRepeatedList.add(printerIp);

                EpsonPrinterThread epsonPrinterThread = new EpsonPrinterThread(this, printerIp, entryDestination.getValue(), delay);
                threadList.add(epsonPrinterThread);
                executorService.execute(new Thread(epsonPrinterThread));
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES);
                Log.d(TAG, "Finalizado ExecutorService");
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }

            for(EpsonPrinterThread epsonPrinterThread : threadList){
                if(epsonPrinterThread.epsonPrinter.tryAgain){
                    return tryAgain();
                }
            }

            return true;
        }
        
        return false;
    }

    private boolean tryAgain() {
        try {
            Log.d(TAG, "Tentando novamente, 10 segundos");
            Thread.sleep(10000);
            DataBaseService.buildNotification(this);
            return runPrintReceiptSequence();
        } catch (InterruptedException e){}

        return false;
    }

    private HashSet<ProductRequest> getProductRequestByStatusSent() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Push<Table> push = new Push<>(AndroidUtil.getImei(this), null);
        String jsonString = gson.toJson(push);

        Container container = HttpService.getWithPost(this, Container.class,
                getResources().getString(R.string.get_product_request_print), jsonString, HttpService.CONTENT_TYPE_JSON);

        if(container != null){
            return container.productRequestList;
        }

        return null;
    }

    private HashMap<Destination, HashMap<Request, HashSet<ProductRequest>>> divideByRequestAndDestination(HashSet<ProductRequest> prodReqList) {
        HashMap<Destination, HashMap<Request, HashSet<ProductRequest>>> prodReqMap = new HashMap<>();
        for(ProductRequest prodReq : prodReqList){

            Product product = QuiosgramaApp.searchProduct(prodReq.product.code);
            if(product != null) {
                Destination destination = new Destination(product.type);
                if (!prodReqMap.containsKey(destination)) {
                    prodReqMap.put(destination, new HashMap<Request, HashSet<ProductRequest>>());
                }

                Request request = prodReq.request;
                if (!prodReqMap.get(destination).containsKey(request)) {
                    prodReqMap.get(destination).put(request, new HashSet<ProductRequest>());
                }

                prodReqMap.get(destination).get(request).add(prodReq);
            }
            else{
                prodReqMap.clear();
            }
        }

        return prodReqMap;
    }

    private class Container{
        public HashSet<ProductRequest> productRequestList;
        public String message;

        public String toString(){
            return message;
        }
    }
}
