package io.oxigen.quiosgrama.observer;

import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.service.SyncServerService;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class TableNameObserver extends ContentObserver{
	
	private Context context;

	public TableNameObserver(Handler handler, Context context) {
		super(handler);
		this.context = context;
	}

	@Override
	public void onChange(boolean selfChange) {
		this.onChange(selfChange, null);
	}		

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		Intent intent = new Intent(context, SyncServerService.class);
		intent.putExtra(KeysContract.METHOD_KEY, SyncServerService.SEND_TABLE);
		context.startService(intent);
	}
}
