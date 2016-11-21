package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.Date;

public class Poi implements Parcelable, Comparable<Poi>{
	
	@Expose
	public int idPoi;
	@Expose
	public String name;
	@Expose
	public int xPosDpi;
	@Expose
	public int yPosDpi;
	@Expose
	public String image;
	@Expose
	public int mapPageNumber;
	@Expose
	public Functionary waiterAlterPoi;
	@Expose
	public Date poiTime;
	public int syncStatus;
	public boolean moved;

	public Poi(Poi poi){
		idPoi = poi.idPoi;
		name = poi.name;
		xPosDpi = poi.xPosDpi;
		yPosDpi = poi.yPosDpi;
		image = poi.image;
		mapPageNumber = poi.mapPageNumber;
		waiterAlterPoi = poi.waiterAlterPoi;
		poiTime = poi.poiTime;
		syncStatus = poi.syncStatus;
	}
	
	public Poi(int idPoi, String name, int xPosDpi, int yPosDpi, String image, int mapPageNumber, Functionary waiter, Date poiTime, int syncStatus){
		this.idPoi = idPoi;
		this.name = name;
		this.xPosDpi = xPosDpi;
		this.yPosDpi = yPosDpi;
		this.image = image;
		this.mapPageNumber = mapPageNumber;
		waiterAlterPoi = waiter;
		this.poiTime = poiTime;
		this.syncStatus = syncStatus;
	}
	
	public Poi(Parcel p){
		idPoi = p.readInt();
		name = p.readString();
		xPosDpi = p.readInt();
		yPosDpi = p.readInt();
		image = p.readString();
		mapPageNumber = p.readInt();
		waiterAlterPoi = p.readParcelable(Functionary.class.getClassLoader());
		poiTime = (Date) p.readSerializable();
		syncStatus = p.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(idPoi);
		p.writeString(name);
		p.writeInt(xPosDpi);
		p.writeInt(yPosDpi);
		p.writeString(image);
		p.writeInt(mapPageNumber);
		p.writeParcelable(waiterAlterPoi, flags);
		p.writeSerializable(poiTime);
		p.writeInt(syncStatus);
	}

	@Override
	public int compareTo(Poi poi) {
		if(idPoi > poi.idPoi)
			return 1;
		else if(idPoi < poi.idPoi)
			return -1;
		return 0;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static final Creator<Poi> CREATOR = new Creator<Poi>() {

		@Override
		public Poi createFromParcel(Parcel source) {
			return new Poi(source);
		}

		@Override
		public Poi[] newArray(int size) {
			return new Poi[size];
		}		
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idPoi;
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
		Poi other = (Poi) obj;
		if (idPoi != other.idPoi)
			return false;
		return true;
	}
}
