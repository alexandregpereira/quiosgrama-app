package io.oxigen.quiosgrama;

public class Device {

	public final String imei;
	public final String registrationId;
	public final String ip;
	public final String billId;

	public Device(String id, String registrationId, String ip, String billId){
		this.imei = id;
		this.registrationId = registrationId;
		this.ip = ip;
		this.billId = billId;
	}
}
