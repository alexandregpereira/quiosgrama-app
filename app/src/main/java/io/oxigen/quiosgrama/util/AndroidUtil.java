package io.oxigen.quiosgrama.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.activity.MainActivity;
import io.oxigen.quiosgrama.data.KeysContract;

public class AndroidUtil {

	private static final String TAG = "AndroidUtil";

	public static AlertDialog.Builder createDialog(Context context, String title, String message){
		AlertDialog.Builder dialog = new AlertDialog.Builder( context );
		dialog.setTitle(title);
		dialog.setMessage(message);

		return dialog;		
	}
	
	public static Dialog createCustomDialog(Activity activity, int theme, int resource){
		Dialog dialog = new Dialog(activity, theme);
		LayoutInflater inflater = activity.getLayoutInflater();
		View dialogView = inflater.inflate(resource, null);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(dialogView);
		
		return dialog;
	}
	
	public static ProgressDialog createProgressDialog(Context context, String message){
		ProgressDialog progress = new ProgressDialog(context);
		progress.setCancelable(true);
		progress.setMessage(message);

		return progress;
	}
	
	public static boolean isFieldsEmpties(EditText... fields){
//		for (String param : params) {
//			if(param.equals(""))
//				return true;
//		}
//		
		return false;		
	}
	
	public static void hideKeyBoard(Context context, EditText field){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(field.getWindowToken(), 0);
	}
	
	public static void showKeyBoard(Context context, EditText field){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(field, 0);
	}
	
	public static String getBestProvider(LocationManager locationManager) throws IllegalArgumentException{
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

		return locationManager.getBestProvider(criteria, true);
	}
	
	public static String getGoogleAccount(Context context){
		String email = new String();
		
		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(context).getAccounts();
		
		for (Account account : accounts){
			if(emailPattern.matcher(account.name).matches()){
				email = account.name;
			}
		}

		return email;
	}
	
	public static void createNotification(Context context, String title, String text){
		if(Build.VERSION.SDK_INT >= 11) {
			NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(context)
							.setSmallIcon(R.drawable.ic_launcher)
							.setContentTitle(title)
							.setContentText(text);
			// Creates an explicit intent for an Activity in your app
//		Intent resultIntent = new Intent(context, MainActivity.class);

			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			// Adds the back stack for the Intent (but not the Intent itself)
//		stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
//		stackBuilder.addNextIntent(resultIntent);
//		PendingIntent resultPendingIntent =
//		        stackBuilder.getPendingIntent(
//		            0,
//		            PendingIntent.FLAG_UPDATE_CURRENT
//		        );
//		mBuilder.setContentIntent(resultPendingIntent);

			createNotification(context, 1, mBuilder);
		}
		else{
			createNotificationOldVersion(context, 1, title, text);
		}
	}

	public static void createNotification(Context context, int mId, String title, String text){
		if(Build.VERSION.SDK_INT >= 11) {
			NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(context)
							.setSmallIcon(R.drawable.ic_launcher)
							.setContentTitle(title)
							.setContentText(text);

			Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setSound(soundUri);

			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(context, MainActivity.class);

			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent =
					stackBuilder.getPendingIntent(
							0,
							PendingIntent.FLAG_UPDATE_CURRENT
					);
			mBuilder.setContentIntent(resultPendingIntent);

			createNotification(context, mId, mBuilder);
		}
		else{
			createNotificationOldVersion(context, mId, title, text);
		}
	}

	private static void createNotificationOldVersion(Context context, int mId, String title, String text) {
		Notification notification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		Intent i = new Intent(context, MainActivity.class);
		PendingIntent pd = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, title, text, pd);
		NotificationManager mN = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mN.notify(mId,notification);
	}

	private static void createNotification(Context context, int mId, NotificationCompat.Builder mBuilder){
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(mId, mBuilder.build());
	}
	
	public static void clearNotofication(Context context, int id){
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
	}
	
	public static int buildImagesValue(String image){
		for (Class<?> clazz : R.class.getDeclaredClasses()) {
			try {
				return clazz.getField(image).getInt(null);
			} catch (NoSuchFieldException e) {
			} catch (IllegalAccessException e) {
				Log.e(TAG, "Erro ao converter imagem");
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "Erro ao converter imagem");
			} catch (NullPointerException e) {
				Log.e(TAG, "Objetos null");
			}
		}
		
		return 0;
	}
	
	public static int dpToPx(int dp, Context context) {
		Resources r = context.getResources();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
	}
	
	public static int pxToDp(int px, Context context){
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return (int) dp;
	}
	
	public static boolean isMyServiceRunning(Context context, String serviceClassName) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClassName.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static String getIpAddress() { 
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
						String ipAddress=inetAddress.getHostAddress().toString();
						return ipAddress;
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("GetIP Address", ex.toString());
		}
		return null; 
	}
	
	public static String getImei(Context context){
		TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, androidId;
		String imei = telManager.getDeviceId();
		if(imei == null) {
			tmDevice = "0";
			tmSerial = "" + telManager.getSimSerialNumber();
			androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

			UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
			return deviceUuid.toString();
		}
		else{
			return imei;
		}
	}
	
	public static Date removeMillisecondsFromDate(Date date){
		try {
			SimpleDateFormat format = new SimpleDateFormat(KeysContract.DATE_FORMAT_KEY, Locale.US);
			return format.parse(format.format(date));
		} catch (ParseException e) {
			return date;
		}
	}

	public static String calculateDate(Context context, Date date1, Date date2){
		long diff = date2.getTime() - date1.getTime();
		long diffMinutes = diff / (60 * 1000);

		if(diffMinutes >= 60) {
			return ((int)diffMinutes / 60) + " " +context.getResources().getString(R.string.hours);
		}
		else if(diffMinutes > 0){
			return diffMinutes + " " +context.getResources().getString(R.string.minutes);
		}
		else{
			return ((int)diff / 1000) +  " " + context.getResources().getString(R.string.seconds);
		}
	}

	public static String calculateDateDays(Context context, Date date1, Date date2){
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

		boolean yesterday = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)-1;

		if(sameDay){
			SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
			return format.format(date1);
		}
		else if(yesterday){
			return context.getResources().getString(R.string.yesterday);
		}
		else{
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
			return format.format(date1);
		}
	}
}
