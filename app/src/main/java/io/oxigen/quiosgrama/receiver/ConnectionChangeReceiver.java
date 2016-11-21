package io.oxigen.quiosgrama.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.service.SyncServerService;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
					NetworkInfo networkInfo =
							intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					if(networkInfo.isConnected() && !AndroidUtil.isMyServiceRunning(context, SyncServerService.class.getName())) {
						// Wifi is connected
						Intent intentService = new Intent(context, SyncServerService.class);
						intentService.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
						context.startService(intentService);
					}
				}
			}
		}, 200);
	}

}
