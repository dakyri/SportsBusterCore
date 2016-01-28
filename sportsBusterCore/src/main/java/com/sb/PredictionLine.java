package com.sb;

import java.util.Date;

public class PredictionLine
{
	public String lineId;
	public Date betDate;
	public String state;
	
	public PredictionLine(String ids, Date pdt, String s)
	{
		lineId = ids;
		betDate = pdt;
		state = s;
	}
}
