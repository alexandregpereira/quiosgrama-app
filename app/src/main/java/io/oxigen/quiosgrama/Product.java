package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

public class Product implements Parcelable{

	@Expose
	public long code;
	@Expose
	public String name;
	@Expose
	public double price;
	@Expose
	public String description;
	@Expose
	public ProductType type;
	@Expose
	public int quantity;
	@Expose
	public int popularity;
	@Expose
	public String tax;

	public Product(){}
	
	public Product(Product p){
		code = p.code;
		name = p.name;
		price = p.price;
		description = p.description;
		type = p.type;
		quantity = p.quantity;
		popularity = p.popularity;
		tax = p.tax;
	}
	
	public Product(long id, String name, double price, int quantity, int popularity, ProductType type){
		this.code = id;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
		this.popularity = popularity;
		this.type = type;
	}

	public Product(long id, String name, double price, int quantity, int popularity, String tax, ProductType type){
		this.code = id;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
		this.popularity = popularity;
		this.tax = tax;
		this.type = type;
	}
	
	public Product(Parcel p){
		code = p.readLong();
		name = p.readString();
		price = p.readDouble();
		description = p.readString();
		type = p.readParcelable(ProductType.class.getClassLoader());
		quantity = p.readInt();
		popularity = p.readInt();
		tax = p.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeLong(code);
		p.writeString(name);
		p.writeDouble(price);
		p.writeString(description);
		p.writeParcelable(type, flags);
		p.writeInt(quantity);
		p.writeInt(popularity);
		p.writeString(tax);
	}
	
	public static final Creator<Product> CREATOR = new Creator<Product>() {

		@Override
		public Product createFromParcel(Parcel source) {
			return new Product(source);
		}

		@Override
		public Product[] newArray(int size) {
			return new Product[size];
		}		
	};
	
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (code ^ (code >>> 32));
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
		Product other = (Product) obj;
		if (code != other.code)
			return false;
		return true;
	}

}
