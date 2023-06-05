package com.cbozan.view.helper;

import java.util.regex.Pattern;

public abstract class Control {



	public static boolean phoneNumberControl(String phoneNumber, Pattern pattern) {
		return pattern.matcher(phoneNumber).find();
	}
	
	
	public static boolean ibanControl(String iban) {
		return Pattern.compile("^((TR\\d{24})|())$", Pattern.CASE_INSENSITIVE).matcher(iban).find();
	}
	public static boolean ibanControl(String iban, String regex) {
		return Pattern.compile(regex).matcher(iban).find();
	}
	public static boolean ibanControl(String iban, Pattern pattern) {
		return pattern.matcher(iban).find();
	}
	
	
	public static boolean decimalControl(String ...args) {
		boolean rValue = true;
		for(String arg : args) {
			rValue &= Pattern.compile("^((\\d+(\\.\\d{1,2})?)|())$").matcher(arg).find();
		}
		return rValue;
	}
	public static boolean decimalControl(Pattern pattern, String ...args) {
		boolean rValue = true;
		for(String arg : args) {
			rValue &= pattern.matcher(arg).find();
		}
		return rValue;
	}
	
}
