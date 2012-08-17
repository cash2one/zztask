package com.zz91.task.board.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

public class DatetimeBindingInitializer implements WebBindingInitializer {

	final static String FORMAT="yyyy-MM-dd HH:mm:ss";
	
	public void initBinder(WebDataBinder webBinder, WebRequest request) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT);
		dateFormat.setLenient(true);
		webBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat,true));
		webBinder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
	}

}
