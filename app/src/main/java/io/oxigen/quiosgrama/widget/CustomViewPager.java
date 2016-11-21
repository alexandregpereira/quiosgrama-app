package io.oxigen.quiosgrama.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager{
	
	private boolean enabled = true;

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
    	if(enabled)
    		return super.onInterceptTouchEvent(ev);
    	
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
    	if(enabled)
    		return super.onTouchEvent(ev);
    	
        return false;
    }
    
    public void setEnabled(boolean enabled){
    	this.enabled = enabled;
    }
}
