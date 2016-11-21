package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Container implements Parcelable {

	public ArrayList<Product> productList;
	public ArrayList<ProductType> productTypeList;
	public ArrayList<Request> requestList;
	public ArrayList<Bill> billList;
	public ArrayList<Complement> complementList;
	public ArrayList<Function> functionList;
	public ArrayList<Functionary> functionaryList;
	public ArrayList<ProductRequest> productRequestList;
	public ArrayList<Table> tableList;
	public ArrayList<Poi> poiList;
	public ArrayList<Client> clientList;

	public Container(){}

	public Container(Parcel p){
		productList = new ArrayList<Product>();
		p.readList(productList, Product.class.getClassLoader());
		productTypeList = new ArrayList<ProductType>();
		p.readList(productTypeList, ProductType.class.getClassLoader());
		requestList = new ArrayList<Request>();
		p.readList(requestList, Request.class.getClassLoader());
		billList = new ArrayList<Bill>();
		p.readList(billList, Bill.class.getClassLoader());
		complementList = new ArrayList<Complement>();
		p.readList(complementList, Complement.class.getClassLoader());
		functionList = new ArrayList<Function>();
		p.readList(functionList, Function.class.getClassLoader());
		functionaryList = new ArrayList<Functionary>();
		p.readList(functionaryList, Functionary.class.getClassLoader());
		productRequestList = new ArrayList<ProductRequest>();
		p.readList(productRequestList, ProductRequest.class.getClassLoader());
		tableList = new ArrayList<Table>();
		p.readList(tableList, Table.class.getClassLoader());
		poiList = new ArrayList<Poi>();
		p.readList(poiList, Poi.class.getClassLoader());
		clientList = new ArrayList<Client>();
		p.readList(clientList, Client.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeList(productList);
		p.writeList(productTypeList);
		p.writeList(requestList);
		p.writeList(billList);
		p.writeList(complementList);
		p.writeList(functionList);
		p.writeList(functionaryList);
		p.writeList(productRequestList);
		p.writeList(tableList);
		p.writeList(poiList);
		p.writeList(clientList);
	}

	public static final Creator<Container> CREATOR = new Creator<Container>() {

		@Override
		public Container createFromParcel(Parcel source) {
			return new Container(source);
		}

		@Override
		public Container[] newArray(int size) {
			return new Container[size];
		}
	};
}
