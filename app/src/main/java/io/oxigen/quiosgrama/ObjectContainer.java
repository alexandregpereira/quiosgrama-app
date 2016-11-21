package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * Created by Alexandre on 11/06/2016.
 */
public class ObjectContainer  implements Parcelable {

    public Kiosk kiosk;
    public Container container;
    public String message;

    public ObjectContainer(){}

    public ObjectContainer(Parcel p){
        kiosk = p.readParcelable(Kiosk.class.getClassLoader());
        container = p.readParcelable(Container.class.getClassLoader());
        message = p.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeParcelable(kiosk, flags);
        p.writeParcelable(container, flags);
        p.writeString(message);
    }

    public static final Creator<ObjectContainer> CREATOR = new Creator<ObjectContainer>() {

        @Override
        public ObjectContainer createFromParcel(Parcel source) {
            return new ObjectContainer(source);
        }

        @Override
        public ObjectContainer[] newArray(int size) {
            return new ObjectContainer[size];
        }
    };

    @Override
    public String toString() {
        return message;
    }
}
