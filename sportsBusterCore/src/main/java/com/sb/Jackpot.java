package com.sb;

public class Jackpot
{
	public String currency;
	public String lastWon;
	public float amount;
	public int id;
	public String name;
	public Boolean processed;

	/**
	 * 
	 * @param _name
	 * @param _id
	 * @param _amount
	 * @param _currency
	 * @param _processed
	 * @param _lastWon
	 */
	public Jackpot(
			String _name, int _id,
			float _amount, String _currency, Boolean _processed,
			String _lastWon)
	{
		name = _name;
		amount = _amount;
		currency = _currency;
		id = _id;
		processed = _processed;
		lastWon = _lastWon;
	}
}
