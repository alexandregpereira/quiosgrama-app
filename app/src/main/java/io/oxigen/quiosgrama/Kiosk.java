package io.oxigen.quiosgrama;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * Created by Alexandre on 11/06/2016.
 */
public class Kiosk implements Parcelable{

    public String name;
    public String companyName;
    public String cnpj;
    public String ie;
    public String im;
    public String address;
    public String licence;

    public Kiosk(){
        companyName = "companyName";
        cnpj = "05761098000113";
        ie = "111111111111";
        im = "123123";
        address = "testeaddress";
    }

    public Kiosk(Kiosk kiosk){
        name = kiosk.name;
        companyName = kiosk.companyName;
        cnpj = kiosk.cnpj;
        ie = kiosk.ie;
        im = kiosk.im;
        address = kiosk.address;
        licence = kiosk.licence;
    }

    public Kiosk(Parcel p){
        name = p.readString();
        companyName = p.readString();
        cnpj = p.readString();
        ie = p.readString();
        im = p.readString();
        address = p.readString();
        licence = p.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(name);
        p.writeString(companyName);
        p.writeString(cnpj);
        p.writeString(ie);
        p.writeString(im);
        p.writeString(address);
        p.writeString(licence);
    }

    public static final Creator<Kiosk> CREATOR = new Creator<Kiosk>() {

        @Override
        public Kiosk createFromParcel(Parcel source) {
            return new Kiosk(source);
        }

        @Override
        public Kiosk[] newArray(int size) {
            return new Kiosk[size];
        }
    };
}
