package com.teamparkin.mtdapp.restadapters;

public class RESTJSONAdapterError {
	private String errorMessage;
	
	public RESTJSONAdapterError(Exception e){
		errorMessage = e.getMessage();
	}
	
	public RESTJSONAdapterError(String s){
		errorMessage = s;
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}
}
