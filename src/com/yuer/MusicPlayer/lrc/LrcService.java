package com.yuer.MusicPlayer.lrc;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
 
public class LrcService extends Service
{
	public static WindowManager winm;
	public static TextView lrc1;
	public static TextView lrc2;
	public static WindowManager.LayoutParams params1;
	public static WindowManager.LayoutParams params2;
	private int statusBarHeight; //����״̬���ĸ߶�
	private float startX;  //�����ʼλ��
    private float startY;
    public static String lrcnext;  //�´β��Ÿ��
    private static int line=1;  //���ڲ��ŵ�����
    private int realL;  //���䲻ͬ��С���͵ĸ�ʼ��
 
    public LrcService() {
    }
 
    @Override
    public void onCreate() {
        super.onCreate();
        winm = (WindowManager) getApplicationContext().getSystemService(
                WINDOW_SERVICE);
        statusBarHeight = getStatusBarHeight();
        realL = winm.getDefaultDisplay().getHeight()/30;
        //System.out.println(statusBarHeight);
        showWindow();
    }
    
    public WindowManager.LayoutParams getParams()
    {
    	WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    	params.type = LayoutParams.TYPE_SYSTEM_ALERT
                | LayoutParams.TYPE_SYSTEM_OVERLAY;// ���ô�������Ϊϵͳ��
    	params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;// ���ô��ڽ���
 
    	params.width = WindowManager.LayoutParams.FILL_PARENT;
    	params.height = WindowManager.LayoutParams.WRAP_CONTENT;
    	params.alpha = 80;
 
    	params.gravity = Gravity.LEFT | Gravity.TOP;
        // ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
    	params.x = 0;
    	params.format = PixelFormat.RGBA_8888;  //���ñ���Ϊ͸��
    	return params;
    }
 
    // ��ʾ��������
    public void showWindow() {
    	params1 = getParams();
    	params1.y = 0;
    	//winm.getDefaultDisplay().getHeight()��ȡ��Ļ�߶�
        
        lrc1 = new TextView(this);
        lrc1.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				float x = event.getRawX();  //��ǰ����λ��
		        float y = event.getRawY()-statusBarHeight;
		 
		        switch (event.getAction()) {
		        case MotionEvent.ACTION_DOWN:
		            startX = event.getX();
		            startY = event.getY();
		            break;
		        case MotionEvent.ACTION_MOVE:
		            //Log.w(TAG, "x::" + startX + ",y::" + startY);
		            //Log.w(TAG, "rawx::" + x + ",rawy::" + y);
		        case MotionEvent.ACTION_UP:
		            updatePosition(x - startX, y - startY);
		            break;
		        }
		        return true;
			}
		});
        lrc1.setTextSize(15);
        lrc1.setTextColor(Color.parseColor("#FFA100"));
        lrc1.setText("");
        TextPaint tp = lrc1.getPaint();
        tp.setFakeBoldText(true);
        lrc1.setVisibility(View.INVISIBLE);
		winm.addView(lrc1, params1);
		
		params2 = getParams();
		params2.y = realL;
		
		lrc2 = new TextView(this);
        lrc2.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				float x = event.getRawX();
		        float y = event.getRawY()-statusBarHeight-realL;
		 
		        switch (event.getAction()) {
		        case MotionEvent.ACTION_DOWN:
		            startX = event.getX();
		            startY = event.getY();
		            break;
		        case MotionEvent.ACTION_MOVE:
		            //Log.w(TAG, "x::" + startX + ",y::" + startY);
		            //Log.w(TAG, "rawx::" + x + ",rawy::" + y);
		        case MotionEvent.ACTION_UP:
		            updatePosition(x - startX, y - startY);
		            break;
		        }
		        return true;
			}
		});
        lrc2.setTextSize(15);
        lrc2.setTextColor(Color.WHITE);
        lrc2.setText(lrcnext);
        lrc2.setGravity(Gravity.RIGHT);
        TextPaint tp2 = lrc2.getPaint();
        tp2.setFakeBoldText(true);
        lrc2.setVisibility(View.INVISIBLE);
		winm.addView(lrc2, params2);
    }
    
    public static void newlrc(String lrc)
    {
    	lrcnext = lrc;
    	if(lrc2!=null&&line==1)
    	{
    		winm.removeView(lrc2);
    		lrc2.setText(lrc);
    		lrc2.setTextColor(Color.WHITE);
    		winm.addView(lrc2, params2);
    		winm.removeView(lrc1);
    		lrc1.setTextColor(Color.parseColor("#FFA100"));
    		winm.addView(lrc1, params1);
    		line = 2;
    		return;
    	}
    	if(lrc1!=null&&line==2)
    	{
    		winm.removeView(lrc1);
    		lrc1.setText(lrc);
    		lrc1.setTextColor(Color.WHITE);
    		winm.addView(lrc1, params1);
    		winm.removeView(lrc2);
    		lrc2.setTextColor(Color.parseColor("#FFA100"));
    		winm.addView(lrc2, params2);
    		line = 1;
    		return;
    	}
    }
    
    // ���¸�������λ�ò���
    private void updatePosition(float x, float y) {
        // View�ĵ�ǰλ��
        params1.x = (int) x;
        params1.y = (int) y;
        params2.x = (int) x;
        params2.y = (int) y + realL;
        winm.updateViewLayout(lrc1, params1);
        winm.updateViewLayout(lrc2, params2);
    }
    
    // ���״̬���߶�
    private int getStatusBarHeight() {
    	int result = 0;
	    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	    if (resourceId > 0) {
	      result = getResources().getDimensionPixelSize(resourceId);
	    }
	    return result;
    }
 
    // service�˳�ʱ�رո�������
    @Override
    public void onDestroy() {
        if (lrc1 != null) {
        	winm.removeView(lrc1);
        }
        if (lrc2 != null) {
        	winm.removeView(lrc2);
        }
        super.onDestroy();
    }
 
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
 
}