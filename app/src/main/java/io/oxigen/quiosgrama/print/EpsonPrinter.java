package io.oxigen.quiosgrama.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.epson.epos2.printer.Printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.dao.ProductRequestDao;
import io.oxigen.quiosgrama.dao.RequestDao;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.util.AndroidUtil;
import io.oxigen.quiosgrama.util.ImageUtil;

/**
 * Created by Alexandre on 02/08/2016.
 *
 */
public class EpsonPrinter {

    private static final String TAG = "PrintService";

    private Context mContext;
    private Bundle mBundle;
    public boolean tryAgain;
    private QuiosgramaApp app;
    public HashSet<ProductRequest> productRequestList;
    private EpsonPrinterHelp epsonPrinterHelp;

    public EpsonPrinter(Context context, String printerIp){
        mContext = context;
        tryAgain = false;

        app = (QuiosgramaApp) context.getApplicationContext();
        productRequestList = new HashSet<>();
        epsonPrinterHelp = new EpsonPrinterHelp(context, printerIp);
    }

    public EpsonPrinter(Context context, String printerIp, Bundle bundle){
        mContext = context;
        mBundle = bundle;
        tryAgain = false;

        app = (QuiosgramaApp) context.getApplicationContext();
        epsonPrinterHelp = new EpsonPrinterHelp(context, printerIp);
    }

    public boolean printProductRequest(HashMap<Request, HashSet<ProductRequest>> prodReqMap){
        Printer printer = epsonPrinterHelp.initializeObject();
        if (printer != null) {
            if (!createReceiptData(printer, prodReqMap)) {
                epsonPrinterHelp.finalizeObject(printer);
                return false;
            }

            if (!epsonPrinterHelp.printData(printer, true)) {
                epsonPrinterHelp.finalizeObject(printer);
                tryAgain = true;
                return false;
            }

            return true;
        }
        
        return false;
    }

    public boolean printBill(){
        Log.d(TAG, "Iniciado");
        Bill bill = mBundle.getParcelable(KeysContract.BILL_KEY);
        if(bill != null) {
            Printer printer = epsonPrinterHelp.initializeObject();
            if (printer != null) {
                if (!createBillReceiptData(printer, bill)) {
                    epsonPrinterHelp.finalizeObject(printer);
                    return false;
                }

                if (!epsonPrinterHelp.printData(printer, true)) {
                    epsonPrinterHelp.finalizeObject(printer);
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    public boolean printReport(){
        Log.d(TAG, "Iniciado");
        String report = mBundle.getString(KeysContract.REPORT_KEY);
        if(report != null && !report.isEmpty()){
            Printer printer = epsonPrinterHelp.initializeObject();
            if (printer != null) {
                if (!createReportReceiptData(printer, report)) {
                    epsonPrinterHelp.finalizeObject(printer);
                    return false;
                }

                if (!epsonPrinterHelp.printData(printer, true)) {
                    epsonPrinterHelp.finalizeObject(printer);
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    private boolean createReceiptData(Printer printer, HashMap<Request, HashSet<ProductRequest>> prodReqMap) {
        Log.d(TAG, "Imprimindo...");

        String method = "";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
            for (Map.Entry<Request, HashSet<ProductRequest>> entryRequest : prodReqMap.entrySet()) {
                StringBuilder textData = new StringBuilder();
                Request request = RequestDao.get(mContext, entryRequest.getKey().id);

                if (request.waiter.name == null || request.waiter.name.isEmpty()) {
                    request.waiter = selectFunctionary(request.waiter.id);
                }

                if (request.waiter != null) {
                    textData.append(request.waiter.name);
                    textData.append(" - ");
                    textData.append(dateFormat.format(request.requestTime));
                    textData.append("\n");
                } else {
                    Log.e(TAG, "Error to obtain the functionary");
                }

                textData.append(request.bill.toString());
                textData.append("\n\n");

                textData.append(mContext.getResources().getString(R.string.cod).toUpperCase());
                textData.append("\t");
                textData.append(mContext.getResources().getString(R.string.product).toUpperCase());
                textData.append("\n");

                Product product = null;
                for (ProductRequest prodReq : entryRequest.getValue()) {
                    product = QuiosgramaApp.searchProduct(prodReq.product.code);

                    if (product != null) {
                        textData.append(product.code);
                        textData.append("\t");
                        if (prodReq.quantity > 0) {
                            textData.append(prodReq.quantity);
                        } else {
                            textData.append(product.quantity);
                        }
                        textData.append(" x ");
                        textData.append(product.name);

                        if (prodReq.complement != null
                                && prodReq.complement.description != null
                                && !prodReq.complement.description.trim().isEmpty()) {
                            textData.append("\n\t");
                            textData.append("  ");
                            textData.append(prodReq.complement.description);
                        }

                        textData.append("\n");
                    } else {
                        textData.append("null");
                    }

                    productRequestList.add(prodReq);
                }

                method = "addTextAlign";
                printer.addTextAlign(Printer.ALIGN_CENTER);

                if(product != null) {
                    method = "addImage";
                    Bitmap logoData = ImageUtil.changeColorIconBitmap(mContext, AndroidUtil.buildImagesValue(product.type.destinationIcon), android.R.color.black);
                    printer.addImage(logoData, 0, 0,
                            logoData.getWidth(),
                            logoData.getHeight(),
                            Printer.COLOR_1,
                            Printer.MODE_MONO,
                            Printer.HALFTONE_DITHER,
                            Printer.PARAM_DEFAULT,
                            Printer.COMPRESS_AUTO);
                }

                method = "addFeedLine";
                printer.addFeedLine(1);

                String appName = mContext.getResources().getString(R.string.app_name);
                printer.addText(appName + " - " + QuiosgramaApp.getKioskName());
                printer.addFeedLine(2);

                method = "addTextAlign";
                printer.addTextAlign(Printer.ALIGN_LEFT);

                method = "addText";
                printer.addText(textData.toString());
                method = "addFeedLine";
                printer.addFeedLine(2);
                method = "addCut";
                printer.addCut(Printer.CUT_FEED);
            }
        } catch (Exception e) {
            EpsonPrinterHelp.ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    private boolean createBillReceiptData(Printer printer, Bill bill) {
        Log.d(TAG, "Imprimindo...");

        String method = "";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            StringBuilder textData = new StringBuilder();

            HashSet<ProductRequest> prodReqList = ProductRequestDao.getByBill(mContext, bill);
            if(prodReqList != null && !prodReqList.isEmpty()){
                textData.append(mContext.getResources().getString(R.string.product).toUpperCase());
                textData.append("\t\t\t\t");
                textData.append(mContext.getResources().getString(R.string.unit).toUpperCase());
                textData.append("\t");
                textData.append(mContext.getResources().getString(R.string.total).toUpperCase());
                textData.append("\n");

                double total = 0;
                for(ProductRequest prodReq : prodReqList){
                    String textProdReq = "";
                    Product product = QuiosgramaApp.searchProduct(prodReq.product.code);

                    if (prodReq.quantity > 0) {
                        textProdReq += prodReq.quantity;
                    } else {
                        textProdReq += product.quantity;
                        prodReq.quantity = product.quantity;
                    }
                    textProdReq += " x ";
                    textProdReq += product.name;

                    if(textProdReq.length() > EpsonPrinterHelp.LINE_LENGTH) {
                        textData.append(textProdReq.substring(0, EpsonPrinterHelp.LINE_LENGTH));
                    }
                    else{
                        for(int i = textProdReq.length(); i < EpsonPrinterHelp.LINE_LENGTH; i++){
                            textProdReq += " ";
                        }
                        textData.append(textProdReq);
                    }

                    textData.append(String.format(Locale.US, "%.2f", product.price));
                    textData.append("\t");
                    textData.append(String.format(Locale.US, "%.2f", prodReq.quantity * product.price));
                    textData.append("\n");

                    if (prodReq.complement != null
                            && prodReq.complement.description != null
                            && !prodReq.complement.description.trim().isEmpty()
                            && prodReq.complement.price > 0){

                        String textComplement = "";
                        textComplement += prodReq.quantity;
                        textComplement += " x ";
                        textComplement += prodReq.complement.description;

                        if(textComplement.length() > EpsonPrinterHelp.LINE_LENGTH) {
                            textData.append(textComplement.substring(0, EpsonPrinterHelp.LINE_LENGTH));
                        }
                        else{
                            for(int i = textComplement.length(); i < EpsonPrinterHelp.LINE_LENGTH; i++){
                                textComplement += " ";
                            }
                            textData.append(textComplement);
                        }

                        textData.append(String.format(Locale.US, "%.2f", prodReq.complement.price));
                        textData.append("\t");
                        textData.append(String.format(Locale.US, "%.2f", prodReq.quantity * prodReq.complement.price));
                        textData.append("\n");

                        total += prodReq.quantity * product.price + prodReq.complement.price;
                    }
                    else {
                        total += prodReq.quantity * product.price;
                    }
                }
                textData.append("\n");
                textData.append(mContext.getResources().getString(R.string.sub_total));
                textData.append("\t\t\t\t");
                textData.append(String.format(Locale.US, "%.2f", total));
                textData.append("\n");
                textData.append(mContext.getResources().getString(R.string.service_total));
                textData.append("\t\t\t\t\t");
                textData.append(String.format(Locale.US, "%.2f", total * 0.1));
                textData.append("\n");
                textData.append(mContext.getResources().getString(R.string.total));
                textData.append("\t\t\t\t\t");
                textData.append(String.format(Locale.US, "%.2f", total * 1.1));
                textData.append("\n");

                method = "addTextAlign";
                printer.addTextAlign(Printer.ALIGN_CENTER);
                String appName = mContext.getResources().getString(R.string.app_name);
                method = "addText";
                printer.addText(appName + " - " + QuiosgramaApp.getKioskName() + "\n");
                method = "addText";
                printer.addText(dateFormat.format(bill.openTime) + " - " + AndroidUtil.calculateDate(mContext, bill.openTime, new Date()));
                method = "addFeedLine";
                printer.addFeedLine(2);
                method = "addText";
                printer.addText(bill.toString());
                method = "addFeedLine";
                printer.addFeedLine(2);

                method = "addTextAlign";
                printer.addTextAlign(Printer.ALIGN_LEFT);

                method = "addText";
                printer.addText(textData.toString());

                method = "addFeedLine";
                printer.addFeedLine(1);

                method = "addTextAlign";
                printer.addTextAlign(Printer.ALIGN_CENTER);
                method = "addText";
                printer.addText(mContext.getResources().getString(R.string.bill_receipt_message));
                method = "addFeedLine";
                printer.addFeedLine(2);
                method = "addCut";
                printer.addCut(Printer.CUT_FEED);
            }
        } catch (Exception e) {
            EpsonPrinterHelp.ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    private boolean createReportReceiptData(Printer printer, String report) {
        Log.d(TAG, "Imprimindo...");

        String method = "";
        try {
            method = "addTextAlign";
            printer.addTextAlign(Printer.ALIGN_CENTER);
            String appName = mContext.getResources().getString(R.string.app_name);
            method = "addText";
            printer.addText(appName + " - " + QuiosgramaApp.getKioskName());
            method = "addFeedLine";
            printer.addFeedLine(2);
            method = "addText";
            printer.addText(mContext.getResources().getString(R.string.report));
            method = "addFeedLine";
            printer.addFeedLine(2);

            method = "addTextAlign";
            printer.addTextAlign(Printer.ALIGN_LEFT);

            method = "addText";
            printer.addText(report);

            method = "addFeedLine";
            printer.addFeedLine(2);
            method = "addCut";
            printer.addCut(Printer.CUT_FEED);
        } catch (Exception e) {
            EpsonPrinterHelp.ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    private Functionary selectFunctionary(long id) {
        ArrayList<Functionary> functionaryList = new ArrayList<>(app.getFunctionaryList());
        for(Functionary waiter : functionaryList){
            if(id == waiter.id){
                return waiter;
            }
        }
        return null;
    }
}
