package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

public class ProductType implements Parcelable{

	@Expose
	public final long id;
	@Expose
	public String name;
	@Expose
	public int priority;
	@Expose
	public String buttonImage;
	@Expose
	public String imageInfo;
	@Expose
	public String colorId;
	public int imageInfoId;
	@Expose
	public int destination;
	@Expose
	public String destinationName;
	@Expose
	public String destinationIcon;
	@Expose
	public String printerIp;

	public ProductType(){
		id = 0l;
	}
	
	public ProductType(long id){
		this.id = id;
	}
	
	public ProductType(Parcel p){
		id = p.readLong();
		name = p.readString();
		priority = p.readInt();
		buttonImage = p.readString();
		imageInfo = p.readString();
		colorId = p.readString();
		imageInfoId = p.readInt();
		destination = p.readInt();
		destinationName = p.readString();
		destinationIcon = p.readString();
		printerIp = p.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeLong(id);
		p.writeString(name);
		p.writeInt(priority);
		p.writeString(buttonImage);
		p.writeString(imageInfo);
		p.writeString(colorId);
		p.writeInt(imageInfoId);
		p.writeInt(destination);
		p.writeString(destinationName);
		p.writeString(destinationIcon);
		p.writeString(printerIp);
	}
	
	public static final Creator<ProductType> CREATOR = new Creator<ProductType>() {

		@Override
		public ProductType createFromParcel(Parcel source) {
			return new ProductType(source);
		}

		@Override
		public ProductType[] newArray(int size) {
			return new ProductType[size];
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
		ProductType other = (ProductType) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
