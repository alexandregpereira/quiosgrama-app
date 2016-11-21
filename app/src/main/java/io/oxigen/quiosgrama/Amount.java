package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.KeysContract;

public class Amount implements Parcelable{

	public static final int PAID_METHOD_MONEY = 1;
	public static final int PAID_METHOD_CARD = 2;

	@Expose
	public final String id;
	@Expose
	public double value;
	@Expose
	public int paidMethod;
	@Expose
	public Bill bill;
	public int syncStatus;

	public Amount(double value, int paidMethod, Bill bill){
		id = DataProviderContract.idGenerator();
		this.value = value;
		this.paidMethod = paidMethod;
		this.bill = bill;
		syncStatus = KeysContract.NO_SYNCHRONIZED_STATUS_KEY;
	}

	public Amount(String id, double value, int paidMethod, Bill bill, int syncStatus){
		this.id = id;
		this.value = value;
		this.paidMethod = paidMethod;
		this.bill = bill;
		this.syncStatus = syncStatus;
	}

	public Amount(Parcel p){
		id = p.readString();
		value = p.readDouble();
		paidMethod = p.readInt();
		bill = p.readParcelable(Bill.class.getClassLoader());
		syncStatus = p.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeString(id);
		p.writeDouble(value);
		p.writeInt(paidMethod);
		p.writeParcelable(bill, flags);
		p.writeInt(syncStatus);
	}

	public static final Creator<Amount> CREATOR = new Creator<Amount>() {

		@Override
		public Amount createFromParcel(Parcel source) {
			return new Amount(source);
		}

		@Override
		public Amount[] newArray(int size) {
			return new Amount[size];
		}
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Amount other = (Amount) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
