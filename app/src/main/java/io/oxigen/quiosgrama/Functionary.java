package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

public class Functionary implements Parcelable{

	public static final int WAITER = 0;
	public static final int ADMIN = 1;
	public static final int CLIENT = 2;
	public static final int CLIENT_WAITER = 3;

	@Expose
	public final long id;
	@Expose
	public String name;
	@Expose
	public Function function;
	@Expose
	public String imei;
	@Expose
	public int adminFlag;
	
	public Functionary(long id){
		this.id = id;
	}
	
	public Functionary(long id, String name, int adminFlag){
		this.id = id;
		this.name = name;
		this.adminFlag = adminFlag;
	}
	
	public Functionary(Parcel p){
		id = p.readLong();
		name = p.readString();
		function = p.readParcelable(Function.class.getClassLoader());
		adminFlag = p.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeLong(id);
		p.writeString(name);
		p.writeParcelable(function, flags);
		p.writeInt(adminFlag);
	}
	
	public static final Creator<Functionary> CREATOR = new Creator<Functionary>() {

		@Override
		public Functionary createFromParcel(Parcel source) {
			return new Functionary(source);
		}

		@Override
		public Functionary[] newArray(int size) {
			return new Functionary[size];
		}		
	};
	
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((imei == null) ? 0 : imei.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Functionary other = (Functionary) obj;
		if (id != other.id)
			return false;
		if (imei == null) {
			if (other.imei != null)
				return false;
		} else if (!imei.equals(other.imei))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
