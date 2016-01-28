package com.sb;

import java.util.Date;

public class MatchTime extends SBTime
{
	public int sec = 0;
	public int min = 0;
	public int half = 0;
	
	public MatchTime(int h, int m, int s)
	{
		setTo(h, m, s);
	}
	
	public MatchTime()
	{
		this(0,0,0);
		return;
	}
	
	public MatchTime(MatchTime t)
	{
		setTo(t);
		return;
	}
	
	public MatchTime(int []args)
	{
		setTo(args);
		return;
	}
	
	public MatchTime(int h, int m)
	{
		this(h,m,0);
	}
	
	public String toShortString()
	{
		return Integer.toString(half) + ":" + Integer.toString(min) + ":" + Integer.toString(sec);
	}
	
	public void setTo(int h, int m, int s)
	{
		sec = s;
		min = m;
		half = h;
	}
	
	public void setTo(MatchTime t)
	{
		half = t.half;
		min = t.min;
		sec = t.sec;
	}
	
	public void setTo(int [] pa)
	{
		half = 0;
		min = 0;
		sec = 0;
		if (pa == null || pa.length == 0){
			return;
		} else {
			half = pa[0];
			if (pa.length > 1){
				min = pa[1];
				if (pa.length > 2){
					sec = pa[2];
				}
			}
		}
		return;
	}
	
	public Boolean inAddedTime()
	{
		return (min >= 45 && half <= 2) || (min >= 15 && half >= 3);
	}
	
	public String toString()
	{
		String secstr = Integer.toString(sec);
		if (sec < 10){
			secstr = "0" + secstr;
		}
		secstr = ":" + secstr;
		if (half <= 1){
			if (min > 45){
				return "45+" + Integer.toString(min - 45) + secstr;
			}
			return Integer.toString(min) + secstr;
		} else if (half == 2){
			if (min > 45){
				return "90+" + Integer.toString(min - 45) + secstr;
			}
			return Integer.toString(min + 45) + secstr;
		} else if (half == 3){
			if (min > 15){
				return "105+" + Integer.toString(min - 15) + secstr;
			}
			return Integer.toString(min + 90) + secstr;
		} else if (half == 4){
			if (min > 15){
				return "120+" + Integer.toString(min - 15) + secstr;
			}
			return Integer.toString(min + 105) + secstr;
		}
		return toShortString();
	}
	
	public Boolean gt(MatchTime t)
	{
		return t != null && (half > t.half || t.half == half && (t.min < min || t.min == min && t.sec < sec));
	}
	
	public String toMinString() 
	{
		String tstr = Integer.toString(min);
		if (min < 10){
			tstr = "0" + tstr;
		}
		return Integer.toString(half) + ":" + tstr;
	}
	
	public void incS(int secs)
	{
		sec = sec + secs;
		min = min + sec / 60;
		sec = sec % 60;
		return;
	}
	
	public MatchTime plusS(int secs)
	{
		MatchTime tn = new MatchTime(this);
		tn.incS(secs);
		return tn;
	}
	
	public Boolean ge(MatchTime t)
	{
		return t != null && (half > t.half || t.half == half && (t.min <= min || t.min == min && t.sec <= sec));
	}
	
	public Boolean equals(MatchTime t)
	{
		return t != null && t.half == half && t.min == min && t.sec == sec;
	}
	
	public static String timeFormat(Date d) 
	{
		String minstr = (d.getMinutes()<10?"0":"")+Integer.toString(d.getMinutes());
		return d.getHours()+":"+minstr+" "+Integer.toString(d.getDate())+"/"+Integer.toString(d.getMonth()+1);
	}
	
	public static String dateFormat(Date d) 
	{
		return Integer.toString(d.getDate())+"/"
				+Integer.toString(d.getMonth()+1)+"/"
				+Integer.toString(1900+d.getYear());
	}
	
	public static String halfName(int hf) 
	{
		if (hf <= 1){
			return "1st half";
		}
		if (hf == 2){
			return "2nd half";
		}
		if (hf == 3){
			return "xtra time 1";
		}
		return "xtra time 2";
	}

}
