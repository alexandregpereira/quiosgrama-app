package io.oxigen.quiosgrama;

import java.util.HashSet;

import com.google.gson.annotations.Expose;

public class Push<T> {

	@Expose
	public String imei;
	@Expose
	public HashSet<T> list;
	
	public Push(String imei, HashSet<T> list){
		this.imei = imei;
		this.list = list;
	}
}
