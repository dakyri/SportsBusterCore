package com.sb;

import java.util.Date;
import java.util.GregorianCalendar;

public class SBTime {
	public static final String months[] =
			{"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
	public static final String weekdays[] =
			{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

	public SBTime()
	{
	}

	public String toString()
	{
		return "";
	}

	public static Date timeStr2date(String timeStr)
	{
//		trace(timeStr, "date");
		if (timeStr == null || timeStr.equals("")){
			return new Date();
		}
		String tstrSplit[] = timeStr.split(" ");
		String dSplit[] = tstrSplit[0].split("-");
		String hSplit[] = tstrSplit[1].split(":");
		GregorianCalendar date = new GregorianCalendar(
				Integer.parseInt(dSplit[0]),
				(Integer.parseInt(dSplit[1]) - 1),
				Integer.parseInt(dSplit[2]),
				Integer.parseInt(hSplit[0]),
				Integer.parseInt(hSplit[1]), 0);
//		trace(date.getTime(), "got time in dateTimeMillis");
		return date.getTime();
	}
	
	public static long dateTimeMillis(String timeStr)
	{
		return timeStr2date(timeStr).getTime();
	}

	public static String ordinalize(int day) 
	{
		if (day == 11){
			return "11th";
		}
		if (day == 12){
			return "12th";
		}
		if (day == 13){
			return "13th";
		}
		if (day % 10 == 1){
			return Integer.toString(day) + "st";
		}
		if (day % 10 == 2){
			return Integer.toString(day) + "nd";
		}
		if (day % 10 == 3){
			return Integer.toString(day) + "rd";
		}
		return Integer.toString(day) + "th";
	}

}
