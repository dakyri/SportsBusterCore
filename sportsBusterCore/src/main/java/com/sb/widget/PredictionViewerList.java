/**
 * 
 */
package com.sb.widget;

import java.util.ArrayList;

import com.sb.DataFeed;
import com.sb.FeedEntry;
import com.sb.MatchTime;
import com.sb.Prediction;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @author dak
 *
 */
public class PredictionViewerList extends ListView implements PredictionViewer
{

	/**
	 * @param context
	 */
	public PredictionViewerList(Context context)
	{
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public PredictionViewerList(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public PredictionViewerList(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public void clear()
	{
		
	}

	@Override
	public void addPredictions(ArrayList<Prediction> feed)
	{
		for (Prediction f: feed) {
			addItem(f, -1);
		}
	}

	@Override
	public void addItem(Prediction f, int pos)
	{
	}

	@Override
	public void setPredictions(ArrayList<Prediction> feed)
	{
		clear();
		addPredictions(feed);
	}

}
