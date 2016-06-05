package com.yuer.MusicPlayer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import com.yuer.MusicPlayer.R;
import com.yuer.MusicPlayer.file.MyComprator;
import com.yuer.MusicPlayer.file.MyFileFilter;
import com.yuer.MusicPlayer.lrc.Jiexilrc;
import com.yuer.MusicPlayer.lrc.KrcText;
import com.yuer.MusicPlayer.lrc.LrcService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MusicPlayer extends Activity
implements OnClickListener,OnSeekBarChangeListener{

	private MediaPlayer player;//ý�岥�Ŷ���
	private File[] musics; //�����ļ�
	private int num=0;//��������
	private int index = 0;//�����±�
	private int total;//������ʱ��
	public static int ind = 0; //����ĸ���
	private TextView musicName;	//��������ʾ�ؼ�
	private ImageButton btnPlay;//�������ź���ͣ��ť�ؼ�
	public static boolean press = false;//�Ƿ��������б�
	private boolean play=false; //�Ƿ��ڲ���
	private SeekBar seekBar;//������
	private TextView jindu,zong;//ʱ��
	private ImageButton next,pre;//��һ������һ��
	private int mode=1;//�����л�ģʽ
	private ImageButton btnMode;//ģʽ��ť
	private int nowpro=0;//��Ϊ��������Ľ���
	private boolean proman=false;//�Ƿ���Ϊ����
	private boolean fugai=false;//�Ƿ��Ǳ������б���
	private SharedPreferences sharedPre;//�����xml�ļ�
	private TextView lrc1; //���ǰ
	private TextView lrc2; //���ǰ
	private TextView lrc3; //�����
	private TextView lrc4; //��ʺ�
	private TextView lrc5; //��ʺ�
	private String[] lyric;//ȫ�����
	private int lrcline = 0;//���������
	private Jiexilrc jiexi;  //�������
	private boolean showlrc=true; //�Ƿ��и����ʾ
	private long preTime = 0;
	private long secondTime;
	private Intent serviceIntent;  //�����ʷ���
	private boolean showwinlrc;  //�Ƿ���ʾ������
	private boolean sett=false;  //�Ƿ����������
	
	private void initUI(){
		musicName = (TextView)findViewById(R.id.music_name);
		btnPlay = (ImageButton)findViewById(R.id.btn_play);
		seekBar = (SeekBar)findViewById(R.id.seekBar);
		jindu = (TextView)findViewById(R.id.jindu);
		zong = (TextView)findViewById(R.id.zong);
		next = (ImageButton)findViewById(R.id.btn_next);
		pre = (ImageButton)findViewById(R.id.btn_pre);
		lrc1 = (TextView)findViewById(R.id.lrc1);
		lrc2 = (TextView)findViewById(R.id.lrc2);
		lrc3 = (TextView)findViewById(R.id.lrc3);
		lrc4 = (TextView)findViewById(R.id.lrc4);
		lrc5 = (TextView)findViewById(R.id.lrc5);
		btnMode = (ImageButton)findViewById(R.id.btn_mode);
		setmode();
	}
	
	private void setmode()
	{   //���ز���ģʽ
		sharedPre = getSharedPreferences("fodname", PreferenceActivity.MODE_WORLD_WRITEABLE);
		int n = sharedPre.getInt("mode", 0);
		if(n==0)
		{
			Editor editor = sharedPre.edit();
			editor.putInt("mode", 1);
			editor.commit();
		}
		else mode=n;
		if(mode==1) btnMode.setImageResource(R.drawable.shunxu_dark);
		if(mode==2) btnMode.setImageResource(R.drawable.shuffle_dark);
		if(mode==3) btnMode.setImageResource(R.drawable.repeat_dark);
	}
	
	public void setshowwinlrc()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		showwinlrc = prefs.getBoolean("showwinlrc", false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playmusic);
		
		registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		//ʹ�ù㲥�����߼���home��
		
		initUI();
		setshowwinlrc();
		filesIni();
		readFiles();
		
		//��������������
		player = new MediaPlayer();
		if(num>0) jiazai();
		
		//Ϊ��ť���õ���¼�
		btnPlay.setOnClickListener(this);
		next.setOnClickListener(this);
		pre.setOnClickListener(this);
		seekBar.setOnSeekBarChangeListener(this);
		btnMode.setOnClickListener(this);
		
		//Ϊ���������ò�������¼�
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0)
			{
				jiazai();
				player.start();
				
				//int now = player.getCurrentPosition(); //��ȡ������ǰ����(ms)
				//int total = player.getDuration();		//��ȡ�������ܲ���ʱ��(ms)
				//player.seekTo(88888)					//�������������õ�ָ��ʱ��λ��
			}
		});
		
		serviceIntent = new Intent(this,LrcService.class);
		startService(serviceIntent);  //���������ʷ���Ĭ�ϲ���ʾ
	}
	
	public void filesIni()
	{
		int n = sharedPre.getInt("fodnum", 0);
		if(n==0)
		{
			Editor editor = sharedPre.edit();
			editor.putInt("fodnum", 0);
			editor.commit();
		}
	}
	
	public void readFiles()
	{
		String[] name = new String[100];
		int n = sharedPre.getInt("fodnum", 0);
		for(int i=0;i<n;i++)
		{
			name[i] = sharedPre.getString("name"+i, "");
		}
		int k = 0;
		for(int i=0;i<n;i++)
		{
			File f = new File(name[i]);
			File[] music = f.listFiles(new MyFileFilter());
			k = k+music.length;
		}
		musics = new File[k];
		k = 0;
		for(int i=0;i<n;i++)
		{
			File f = new File(name[i]);
			File[] music = f.listFiles(new MyFileFilter());
			for(int j=0;j<music.length;j++)
			{
				musics[k] = music[j];
				k++;
			}
		}
		Arrays.sort(musics,new MyComprator());
		num = musics.length;
		/*
		for(int i=0;i<num;i++)
		{
			System.out.println(musics[i].getName());
		}*/
	}
	
	public void initial()
	{
		player.reset();
		h.removeCallbacks(r);
		btnPlay.setImageResource(R.drawable.play_dark);
		play=false;
		jindu.setText(getTime(0));
		zong.setText(getTime(0));
		seekBar.setProgress(0);
		seekBar.setMax(1000);
		getlrc("");
		musicName.setText("������");
		index=0;
	}
	
	public void jiazai()
	{
		try {
			player.reset();
			//System.out.println(index);
			//���ò���Դ
			player.setDataSource(musics[index].getAbsolutePath());
			//���ز���Դ
			player.prepare();
			
			musicName.setText(musics[index].getName().replaceAll(".mp3", ""));
			total = player.getDuration();   //��ȡ������ʱ��
			seekBar.setMax(total);
			zong.setText(getTime(total));
			getlrc(musics[index].getName().replaceAll(".mp3", ".krc"));
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getlrc(String name)
	{
		KrcText lrc = new KrcText(name);
		lrc1.setText("");
		lrc2.setText("");
		lrc4.setText("");
		lrc5.setText("");
		try {
			String l = lrc.getLrc();
			lyric = l.split("\n");
			lrc3.setText("");
			showlrc=true;
			jiexi = new Jiexilrc(lyric);
			setlrc();
		} catch (IOException e) {
			lrc3.setText("�޸��");
			showlrc=false;
			setwinlrc(lrc4.getText().toString());
		}
		lrcline = 0;
	}
	
	public void setlrc()
	{
		if(!showlrc) return;
		int line = jiexi.getLine(player.getCurrentPosition(),lrcline);
		if(line!=lrcline)
		{
			if(line>=3) lrc1.setText(jiexi.fortmat(lyric[line-3]));
			else lrc1.setText("");
			if(line>=2) lrc2.setText(jiexi.fortmat(lyric[line-2]));
			else lrc2.setText("");
			if(line>=1) lrc3.setText(jiexi.fortmat(lyric[line-1]));
			else lrc3.setText("");
			if(line<lyric.length) lrc4.setText(jiexi.fortmat(lyric[line]));
			else lrc4.setText("");
			setwinlrc(lrc4.getText().toString());
			if(line<lyric.length-1) lrc5.setText(jiexi.fortmat(lyric[line+1]));
			else lrc5.setText("");
			lrcline = line;
		}
	}
	
	public void setwinlrc(String lrc)
	{  //������
		LrcService.newlrc(lrc);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_play:
			//��ʼ����
			play = !play;
			if(play)
			{
				if(num==0)
				{
					show("���������");
					play = !play;
					break;
				}
				player.start();
				h.post(r);
				//show("��ʼ����");
				btnPlay.setImageResource(R.drawable.pause_dark);
			}
			else
			{
				player.pause();
				h.removeCallbacks(r);
				//show("��ͣ");
				btnPlay.setImageResource(R.drawable.play_dark);
			}
			break;
		case R.id.btn_pre:
			if(num==0)
			{
				show("���������");
				break;
			}
			if(mode==1)
			{
				index = index-2;
			}
		case R.id.btn_next:
			if(num==0)
			{
				show("���������");
				break;
			}
			if(!play)
			{
				player.start();
				h.post(r);
				btnPlay.setImageResource(R.drawable.pause_dark);
				play = !play;
			}
			changeMusic();
			break;
		case R.id.btn_mode:
			if(mode==1)
			{
				mode = 2;
				btnMode.setImageResource(R.drawable.shuffle_dark);
				//show("�������");
				break;
			}
			if(mode==2)
			{
				mode = 3;
				btnMode.setImageResource(R.drawable.repeat_dark);
				//show("����ѭ��");
				break;
			}
			if(mode==3)
			{
				mode = 1;
				btnMode.setImageResource(R.drawable.shunxu_dark);
				//show("˳�򲥷�");
				break;
			}
		}
		
	}
	
	public void show(CharSequence s)
	{
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	
	Handler h = new Handler();
	Runnable r = new Runnable() {
		
		@Override
		public void run() {
			h.postDelayed(r, 100);  //���100ms�ٴ�ִ��
			if(!proman) seekBar.setProgress(player.getCurrentPosition());
			setlrc();
			if(seekBar.getProgress()+300>total && seekBar.getProgress()<total)
			{  //�����Զ��л�����
				changeMusic();
			}
		}
	};

	@Override
	public void onProgressChanged(SeekBar arg0, int now, boolean isFromUser) {
		if(isFromUser)
		{
			if(total-now<1000) now = total-1000;
			nowpro = now;
			proman = true;
			//System.out.println(now);
		}
		if(num==0) now = 0;
		jindu.setText(getTime(now));
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		if(num!=0)
		{
			player.seekTo(nowpro);
			lrcline = 0;
			setlrc();
		}
		proman = false;
	}
	
	public String getTime(int time)
	{
		String text="";
		time = time/1000;
		int fen = time/60;
		int miao = time%60;
		if(fen<10)
		{
			text = text + "0";
		}
		text = text + fen;
		text = text + ":";
		if(miao<10)
		{
			text = text + "0";
		}
		text = text + miao;
		return text;
	}
	
	public void changeMusic()
	{
		//ģʽһ  ˳������һ��
		//ģʽ��  �����һ��
		//ģʽ��  ѭ������
		player.seekTo(total);  //���ý���Ϊ��󣬻ᴥ����������¼�
		seekBar.setProgress(0);
		if(mode==1)
		{
			index++;
			if(index>=num)
			{
				index = 0;
			}
			if(index<0)
			{
				index = num-1;
			}
		}
		if(mode==2)
		{
			Random r = new Random();
			index=r.nextInt(num);
		}
		//show("�����л�");
	}
	
	public void lists(View v)
    {//��ת����һ��Activity
		fugai = true;
    	Intent i = new Intent(this,MusicsList.class);
    	//����
    	startActivity(i);
    }

    @Override
	protected void onRestart() {
    	if(fugai)  //�Ӹ����б�ص�������
    	{
	    	readFiles();
	    	if(press)  //����˸���
	    	{
				index = ind;
				if(!play)
				{
					btnPlay.setImageResource(R.drawable.pause_dark);
					jiazai();
					player.start();
					h.post(r);
					play = !play;
				}
				else player.seekTo(total);
				seekBar.setProgress(0);
				press = false;
	    	}
	    	else
	    	{
	    		if(num==0) initial();
	    		if(musicName.getText().equals("������") && num>0) jiazai();
	    	}
	    	fugai = false;
    	}
    	else
    	{
    		if(play)  //��绰����֮���Զ���������
    		{
    			player.start();
				h.post(r);
				btnPlay.setImageResource(R.drawable.pause_dark);
    		}
    	}
    	LrcService.lrc1.setVisibility(View.INVISIBLE);
		LrcService.lrc2.setVisibility(View.INVISIBLE);
    	if(sett)
    	{  //����showwinlrc
    		setshowwinlrc();
    		sett = false;
    	}
    	super.onRestart();
	}
    
    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// �������ذ�����������ʱ��������¼�
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			secondTime = System.currentTimeMillis();  //��ȡ�ڶ��β����˳�ʱ��
			if(secondTime - preTime > 2000)
			{
				show("�ٰ�һ���˳�");
				preTime = secondTime;
			}
			else
			{
				h.removeCallbacks(r);
				player.release();
				savemode();
				stopService(serviceIntent);
				System.exit(0);
				return super.onKeyUp(keyCode, event);
			}
		}
		return false;
	}
    
    public void savemode()
	{
		Editor editor = sharedPre.edit();
		editor.putInt("mode", mode);
		editor.commit();
	}
    
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                     //��ʾ����home��,�����˺�̨
                	if(showwinlrc)
        			{
        				LrcService.lrc1.setVisibility(View.VISIBLE);
        				LrcService.lrc2.setVisibility(View.VISIBLE);
        			}
                }else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){
                    //��ʾ����home��,��ʾ���ʹ�õĳ����б�
                }
            }
        }
    };
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.action_settings:
			sett = true;
			Intent i = new Intent(this,Setting.class);
	    	startActivity(i);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
    
}
