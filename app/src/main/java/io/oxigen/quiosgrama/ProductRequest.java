package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.Date;

public class ProductRequest implements Parcelable, Comparable<ProductRequest>{

	public static final int NOT_VISUALIZED_STATUS = 0;
	public static final int VISUALIZED_STATUS = 1;
	public static final int READY_STATUS = 2;

	@Expose
	public final Request request;
	@Expose
	public final Product product;
	@Expose
	public Complement complement;
	//Nao usado no sistema, usado a quantidade dentro de produto. Usado para converter o JSON do webservice
	@Expose
	public int quantity;
	@Expose
	public boolean valid;
	@Expose
	public String transferRoute;
	@Expose
	public Date productRequestTime;
	@Expose
	public int status;
	public int syncStatus;

	public ProductRequest(Request request, Product product, Complement complement){
		this.request = new Request(request);
		this.product = new Product(product);
		this.complement = complement;
		valid = true;
		productRequestTime = new Date();
		status = 0;
		syncStatus = 1;
	}

	public ProductRequest(Request request, Product product, Complement complement, boolean valid, String transferRoute, Date productRequestTime, int status, int syncStatus){
		this.request = new Request(request);
		this.product = new Product(product);
		this.complement = complement;
		this.valid = valid;
		this.transferRoute = transferRoute;
		this.productRequestTime = productRequestTime;
		this.status = status;
		this.syncStatus = syncStatus;
	}

	public ProductRequest(Parcel p){
		request = p.readParcelable(Request.class.getClassLoader());
		product = p.readParcelable(Product.class.getClassLoader());
		complement = p.readParcelable(Complement.class.getClassLoader());
		quantity = p.readInt();
		valid = p.readByte() != 0;
		transferRoute = p.readString();
		productRequestTime = (Date) p.readSerializable();
		status = p.readInt();
		syncStatus = p.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeParcelable(request, flags);
		p.writeParcelable(product, flags);
		p.writeParcelable(complement, flags);
		p.writeInt(quantity);
		p.writeByte((byte) (valid ? 1 : 0));
		p.writeString(transferRoute);
		p.writeSerializable(productRequestTime);
		p.writeInt(status);
		p.writeInt(syncStatus);
	}
	
	public static final Creator<ProductRequest> CREATOR = new Creator<ProductRequest>() {

		@Override
		public ProductRequest createFromParcel(Parcel source) {
			return new ProductRequest(source);
		}

		@Override
		public ProductRequest[] newArray(int size) {
			return new ProductRequest[size];
		}		
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((complement == null) ? 0 : complement.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((request == null) ? 0 : request.hashCode());
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
		ProductRequest other = (ProductRequest) obj;
		if (complement == null) {
			if (other.complement != null)
				return false;
		} else if (!complement.equals(other.complement))
			return false;
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		if (request == null) {
			if (other.request != null)
				return false;
		} else if (!request.equals(other.request))
			return false;
		return true;
	}

	public void setTransferRoute(Bill previousBill, Bill bill) {
		setTransferRouteDefault(previousBill, bill);
		this.request.bill = bill;
	}

	public void setTransferRoute(Bill postBill) {
		setTransferRouteDefault(request.bill, postBill);
	}

	private void setTransferRouteDefault(Bill previousBill, Bill postBill){
		if(this.transferRoute != null){
			this.transferRoute += " >> " + postBill.table.number;
		}
		else{
			this.transferRoute = previousBill.table.number + " >> " + postBill.table.number;
		}
	}

	@Override
	public int compareTo(ProductRequest productRequest) {
		if (request.requestTime != null && productRequest.request.requestTime != null) {
			if(request.syncStatus == productRequest.request.syncStatus){
				return request.requestTime.compareTo(productRequest.request.requestTime);
			}
			else if(request.syncStatus != 0){
				return 1;
			}
			else{
				return -1;
			}
		}
		return 0;
	}
}
