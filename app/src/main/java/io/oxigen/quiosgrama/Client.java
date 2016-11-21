package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

public class Client implements Parcelable {
	
	@Expose
	public long id;
	@Expose
	public String name;
	@Expose
	public String cpf;
	@Expose
	public String phone;
	@Expose
	public boolean tempFlag;
	@Expose
	public boolean presentFlag;
	
	public Client(){}
	
	public Client(Client client){
		id = client.id;
		name = client.name;
		cpf = client.cpf;
		phone = client.phone;
		tempFlag = client.tempFlag;
		presentFlag = client.presentFlag;
	}
	
	public Client(String name){
		this.name = name;
		tempFlag = true;
	}
	
	public Client(long id, String name,
			String cpf, String phone, int tempFlag, int presentFlag) {
		this.id = id;
		this.name = name;
		this.cpf = cpf;
		this.phone = phone;
		this.tempFlag = tempFlag == 1;
		this.presentFlag = presentFlag == 1;
	}

	public Client(Parcel p) {
		this.id = p.readLong();
		this.name = p.readString();
		this.cpf = p.readString();
		this.phone = p.readString();
		this.tempFlag = p.readByte() != 0;
		this.presentFlag = p.readByte() != 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(cpf);
		dest.writeString(phone);
		dest.writeByte((byte) (tempFlag ? 1 : 0)); 
		dest.writeByte((byte) (presentFlag ? 1 : 0)); 
	}
	
	public static final Creator<Client> CREATOR = new Creator<Client>() {

		@Override
		public Client createFromParcel(Parcel source) {
			return new Client(source);
		}

		@Override
		public Client[] newArray(int size) {
			return new Client[size];
		}		
	};

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
		Client other = (Client) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
