package io.oxigen.quiosgrama.service;

import android.app.IntentService;
import android.content.Intent;

/**
 *
 * Created by Alexandre on 16/08/2016.
 */
public class FiscalService extends IntentService {

    public FiscalService(){
        super(FiscalService.class.getName());
    }

    public FiscalService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        execute();
    }

    private void execute() {

    }
}
