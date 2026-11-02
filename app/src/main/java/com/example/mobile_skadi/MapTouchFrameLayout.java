package com.example.mobile_skadi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/** Mantém os gestos do mapa dentro da página rolável. */
public final class MapTouchFrameLayout extends FrameLayout {
    public MapTouchFrameLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(
                event.getActionMasked() != MotionEvent.ACTION_UP && event.getActionMasked() != MotionEvent.ACTION_CANCEL);
        return super.dispatchTouchEvent(event);
    }
}
