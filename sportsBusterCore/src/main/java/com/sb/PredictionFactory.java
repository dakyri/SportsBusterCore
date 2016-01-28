package com.sb;

import java.util.Date;

public interface PredictionFactory
{
	Prediction prediction(
			int _pmId, String _ticketId,
			float _bet, String _currency,
			Date _timestamp, String _result, String selStr, Object... extras);
}
