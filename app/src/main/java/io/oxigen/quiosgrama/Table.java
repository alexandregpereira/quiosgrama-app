package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.Date;

public class Table implements Parcelable, Comparable<Table>{

	@Expose
	public final int number;
	@Expose
	public int xPosInDpi;
	@Expose
	public int yPosDpi;
	@Expose
	public int mapPageNumber;
	@Expose
	public Functionary waiterAlterTable;
	@Expose
	public Date tableTime;
	@Expose
	public Client client;
	public int syncStatus;
	@Expose
	public String clientTemp;
	public boolean moved;
	@Expose
	public boolean show = true;

	public Table(Table table){
		number = table.number;
		xPosInDpi = table.xPosInDpi;
		yPosDpi = table.yPosDpi;
		mapPageNumber = table.mapPageNumber;
		waiterAlterTable = table.waiterAlterTable;
		tableTime = table.tableTime;
		syncStatus = table.syncStatus;
		clientTemp = table.clientTemp;
		show = table.show;
	}
	
	public Table(int tableNumber, Functionary waiterAlterTable){
		number = tableNumber;
		xPosInDpi = 25;
		this.waiterAlterTable = waiterAlterTable;
		tableTime = new Date();
		syncStatus = 1;
	}
	
	public Table(int tableNumber, int xPosInDpi, int yPosDpi, int mapPageNumber, Date tableTime,
			Functionary waiterAlterTable, int syncStatus, Client client, String clientTemp, boolean show){
		number = tableNumber;
		this.xPosInDpi = xPosInDpi;
		this.yPosDpi = yPosDpi;
		this.mapPageNumber = mapPageNumber;
		this.waiterAlterTable = waiterAlterTable;
		this.tableTime = tableTime;
		this.syncStatus = syncStatus;
		this.client = client;
		this.clientTemp = clientTemp;
		this.show = show;
	}
	
	public Table(Parcel p){
		number = p.readInt();
		xPosInDpi = p.readInt();
		yPosDpi = p.readInt();
		mapPageNumber = p.readInt();
		waiterAlterTable = p.readParcelable(Functionary.class.getClassLoader());
		tableTime = (Date) p.readSerializable();
		syncStatus = p.readInt();
		clientTemp = p.readString();
		client = p.readParcelable(Client.class.getClassLoader());
		show = p.readByte() != 0;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(number);
		p.writeInt(xPosInDpi);
		p.writeInt(yPosDpi);
		p.writeInt(mapPageNumber);
		p.writeParcelable(waiterAlterTable, flags);
		p.writeSerializable(tableTime);
		p.writeInt(syncStatus);
		p.writeString(clientTemp);
		p.writeParcelable(client, flags);
		p.writeByte((byte) (show ? 1 : 0));
	}
	
	public static final Creator<Table> CREATOR = new Creator<Table>() {

		@Override
		public Table createFromParcel(Parcel source) {
			return new Table(source);
		}

		@Override
		public Table[] newArray(int size) {
			return new Table[size];
		}		
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
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
		Table other = (Table) obj;
		if (number != other.number)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if(client != null)
			return String.valueOf(number) + " - " + client.toString();
		else if(clientTemp != null)
			return String.valueOf(number) + " - " + clientTemp;
		return String.valueOf(number);
	}

	@Override
	public int compareTo(Table table) {
		if(number > table.number){
			return 1;
		}
		else if(number < table.number)
			return -1;
		return 0;
	}
}
