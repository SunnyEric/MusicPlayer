package com.yuer.MusicPlayer.lrc;

public class Jiexilrc {

	private String[] lyric;//ȫ�����
	
	public Jiexilrc(String[] lyr)
	{
		lyric = lyr;
	}
	
	public int getLine(int jindu,int qline)
	{
		int index=0;//��λ��ʵ�����
		for(index=qline;index<lyric.length;index++)
		{
			if(lyric[index].length()>=15)  //һ������Ҫ15���Ȳ��Ǹ��
			{
				//System.out.println(s);
				int i = lyric[index].indexOf(",");  //�ҵ����ǰ���[100,200]�е�','
				if(i==-1)
				{
					continue;
				}
				String j = lyric[index].substring(1, i);
				//System.out.println(j);
				if(jindu<Integer.parseInt(j)) break;
			}
		}
		return index;
	}
	
	public String fortmat(String lrc)
	{
		int i=0,t=0;
		String s="";
		for(i=0;i<lrc.length();i++)
		{
			if(lrc.charAt(i)=='>')
			{
				t=1;
				continue;
			}
			if(lrc.charAt(i)=='<')
			{
				t=0;
				continue;
			}
			if(t==1) s = s+lrc.charAt(i);
			//System.out.println((int)lrc.charAt(i));
		}
		return s;
	}
}