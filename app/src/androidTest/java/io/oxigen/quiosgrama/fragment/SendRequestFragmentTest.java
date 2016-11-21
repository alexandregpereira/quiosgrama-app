package io.oxigen.quiosgrama.fragment;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Complement;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.Table;

import static org.junit.Assert.assertTrue;

/**
 * Created by Alexandre on 10/04/2016.
 *
 */
public class SendRequestFragmentTest extends SendRequestFragment{

    Request previousRequest;

    @Before
    public void buildMockup(){
        Functionary waiter = new Functionary(1, "Teste", Functionary.ADMIN);
        ArrayList<Functionary> waiterList = new ArrayList<>();
        waiterList.add(waiter);
        Table table = new Table(5, waiter);

        Bill bill = new Bill(table,
                waiter,
                null,
                new Date());
        ArrayList<Bill> billList = new ArrayList<>();
        billList.add(bill);

        ArrayList<Table> tableList = new ArrayList<>();
        tableList.add(table);

        app = new QuiosgramaApp();
        app.createFunctionaryList(waiterList);
        app.createBillList(billList);

        productRequestList = new ArrayList<>();
        request = new Request(waiter);

        request.bill = bill;
        previousRequest = new Request(request);



    }

    @Test
    public void validarListaDeProdutosDaContaAnterior() {
        final int PREVIOUS_QUANTITY = 4;
        final int POST_QUANTITY = 1;

        Product prod = new Product(1, "Produto", 5, PREVIOUS_QUANTITY, 1, null);
        ProductRequest prodReq = new ProductRequest(request, prod, new Complement(""));
        productRequestList.add(prodReq);

        createPreviousProductRequestList();

        prodReq.product.quantity = POST_QUANTITY;

        sendRequest(20);
        boolean test = true;
        String message = "validarListaDeProdutosDaContaAnterior: ";

        for(ProductRequest previousProdReq: mPreviousProductRequestList){
            if(previousProdReq.valid){
                test = previousProdReq.request.equals(previousRequest)
                        && previousProdReq.product.quantity == (PREVIOUS_QUANTITY - POST_QUANTITY);

                if(!test){
                    message += "Erro na diferença da lista anterior. Produto valido";
                    break;
                }
            }
            else{
                test = !previousProdReq.request.equals(previousRequest)
                        && previousProdReq.product.quantity == POST_QUANTITY;

                if(!test){
                    message += "Erro na diferença da lista anterior. Produto invalido";
                    break;
                }
            }
        }

        assertTrue(message, test);
    }

    @Override
    protected void sendRequest(int tableNumber) {
        try {
            super.sendRequest(tableNumber);
        }catch (Exception e){

        }
    }

}
