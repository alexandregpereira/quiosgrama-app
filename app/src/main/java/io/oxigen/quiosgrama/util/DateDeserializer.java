package io.oxigen.quiosgrama.util;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.oxigen.quiosgrama.data.KeysContract;

public class DateDeserializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

	@Override
	public Date deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
		SimpleDateFormat dateFormat[] = new SimpleDateFormat[]{
				new SimpleDateFormat(KeysContract.DATE_FORMAT_KEY, Locale.US),
				new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.US),
				new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.US),
				new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US),
				new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US)
		};
		String myDate = je.getAsString();
		for (SimpleDateFormat simpleDateFormat : dateFormat) {
			try {
				return simpleDateFormat.parse(myDate);
			} catch (ParseException e) {
			}
		}

		Log.e("DateDeserializer", "Erro ao deserializar data");
		return null;
	}

	@Override
	public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(KeysContract.DATE_FORMAT_KEY, Locale.US);
		JsonObject obj = new JsonObject();
		obj.addProperty("date", dateFormat.format(date));
		return obj.get("date");
	}
}
