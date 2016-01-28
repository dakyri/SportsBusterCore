package com.sb;

import java.util.ArrayList;

public class DataFeed extends ArrayList<FeedEntry>
{
	private static final long serialVersionUID = 1L;

	public DataFeed(FeedEntry... args)
	{
		for (FeedEntry fe:  args)	{
			push(fe);
		}
		return;
	}
	
	public void push(FeedEntry e)
	{
		add(e);
	}
	
	public String toString()
	{
		return "DataFeed["+Integer.toString(size())+" items]";
	}

}
