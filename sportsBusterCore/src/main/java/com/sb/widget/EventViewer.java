package com.sb.widget;

import com.sb.DataFeed;
import com.sb.FeedEntry;

public interface EventViewer {
	public void clear();
	public void addItem(FeedEntry f, int pos);
	public void addFeed(DataFeed feed);
	public void setFeed(DataFeed feed);
}
