package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

public class Function implements Parcelable {

	public long id;
	public String name;
	
	public Function(){}
	
	public Function(Parcel p){
		id = p.readLong();
		name = p.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeLong(id);
		p.writeString(name);
	}
	
	public static final Creator<Function> CREATOR = new Creator<Function>() {

		@Override
		public Function createFromParcel(Parcel source) {
			return new Function(source);
		}

		@Override
		public Function[] newArray(int size) {
			return new Function[size];
		}		
	};
}
