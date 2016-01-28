package com.sb;

import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

public abstract class Prediction
{
	public int pmId;
	public String ticketId;
	public float bet;
	public String currency;
	public Date timestamp;
	public String result;
	
	public ArrayList<PredictionLine> details=null;
	
	public class Result
	{
		public static final String WON = "W";
		public static final String LOST = "L";
		public static final String OPEN = "U";
	}
	
	public Prediction(int pmId, float _bet, String _currency)
	{
		this(pmId, null, _bet, _currency, null, Result.OPEN);
	}
	
	public Prediction(
			int _pmId, String _ticketId,
			float _bet, String _currency,
			Date _timestamp, String _result)
	{
		pmId = _pmId;
	    ticketId = ((_ticketId != null)?_ticketId:"");
	    bet = _bet;
	    currency = _currency;
	    timestamp = _timestamp;
	    result = _result;
	}
	
	public Prediction fromTicketInput(String selStr)
	{
		return fromTicketInput(0, "", (float) 0.0, "FRE", null, Result.OPEN, selStr);
	}
	
	public void addLine(PredictionLine l)
	{
		if (details == null) details = new ArrayList<PredictionLine>();
		details.add(l);
	}

	public abstract String toString();
	public abstract String ticketInputString();
	public abstract Prediction clone();
	public abstract Prediction fromTicketInput(
			int _pmId, String _ticketId,
			float _bet, String _currency,
			Date _timestamp, String _result, String selStr,
			Object... extras);
}
