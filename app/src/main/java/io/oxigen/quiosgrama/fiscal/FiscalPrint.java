package io.oxigen.quiosgrama.fiscal;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import io.oxigen.quiosgrama.Kiosk;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.print.EpsonPrinterHelp;
import io.oxigen.quiosgrama.util.QrCodeUtil;

/**
 *
 * Created by Alexandre on 26/08/2016.
 */
public class FiscalPrint {

    private static final String TAG = "FiscalPrint";

    private static final int LINE_LENGTH = 47;
    private static final int MAX_TEXT_LENGTH = 41;

    public static int printCoupon(Context context, String cpf, HashSet<ProductRequest> prodReqList, boolean servicePaid, int paidMethod, double total, String[] satFields, String satSerialNumber, double discount) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String printerIp = preferences.getString(context.getResources().getString(R.string.preference_printer_ip_key),
                context.getResources().getString(R.string.preference_printer_ip_default));
        EpsonPrinterHelp epsonPrinterHelp = new EpsonPrinterHelp(context, printerIp);

        Printer printer = epsonPrinterHelp.initializeObject();

        if(createDanfe(context, cpf, printer, prodReqList, servicePaid, paidMethod, total, satFields, satSerialNumber, discount)
                && epsonPrinterHelp.printData(printer, true)) {
            return Fiscal.SAT_CFE_SENT;
        } else {
            epsonPrinterHelp.finalizeObject(printer);
            return Fiscal.SAT_PRINT_ERROR;
        }
    }

    private static boolean createDanfe(Context context, String cpf, Printer printer, HashSet<ProductRequest> prodReqList, boolean servicePaid, int paidMethod, double totalPaid, String[] satFields, String satSerialNumber, double discount) {
        Kiosk kiosk = QuiosgramaApp.getKiosk();
        Log.d(TAG, "Imprimindo...");

        String method = "";
        try {
            method = "addTextAlign";
            printer.addTextAlign(Printer.ALIGN_CENTER);

            method = "addText";
            printer.addText(kiosk.name);
            printer.addFeedLine(1);
            printer.addText(kiosk.companyName);
            printer.addFeedLine(1);
            printer.addText(kiosk.address);

            method = "addFeedLine";
            printer.addFeedLine(2);

            String companyInformation = "CNPJ " + kiosk.cnpj + " IE " + kiosk.ie + " IM " + kiosk.im;
            String divisor = "";
            for (int i = 0; i < LINE_LENGTH; i++){
                divisor += "-";
            }
            method = "addText";
            printer.addText(companyInformation);
            printer.addFeedLine(1);
            printer.addText(divisor);
            printer.addFeedLine(1);

            printer.addTextStyle(Printer.FALSE, Printer.FALSE, Printer.TRUE, Printer.COLOR_1);
            printer.addText("Extrato No. " + satFields[0]);
            printer.addFeedLine(1);
            printer.addText("CUPOM FISCAL ELETRÔNICO - SAT");
            printer.addFeedLine(1);
            printer.addText(divisor);
            printer.addFeedLine(1);

            printer.addTextStyle(Printer.FALSE, Printer.FALSE, Printer.FALSE, Printer.COLOR_1);
            method = "addTextAlign";
            printer.addTextAlign(Printer.ALIGN_LEFT);

            if(cpf != null && !cpf.isEmpty()){
                printer.addText("CPF/CNPJ do Consumidor: " + cpf);
                printer.addFeedLine(1);
                printer.addText(divisor);
                printer.addFeedLine(1);
            }

            printer.addText("# | COD | DESC | QTD | UN | VL UN R$ | (VL TR R$)* | VL ITEM  R$");
            printer.addFeedLine(1);
            printer.addText(divisor);
            printer.addFeedLine(1);

            method = "addText";
            TotalContainer totalContainer = createProductPrint(printer, prodReqList, servicePaid, discount);
            createReceivePrint(printer, totalContainer, totalPaid, paidMethod);

            method = "addText";
            printer.addText(divisor);
            printer.addFeedLine(1);
            printer.addText("OBSERVAÇÕES DO CONTRIBUINTE");

            method = "addFeedLine";
            printer.addFeedLine(2);

            method = "addText";
            printer.addText("*Valor aproximado dos tributos do item");
            printer.addFeedLine(1);
            printer.addText("Valor aproximado dos tributos deste cupom");
            printer.addFeedLine(1);
            printer.addText(formatTextSpace("(conforme Lei Fed. 12.741/2012) R$", String.format("%.2f", totalContainer.totalItem12741)));
            printer.addFeedLine(1);
            printer.addText(divisor);

            method = "addFeedLine";
            printer.addFeedLine(2);

            method = "addTextAlign";
            printer.addTextAlign(Printer.ALIGN_CENTER);

            method = "addText";
            printer.addText("SAT No. ");
            printer.addTextStyle(Printer.FALSE, Printer.FALSE, Printer.TRUE, Printer.COLOR_1);
            printer.addText(satSerialNumber);
            printer.addTextStyle(Printer.FALSE, Printer.FALSE, Printer.FALSE, Printer.COLOR_1);
            printer.addFeedLine(1);

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.US);
            String date;
            try {
                date = format2.format(format.parse(satFields[7]));
                printer.addText(date);
            } catch (ParseException e) {
                printer.addText(format2.format(new Date()));
            }
            printer.addFeedLine(2);

            createSatConsultKey(printer);
            createQrCode(context, printer, satFields[11]);

            method = "addCut";
            printer.addCut(Printer.CUT_FEED);

        } catch (Epos2Exception e) {
            EpsonPrinterHelp.ShowMsg.showException(e, method, context);
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (WriterException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void createReceivePrint(Printer printer, TotalContainer totalContainer, double totalPaid, int paidMethod) throws Epos2Exception {
        String totalPaidText = String.format("%.2f", totalPaid);
        printer.addText(formatTextSpace(getPaidMethodText(paidMethod), totalPaidText));

        double difference =  totalPaid - totalContainer.total;
        if(difference > 0) {
            printer.addFeedLine(1);
            printer.addText(formatTextSpace("Troco R$", String.format("%.2f", difference)));
        }

        printer.addFeedLine(2);
    }

    private static TotalContainer createProductPrint(Printer printer, HashSet<ProductRequest> prodReqList, boolean servicePaid, double discount) throws Epos2Exception, JSONException {
        TotalContainer totalContainer = new TotalContainer();
        totalContainer.discountBySubTotal -= discount;
        int i = 0;
        double discountByItem = discount / prodReqList.size();
        for(ProductRequest prodReq : prodReqList){
            Product product = prodReq.product;

            String itemNumber = formatItemPrintNumber(++i);
            String codeString = formatProductCodePrint(product.code);

            JSONObject taxJson = new JSONObject(product.tax);
            double vItem12741 = Double.valueOf(taxJson.getString("vItem12741"));

            double itemTotal = prodReq.quantity * product.price;
            String itemText = String.format("%s %s %s %d %s X %.2f (%.2f)", itemNumber, codeString, product.name, prodReq.quantity, "un", product.price, vItem12741);
            String itemValueText = String.format("%.2f", itemTotal);

            printer.addText(formatTextSpace(itemText, itemValueText));
            printer.addFeedLine(1);

            if(prodReq.complement != null && prodReq.complement.price > 0){
                double complementItemTotal = prodReq.quantity * prodReq.complement.price;
                String complementTaxLabel = "Acréscimo sobre item";
                String complementValue = String.format("%.2f", complementItemTotal);

                printer.addText(formatTextSpace(complementTaxLabel, complementValue));
                printer.addFeedLine(1);
                totalContainer.increaseTotalByItem += complementItemTotal;
            }

            if(servicePaid){
                double serviceTax = (prodReq.quantity * product.price) * 0.1;
                if(prodReq.complement != null){
                    serviceTax = (prodReq.quantity * product.price + prodReq.quantity * prodReq.complement.price) * 0.1;
                }

                String serviceTaxText = "Rateio de acréscimo sobre subtotal";
                String serviceTaxValueText = String.format("%.2f", serviceTax);

                printer.addText(formatTextSpace(serviceTaxText, serviceTaxValueText));
                printer.addFeedLine(1);
                totalContainer.increaseTotalBySubTotal += serviceTax;
            }

            if(discount > 0){
                String discountText = "Rateio de desconto sobre subtotal";
                String discountValueText = String.format("-%.2f", discountByItem);
                printer.addText(formatTextSpace(discountText, discountValueText));
                printer.addFeedLine(1);
            }

            totalContainer.totalItems += itemTotal;
            totalContainer.totalItem12741 += vItem12741;
        }

        printer.addFeedLine(1);

        createTotalPrint(printer, totalContainer);
        return totalContainer;
    }

    private static void createTotalPrint(Printer printer, TotalContainer totalContainer) throws Epos2Exception {
        String totalItemsLabel = "Total bruto de itens";
        String totalIncreaseByItemsLabel = "Total de acréscimo sobre item";
        String totalIncreaseBySubTotalLabel = "Acréscimo sobre subtotal";
        String totalDiscountBySubTotalLabel = "Desconto sobre subtotal";
        String totalLabel = "TOTAL R$";

        totalContainer.total = totalContainer.totalItems;
        if (totalContainer.increaseTotalByItem > 0
                || totalContainer.increaseTotalBySubTotal > 0 || totalContainer.discountBySubTotal < 0) {
            String totalItemsValue = String.format("%.2f", totalContainer.totalItems);
            printer.addText(formatTextSpace(totalItemsLabel, totalItemsValue));
            printer.addFeedLine(1);
        }

        if(totalContainer.increaseTotalByItem > 0){
            String totalIncreaseByItemsValue = String.format("%.2f", totalContainer.increaseTotalByItem);
            printer.addText(formatTextSpace(totalIncreaseByItemsLabel, totalIncreaseByItemsValue));
            printer.addFeedLine(1);
            totalContainer.total += totalContainer.increaseTotalByItem;
        }

        if(totalContainer.increaseTotalBySubTotal > 0){
            String totalIncreaseBySubTotalValue = String.format("%.2f", totalContainer.increaseTotalBySubTotal);
            printer.addText(formatTextSpace(totalIncreaseBySubTotalLabel, totalIncreaseBySubTotalValue));
            printer.addFeedLine(1);
            totalContainer.total += totalContainer.increaseTotalBySubTotal;
        }

        if(totalContainer.discountBySubTotal < 0){
            String discountValue = String.format("%.2f", totalContainer.discountBySubTotal);
            printer.addText(formatTextSpace(totalDiscountBySubTotalLabel, discountValue));
            printer.addFeedLine(1);
            totalContainer.total += totalContainer.discountBySubTotal;
        }

        printer.addTextStyle(Printer.FALSE, Printer.FALSE, Printer.TRUE, Printer.COLOR_1);
        String totalValue = String.format("%.2f", totalContainer.total);
        printer.addText(formatTextSpace(totalLabel, totalValue));
        printer.addTextStyle(Printer.FALSE, Printer.FALSE, Printer.FALSE, Printer.COLOR_1);
        printer.addFeedLine(2);
    }

    private static String formatProductCodePrint(long code) {
        if(code <= 9){
            return "0000" + code;
        }
        else if(code < 99){
            return "000" + code;

        }
        else if(code < 999){
            return "00" + code;

        }
        else if(code < 9999){
            return "0" + code;

        }
        else{
            return "" + code;
        }
    }

    private static String formatItemPrintNumber(int i) {
        if(i < 10){
            return "00" + i;
        }
        else if(i < 100){
            return "0" + i;
        }
        else{
            return String.valueOf(i);
        }
    }

    private static String getPaidMethodText(int paidMethod) {
        switch (paidMethod){
            case Fiscal.SAT_METHOD_PAID_DINHEIRO:
                return "Dinheiro";
            case Fiscal.SAT_METHOD_PAID_DEBITO:
            case Fiscal.SAT_METHOD_PAID_CREDITO:
                return "Cartão de débito";
        }
        return null;
    }

    private static String formatTextSpace(String str1, String str2){
        int qtdSpaces = LINE_LENGTH - str1.length() - str2.length();

        if(str1.length() > LINE_LENGTH ||
                str1.length() > MAX_TEXT_LENGTH){
            String tmp = str1.substring(0, MAX_TEXT_LENGTH) + "\n";
            String tmp2 = str1.substring(MAX_TEXT_LENGTH, str1.length());

            qtdSpaces = LINE_LENGTH - tmp2.length() - str2.length();

            for(int j = 0; j < qtdSpaces; j++){
                tmp2 += " ";
            }

            str1 = tmp + tmp2;
        }
        else{
            for(int j = 0; j < qtdSpaces; j++){
                str1 += " ";
            }
        }

        return str1 + str2;
    }

    private static void createQrCode(Context context, Printer printer, String qrcodeValue) throws Epos2Exception, WriterException {
        Bitmap imageData = QrCodeUtil.encodeAsBitmap(context, qrcodeValue, 400, 400);
        printer.addImage(imageData, 0, 0,
                imageData.getWidth(),
                imageData.getHeight(),
                Printer.COLOR_1,
                Printer.MODE_MONO,
                Printer.HALFTONE_DITHER,
                Printer.PARAM_DEFAULT,
                Printer.COMPRESS_AUTO);

        printer.addTextAlign(Printer.ALIGN_LEFT);
        printer.addFeedLine(2);
    }

    private static void createSatConsultKey(Printer printer) throws Epos2Exception {
        String satConsultKeyTmp = Fiscal.ultimaChaveVenda.substring(3, Fiscal.ultimaChaveVenda.length());
        String satConsultKey = "";
        int j;
        int k = 0;
        for(int i = 1; i <= 11; i++){
            j = i * 4;
            satConsultKey += satConsultKeyTmp.substring(k, j) + " ";
            k = j;
            if(i == 6){
                satConsultKey += "\n";
            }
        }

        satConsultKey = satConsultKey.substring(0, satConsultKey.length());
        printer.addText(satConsultKey);
        printer.addFeedLine(2);

        String barcode1 = satConsultKeyTmp.substring(0, 22);
        String barcode2 = satConsultKeyTmp.substring(22, satConsultKeyTmp.length());
        printer.addBarcode("{C"+barcode1,
                Printer.BARCODE_CODE128,
                Printer.HRI_NONE,
                Printer.FONT_A,
                2,
                100);
        printer.addFeedLine(1);
        printer.addBarcode("{C"+barcode2,
                Printer.BARCODE_CODE128,
                Printer.HRI_NONE,
                Printer.FONT_A,
                2,
                100);
        printer.addFeedLine(1);
    }
}
