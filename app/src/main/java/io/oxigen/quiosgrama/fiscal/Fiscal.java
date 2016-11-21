package io.oxigen.quiosgrama.fiscal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.preference.PreferenceManager;
import android.util.Xml;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import io.oxigen.quiosgrama.Amount;
import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Kiosk;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.dao.ProductRequestDao;
import io.oxigen.quiosgrama.sat.EasySAT;
import io.oxigen.quiosgrama.sat.SATCommunicationException;
import io.oxigen.quiosgrama.sat.SATNotConnectedException;
import io.oxigen.quiosgrama.service.SyncServerService;

/**
 *
 * Created by Alexandre on 16/08/2016.
 */
public class Fiscal{

    private static final String TAG = "Fiscal";
    public static final int SAT_CFE_SENT = 100;
    public static final int SAT_NOT_CONNECTED = 101;
    public static final int SAT_PRINT_ERROR = 102;
    public static final int SAT_CREATE_XML_ERROR = 103;
    public static final int SAT_GET_PRODUCTS_ERROR = 104;
    public static final int SAT_UNKNOWN_ERROR = 105;
    public static final int SAT_CFE_SENT_ERROR = 106;
    public static final int SAT_BUFFER_ERROR = 107;
    public static final int SAT_SERIAL_NUMBER_ERROR = 108;
    public static final int SAT_INVALID_DISCOUNT_ERROR = 109;
    public static final int SAT_TAX_NOT_FOUND_ERROR = 110;

    public final static int SAT_METHOD_PAID_DINHEIRO = 1;
    public final static int SAT_METHOD_PAID_CHEQUE = 2;
    public final static int SAT_METHOD_PAID_CREDITO = 3;
    public final static int SAT_METHOD_PAID_DEBITO = 4;
    public final static int SAT_METHOD_PAID_ALIMENTACAO = 10;
    public final static int SAT_METHOD_PAID_REFEICAO = 11;
    public final static int SAT_METHOD_PAID_OUTROS = 99;

    private static final String CODIGO_DE_ATIVACAO = "99999999";

    private static long ultimoNumeroSessao = 0;
    private static Random random = new Random();
    public static String ultimaChaveVenda;

    public static String LAST_PRODUCT;

    public static String easySatMessage;

    public static int execute(Context context, String cpf, Bill bill, ArrayList<Amount> amountList, double discount){
        QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String satSerialNumber = preferences.getString(context.getResources().getString(R.string.preference_sat_serial_number_key),
                "");

        if(!satSerialNumber.trim().isEmpty()) {
            if(app.easySat.getDevice() != null) {
                HashSet<ProductRequest> prodReqList = ProductRequestDao.getByBill(context, bill);
                if (prodReqList != null && !prodReqList.isEmpty()
                        && amountList != null && !amountList.isEmpty()) {

                    double totalPaid = 0;
                    int paidMethod = SAT_METHOD_PAID_DINHEIRO;
                    for(Amount amount : amountList){
                        totalPaid += amount.value;
                        if(amount.paidMethod == Amount.PAID_METHOD_CARD){
                            paidMethod = SAT_METHOD_PAID_DEBITO;
                        }
                    }

                    String xml;
                    try {
                        xml = createXmlFile(cpf, prodReqList, bill.servicePaid, paidMethod, totalPaid, discount);
                    } catch (JSONException e) {
                        return SAT_TAX_NOT_FOUND_ERROR;
                    }
                    if(xml != null) {
                        xml = handleSpecialCharacters(xml);
                        String result;
                        try {
                            result = app.easySat.enviarDadosVenda(genNumeroSessao(), CODIGO_DE_ATIVACAO, xml);
                        } catch (SATCommunicationException e) {
                            e.printStackTrace();
                            return SAT_NOT_CONNECTED;
                        } catch (SATNotConnectedException e) {
                            e.printStackTrace();
                            return SAT_NOT_CONNECTED;
                        } catch (BufferOverflowException e){
                            e.printStackTrace();
                            return SAT_BUFFER_ERROR;
                        }

                        if(result != null) {
                            String[] fields = result.split("\\|");
                            easySatMessage = fields[3];
                            if (fields.length >= 9) {
                                ultimaChaveVenda = fields[8];

                                createXmlSecureCopy(context, xml, bill.id);
                                return FiscalPrint.printCoupon(context, cpf, prodReqList, bill.servicePaid, paidMethod, totalPaid, fields, satSerialNumber, discount);
                            }
                            else{
                                easySatMessage += "\n\n" + xml;
                                return SAT_CFE_SENT_ERROR;
                            }
                        }
                        else{
                            return SAT_UNKNOWN_ERROR;
                        }
                    }
                    else{
                        return SAT_CREATE_XML_ERROR;
                    }
                }
                else{
                    return SAT_GET_PRODUCTS_ERROR;
                }
            }
            else{
                return SAT_NOT_CONNECTED;
            }
        }
        else{
            return SAT_SERIAL_NUMBER_ERROR;
        }
    }

    public static int testPrintCoupon(Context context, String cpf, Bill bill, ArrayList<Amount> amountList, double discount) {
        HashSet<ProductRequest> prodReqList = ProductRequestDao.getByBill(context, bill);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String satSerialNumber = preferences.getString(context.getResources().getString(R.string.preference_sat_serial_number_key),
                "");

        if(!satSerialNumber.trim().isEmpty()) {

            double totalPaid = 0;
            int paidMethod = SAT_METHOD_PAID_DINHEIRO;
            for (Amount amount : amountList) {
                totalPaid += amount.value;
                if (amount.paidMethod == Amount.PAID_METHOD_CARD) {
                    paidMethod = SAT_METHOD_PAID_DEBITO;
                }
            }

            String[] fields = new String[]{
                    "000344",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "20160826184103",
                    "",
                    "",
                    "",
                    "QqtRXMiY/F21lslO1GezZFBMaoQyYLKATgoN8u0F06aaKAQKq/dxhEbA3ISGLvFFwvxYe+B7TBKDxQQE2s7AtmI2nGe2O0VWUFJQIOZ8Yt+oOC029F5puyAOMVtUGlYQeIbqqt376EbSlfX0mDQ7Sw5hF/UCjwbcQ9jUxXYB6XLd0y/l1yudpFG6Q1prHPXuGwrrsLJSUI0ejXhHxJgMQj+fdT0pg8zX/9EBStod5Sn7Nt9GRhbIjjj3r/qQdfd3tD7speL9Y7vWlKgZUcsMQLX6twtIpsDB1Bhsk3gflFc5nfVtrTTBruWEuE3Jwjkgu7I5ceBsCIZ6zJgQWMZR9w=="
            };

            ultimaChaveVenda = "CFe35160805761098000113599000074510000562088780";

            FiscalPrint.printCoupon(context, cpf, prodReqList, bill.servicePaid, paidMethod, totalPaid, fields, satSerialNumber, discount);

            return SAT_GET_PRODUCTS_ERROR;
        }
        else{
            return SAT_SERIAL_NUMBER_ERROR;
        }
    }

    private static void createXmlSecureCopy(Context context, String xml, String billId) {
        File xmlFile = new File(context.getFilesDir(), billId + "cfe.xml");
        try {
            FileOutputStream xmlFileStream = new FileOutputStream(xmlFile);
            xmlFileStream.write(xml.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            SyncServerService.sendBroadcastMessageError(context,
                    context.getResources().getString(R.string.sat_secure_copy));
        } catch (IOException e) {
            e.printStackTrace();
            SyncServerService.sendBroadcastMessageError(context,
                    context.getResources().getString(R.string.sat_secure_copy));
        }
    }

    public static String handleSpecialCharacters(String xml) {
        String result = xml.replaceAll("ç", "c");
        result = result.replaceAll("Ç", "Ç");
        result = result.replaceAll("á", "a");
        result = result.replaceAll("Á", "A");
        result = result.replaceAll("é", "e");
        result = result.replaceAll("É", "E");
        result = result.replaceAll("í", "i");
        result = result.replaceAll("Í", "I");
        result = result.replaceAll("ó", "o");
        result = result.replaceAll("Ó", "O");
        result = result.replaceAll("ú", "u");
        result = result.replaceAll("Ú", "U");
        result = result.replaceAll("à", "a");
        result = result.replaceAll("À", "A");
        result = result.replaceAll("ã", "a");
        result = result.replaceAll("Ã", "A");
        result = result.replaceAll("â", "a");
        result = result.replaceAll("Â", "A");
        result = result.replaceAll("ê", "e");
        result = result.replaceAll("Ê", "E");
        result = result.replaceAll("ô", "o");
        result = result.replaceAll("Ô", "O");

        return result;
    }

    private static long genNumeroSessao() {
        ultimoNumeroSessao = random.nextInt(900000) + 100000; //entre 100000 e 999999
        return ultimoNumeroSessao;
    }

    private static String createXmlFile(String cpf, HashSet<ProductRequest> prodReqList, boolean servicePaid, int paidMethod, double totalReceived, double discount) throws JSONException {
        try {
            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();

            xmlSerializer.setOutput(writer);
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xmlSerializer.startDocument("UTF-8",true);

            xmlSerializer.startTag("", "CFe");
            xmlSerializer.startTag("", "infCFe");
            xmlSerializer.attribute(null, "versaoDadosEnt", "0.07");

            createAcXml(xmlSerializer);
            createEmitenteXml(xmlSerializer);
            createDestinatarioXml(xmlSerializer, cpf);

            int i = 0;
            double totalItemns = 0;
            for (ProductRequest prodReq : prodReqList) {
                xmlSerializer.startTag("", "det");
                xmlSerializer.attribute(null, "nItem", String.valueOf(++i));

                totalItemns += createProductXml(xmlSerializer, prodReq);

                xmlSerializer.endTag("", "det");
            }

            createTotalXml(xmlSerializer, servicePaid, totalItemns, discount);

            xmlSerializer.startTag("", "pgto");
            xmlSerializer.startTag("", "MP");

            xmlSerializer.startTag("", "cMP");
            if(paidMethod < 10){
                xmlSerializer.text("0" + String.valueOf(paidMethod));
            }
            else{
                xmlSerializer.text(String.valueOf(paidMethod));
            }
            xmlSerializer.endTag("", "cMP");

            xmlSerializer.startTag("", "vMP");
            xmlSerializer.text(String.format(Locale.US, "%.2f", totalReceived));
            xmlSerializer.endTag("", "vMP");

            xmlSerializer.endTag("", "MP");
            xmlSerializer.endTag("", "pgto");

            xmlSerializer.endTag("", "infCFe");
            xmlSerializer.endTag("", "CFe");
            xmlSerializer.endDocument();

            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void createTotalXml(XmlSerializer xmlSerializer, boolean servicePaid, double totalItems, double discount) throws IOException {
        xmlSerializer.startTag("", "total");

        double result = discount * -1;

        if(servicePaid){
            double serviceTax = totalItems * 0.1;
            result += serviceTax;
        }

        if(result > 0){
            xmlSerializer.startTag("", "DescAcrEntr");
            xmlSerializer.startTag("", "vAcresSubtot");
            xmlSerializer.text(String.format(Locale.US, "%.2f", result));
            xmlSerializer.endTag("", "vAcresSubtot");
            xmlSerializer.endTag("", "DescAcrEntr");
        }
        else if(result < 0){
            xmlSerializer.startTag("", "DescAcrEntr");
            xmlSerializer.startTag("", "vDescSubtot");
            xmlSerializer.text(String.format(Locale.US, "%.2f", result * -1));
            xmlSerializer.endTag("", "vDescSubtot");
            xmlSerializer.endTag("", "DescAcrEntr");
        }

        xmlSerializer.endTag("", "total");
    }

    private static void createDestinatarioXml(XmlSerializer xmlSerializer, String cpf) throws IOException {
        xmlSerializer.startTag("", "dest");
        if(cpf != null && !cpf.isEmpty()) {
            xmlSerializer.startTag("", "CPF");
            xmlSerializer.text(cpf);
            xmlSerializer.endTag("", "CPF");
        }
        xmlSerializer.endTag("", "dest");
    }

    private static void createEmitenteXml(XmlSerializer xmlSerializer) throws IOException {
        Kiosk kiosk = QuiosgramaApp.getKiosk();

        xmlSerializer.startTag("", "emit");

        xmlSerializer.startTag("", "CNPJ");
        xmlSerializer.text(kiosk.cnpj);
        xmlSerializer.endTag("", "CNPJ");

        xmlSerializer.startTag("", "IE");
        xmlSerializer.text(kiosk.ie);
        xmlSerializer.endTag("", "IE");

        xmlSerializer.startTag("", "IM");
        xmlSerializer.text(kiosk.im);
        xmlSerializer.endTag("", "IM");

        xmlSerializer.startTag("", "indRatISSQN");
        xmlSerializer.text("N");
        xmlSerializer.endTag("", "indRatISSQN");

        xmlSerializer.endTag("", "emit");
    }

    private static void createAcXml(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startTag("", "ide");

        xmlSerializer.startTag("", "CNPJ");
        xmlSerializer.text("16716114000172");
        xmlSerializer.endTag("", "CNPJ");

        xmlSerializer.startTag("", "signAC");
        xmlSerializer.text("SGR-SAT SISTEMA DE GESTAO E RETAGUARDA DO SAT");
        xmlSerializer.endTag("", "signAC");

        xmlSerializer.startTag("", "numeroCaixa");
        xmlSerializer.text("001");
        xmlSerializer.endTag("", "numeroCaixa");

        xmlSerializer.endTag("", "ide");
    }

    private static double createProductXml(XmlSerializer xmlSerializer, ProductRequest prodReq) throws IOException, JSONException {
        Product product = prodReq.product;
        LAST_PRODUCT = product.name;
        JSONObject taxJson = new JSONObject(product.tax);

        xmlSerializer.startTag("", "prod");

        xmlSerializer.startTag("", "cProd");
        xmlSerializer.text(String.valueOf(product.code));
        xmlSerializer.endTag("", "cProd");

        xmlSerializer.startTag("", "xProd");
        xmlSerializer.text(String.valueOf(product.name));
        xmlSerializer.endTag("", "xProd");

        xmlSerializer.startTag("", "NCM");
        xmlSerializer.text(taxJson.getString("NCM"));
        xmlSerializer.endTag("", "NCM");

        xmlSerializer.startTag("", "CFOP");
        xmlSerializer.text("5100");
        xmlSerializer.endTag("", "CFOP");

        xmlSerializer.startTag("", "uCom");
        xmlSerializer.text("un");
        xmlSerializer.endTag("", "uCom");

        xmlSerializer.startTag("", "qCom");
        xmlSerializer.text(String.format(Locale.US, "%d.0000", prodReq.quantity));
        xmlSerializer.endTag("", "qCom");

        xmlSerializer.startTag("", "vUnCom");
        xmlSerializer.text(String.format(Locale.US, "%.2f", product.price));
        xmlSerializer.endTag("", "vUnCom");

        xmlSerializer.startTag("", "indRegra");
        xmlSerializer.text("A");
        xmlSerializer.endTag("", "indRegra");

        double totalItem = prodReq.quantity * product.price;
        if(prodReq.complement != null && prodReq.complement.price > 0){
            totalItem += prodReq.quantity * prodReq.complement.price;
            xmlSerializer.startTag("", "vOutro");
            xmlSerializer.text(String.format(Locale.US, "%.2f", prodReq.complement.price));
            xmlSerializer.endTag("", "vOutro");
        }

        xmlSerializer.endTag("", "prod");

        createImpostoXml(xmlSerializer, taxJson);

        return totalItem;
    }

    private static void createImpostoXml(XmlSerializer xmlSerializer, JSONObject taxJson) throws IOException, JSONException {
        xmlSerializer.startTag("", "imposto");

        xmlSerializer.startTag("", "vItem12741");
        xmlSerializer.text(taxJson.getString("vItem12741"));
        xmlSerializer.endTag("", "vItem12741");

        createIcmsXml(xmlSerializer, taxJson);
        createPisXml(xmlSerializer, taxJson);
        createCofinsXml(xmlSerializer, taxJson);

        xmlSerializer.endTag("", "imposto");
    }

    private static void createIcmsXml(XmlSerializer xmlSerializer, JSONObject taxJson) throws IOException, JSONException {
        JSONObject icmsJson = taxJson.getJSONObject("ICMS");
        xmlSerializer.startTag("", "ICMS");
        xmlSerializer.startTag("", icmsJson.getString("description"));

        xmlSerializer.startTag("", "Orig");
        xmlSerializer.text(icmsJson.getString("Orig"));
        xmlSerializer.endTag("", "Orig");

        if(!icmsJson.isNull("CST")) {
            xmlSerializer.startTag("", "CST");
            xmlSerializer.text(icmsJson.getString("CST"));
            xmlSerializer.endTag("", "CST");
        }

        if(!icmsJson.isNull("CSOSN")) {
            xmlSerializer.startTag("", "CSOSN");
            xmlSerializer.text(icmsJson.getString("CSOSN"));
            xmlSerializer.endTag("", "CSOSN");
        }

        if(!icmsJson.isNull("pICMS")) {
            xmlSerializer.startTag("", "pICMS");
            xmlSerializer.text(icmsJson.getString("pICMS"));
            xmlSerializer.endTag("", "pICMS");
        }

        xmlSerializer.endTag("", icmsJson.getString("description"));
        xmlSerializer.endTag("", "ICMS");
    }

    private static void createPisXml(XmlSerializer xmlSerializer, JSONObject taxJson) throws IOException, JSONException {
        JSONObject pisJson = taxJson.getJSONObject("PIS");
        xmlSerializer.startTag("", "PIS");
        xmlSerializer.startTag("", pisJson.getString("description"));

        if(!pisJson.isNull("CST")) {
            xmlSerializer.startTag("", "CST");
            xmlSerializer.text(pisJson.getString("CST"));
            xmlSerializer.endTag("", "CST");
        }

        if(!pisJson.isNull("vBC")) {
            xmlSerializer.startTag("", "vBC");
            xmlSerializer.text(pisJson.getString("vBC"));
            xmlSerializer.endTag("", "vBC");
        }

        if(!pisJson.isNull("pPIS")) {
            xmlSerializer.startTag("", "pPIS");
            xmlSerializer.text(pisJson.getString("pPIS"));
            xmlSerializer.endTag("", "pPIS");
        }

        if(!pisJson.isNull("qBCProd")) {
            xmlSerializer.startTag("", "qBCProd");
            xmlSerializer.text(pisJson.getString("qBCProd"));
            xmlSerializer.endTag("", "qBCProd");
        }

        if(!pisJson.isNull("vAliqProd")) {
            xmlSerializer.startTag("", "vAliqProd");
            xmlSerializer.text(pisJson.getString("vAliqProd"));
            xmlSerializer.endTag("", "vAliqProd");
        }

        xmlSerializer.endTag("", pisJson.getString("description"));
        xmlSerializer.endTag("", "PIS");
    }

    private static void createCofinsXml(XmlSerializer xmlSerializer, JSONObject taxJson) throws IOException, JSONException {
        JSONObject cofinsJson = taxJson.getJSONObject("COFINS");
        xmlSerializer.startTag("", "COFINS");
        xmlSerializer.startTag("", cofinsJson.getString("description"));

        if(!cofinsJson.isNull("CST")) {
            xmlSerializer.startTag("", "CST");
            xmlSerializer.text(cofinsJson.getString("CST"));
            xmlSerializer.endTag("", "CST");
        }

        if(!cofinsJson.isNull("vBC")) {
            xmlSerializer.startTag("", "vBC");
            xmlSerializer.text(cofinsJson.getString("vBC"));
            xmlSerializer.endTag("", "vBC");
        }

        if(!cofinsJson.isNull("pPIS")) {
            xmlSerializer.startTag("", "pPIS");
            xmlSerializer.text(cofinsJson.getString("pPIS"));
            xmlSerializer.endTag("", "pPIS");
        }

        if(!cofinsJson.isNull("qBCProd")) {
            xmlSerializer.startTag("", "qBCProd");
            xmlSerializer.text(cofinsJson.getString("qBCProd"));
            xmlSerializer.endTag("", "qBCProd");
        }

        if(!cofinsJson.isNull("vAliqProd")) {
            xmlSerializer.startTag("", "vAliqProd");
            xmlSerializer.text(cofinsJson.getString("vAliqProd"));
            xmlSerializer.endTag("", "vAliqProd");
        }

        xmlSerializer.endTag("", cofinsJson.getString("description"));
        xmlSerializer.endTag("", "COFINS");
    }

    public static void connectEasySat(QuiosgramaApp app, PendingIntent usbPermissionIntent) {
        UsbManager manager = (UsbManager) app.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (EasySAT.isEasySATDevice(device)) {
                if (manager.hasPermission(device)) {
                    app.easySat.setDevice(device);
                } else {
                    manager.requestPermission(device, usbPermissionIntent);
                }
                break;
            }
        }
    }

    public static void testCouponIssue(Context context) throws SATNotConnectedException, SATCommunicationException, IOException {
        QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();

        InputStream inputStream = context.getAssets().open("CFeVenda.xml");
        String xmlVenda = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
        String result = app.easySat.enviarDadosVenda(genNumeroSessao(), CODIGO_DE_ATIVACAO, xmlVenda);
        String[] fields = result.split("\\|");
        if (fields.length >= 9) {
            ultimaChaveVenda = fields[8];
        }
    }
}
