package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.Date;

import io.oxigen.quiosgrama.data.DataProviderContract;

public class Bill implements Parcelable, Comparable<Bill>{

	@Expose
	public String id;
	@Expose
	public Date openTime;
	@Expose
	public Date closeTime;
	@Expose
	public Date paidTime;
	@Expose
	public Functionary waiterOpenTable;
	@Expose
	public Functionary waiterCloseTable;
	@Expose
	public Table table;
	@Expose
	public Date billTime;
	@Expose
	public boolean servicePaid;
	public int syncStatus;
	public double amountPaid;

	public Bill(Bill bill){
		id = bill.id;
		openTime = bill.openTime;
		closeTime = bill.closeTime;
		paidTime = bill.paidTime;
		waiterOpenTable = bill.waiterOpenTable;
		waiterCloseTable = bill.waiterCloseTable;
		table = bill.table;
		billTime = bill.billTime;
		syncStatus = bill.syncStatus;
		amountPaid = bill.amountPaid;
	}
	
	public Bill(String id, Date openTime, Date closeTime, Date paidTime,
			Functionary waiterOpenTable, Functionary waiterCloseTable, Date billTime, Table table, int syncStatus, boolean servicePaid){
		this.id = id;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.paidTime = paidTime;
		this.waiterOpenTable = waiterOpenTable;
		this.waiterCloseTable = waiterCloseTable;
		this.table = table;
		this.billTime = billTime;
		this.syncStatus = syncStatus;
		this.servicePaid = servicePaid;
	}
	
	public Bill(Table table, 
			Functionary waiterOpenTable, 
			Functionary waiterCloseTable, 
			Date openTime){
		id = DataProviderContract.idGenerator();
		this.openTime = openTime;
		this.table = table;
		this.waiterOpenTable = waiterOpenTable;
		this.waiterCloseTable = waiterCloseTable;
		billTime = new Date();
		syncStatus = 1;
		amountPaid = 0;
	}
	
	public Bill(Parcel p){
		id = p.readString();
		openTime = (Date) p.readSerializable();
		closeTime = (Date) p.readSerializable();
		paidTime = (Date) p.readSerializable();
		waiterOpenTable = p.readParcelable(Functionary.class.getClassLoader());
		waiterCloseTable = p.readParcelable(Functionary.class.getClassLoader());
		table = p.readParcelable(Table.class.getClassLoader());
		billTime = (Date) p.readSerializable();
		syncStatus = p.readInt();
		amountPaid = p.readDouble();
		servicePaid = p.readByte() != 0;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeString(id);
		p.writeSerializable(openTime);
		p.writeSerializable(closeTime);
		p.writeSerializable(paidTime);
		p.writeParcelable(waiterOpenTable, flags);
		p.writeParcelable(waiterCloseTable, flags);
		p.writeParcelable(table, flags);
		p.writeSerializable(billTime);
		p.writeInt(syncStatus);
		p.writeDouble(amountPaid);
		p.writeByte((byte) (servicePaid ? 1 : 0));
	}
	
	public static final Creator<Bill> CREATOR = new Creator<Bill>() {

		@Override
		public Bill createFromParcel(Parcel source) {
			return new Bill(source);
		}

		@Override
		public Bill[] newArray(int size) {
			return new Bill[size];
		}		
	};

	@Override
	public String toString() {
		return "Mesa "+ table.number;
	}

	@Override
	public int compareTo(Bill bill) {
		if(table != null && bill.table != null){
			return table.compareTo(bill.table);
		}
		return 0;
	}

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
		Bill other = (Bill) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
