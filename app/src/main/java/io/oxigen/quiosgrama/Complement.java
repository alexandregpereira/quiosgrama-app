package io.oxigen.quiosgrama;

import java.util.HashSet;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

public class Complement implements Parcelable{

	@Expose
	public final String description;
	@Expose
	public double price;
	@Expose
	public String drawable;
	@Expose
	public HashSet<ProductType> typeSet;
	
	public Complement(Complement complement){
		description = complement.description;
		price = complement.price;
		drawable = complement.drawable;
		typeSet = complement.typeSet;
	}
	
	public Complement(String description){
		this.description = description;
		typeSet = new HashSet<ProductType>();
	}
	
	public Complement(String description, double price, String drawableId){
		this.description = description;
		this.price = price;
		this.drawable = drawableId;
		typeSet = new HashSet<ProductType>(); 
	}
	
	public Complement(Parcel p){
		description = p.readString();
		price = p.readDouble();
		drawable = p.readString();
		typeSet = new HashSet<ProductType>(); 
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeString(description);
		p.writeDouble(price);
		p.writeString(drawable);
	}
	
	public static final Creator<Complement> CREATOR = new Creator<Complement>() {

		@Override
		public Complement createFromParcel(Parcel source) {
			return new Complement(source);
		}

		@Override
		public Complement[] newArray(int size) {
			return new Complement[size];
		}		
	};
	
	@Override
	public String toString() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
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
		Complement other = (Complement) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		return true;
	}

	public boolean containsProductType(ProductType type) {
		if(type != null && typeSet != null){
			return typeSet.contains(type);
		}
		return false;
	}
}
