package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.Date;

import io.oxigen.quiosgrama.data.DataProviderContract;

public class Request implements Parcelable, Comparable<Request> {

    @Expose
    public final String id;
    @Expose
    public Date requestTime;
    @Expose
    public Bill bill;
    @Expose
    public Functionary waiter;
    public int syncStatus;

    public Request(Request request) {
        id = request.id;
        requestTime = request.requestTime;
        if (request.bill != null) {
            bill = new Bill(request.bill);
        }
        waiter = request.waiter;
        syncStatus = request.syncStatus;
    }

    public Request(String id, Date requestTime, Bill bill, Functionary waiter, int syncStatus) {
        this.id = id;
        this.requestTime = requestTime;
        this.bill = bill;
        this.waiter = waiter;
        this.syncStatus = syncStatus;
    }

    public Request(Functionary waiter) {
        id = DataProviderContract.idGenerator();
        requestTime = new Date();
        this.waiter = waiter;
        this.syncStatus = 1;
    }

    public Request(Functionary waiter, int transfer) {
        id = DataProviderContract.idGenerator();
        requestTime = new Date();
        this.waiter = waiter;
        this.syncStatus = 1;
    }

    public Request(Parcel p) {
        id = p.readString();
        requestTime = (Date) p.readSerializable();
        bill = p.readParcelable(Bill.class.getClassLoader());
        waiter = p.readParcelable(Functionary.class.getClassLoader());
        this.syncStatus = p.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(id);
        p.writeSerializable(requestTime);
        p.writeParcelable(bill, flags);
        p.writeParcelable(waiter, flags);
        p.writeInt(syncStatus);
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {

        @Override
        public Request createFromParcel(Parcel source) {
            return new Request(source);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
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
        Request other = (Request) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public int compareTo(Request request) {
        if (requestTime != null && request.requestTime != null) {
            return requestTime.compareTo(request.requestTime);
        }
        return 0;
    }
}
