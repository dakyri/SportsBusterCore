/**
 * 
 */
package com.sb.widget;

import com.sb.DataFeed;
import com.sb.FeedEntry;
import com.sb.MatchTime;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @author dak
 *
 */
public class EventViewerList extends ListView implements EventViewer
{

	/**
	 * @param context
	 */
	public EventViewerList(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public EventViewerList(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public EventViewerList(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void clear()
	{
		
	}
	
	public void addItem(FeedEntry f, int pos)
	{
		
	}

	public void addFeed(DataFeed feed)
	{
		for (FeedEntry f: feed) {
			addItem(f, -1);
		}
	}
	
	public void setFeed(DataFeed feed)
	{
		clear();
		addFeed(feed);
	}

}
