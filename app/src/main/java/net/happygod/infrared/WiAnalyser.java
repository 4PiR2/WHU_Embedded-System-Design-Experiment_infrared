package net.happygod.infrared;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.util.*;

public class WiAnalyser extends Activity
{
	ConsumerIrManager mCIR;
	/**
	 * Initialization of the Activity after it is first created.  Must at least
	 * call {@link Activity#setContentView setContentView()} to
	 * describe what is to be displayed in the screen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);
		// Get a reference to the ConsumerIrManager
		mCIR=(ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
		setContentView(R.layout.activity_wianalyser);
		findViewById(R.id.button1).setOnClickListener(button1Listener);
		findViewById(R.id.button2).setOnClickListener(button2Listener);
		findViewById(R.id.button3).setOnClickListener(button3Listener);
	}
	View.OnClickListener button1Listener=new View.OnClickListener()
	{
		public void onClick(View v)
		{
			NECtransmit(38000,0,104,2);
		}
	};
	View.OnClickListener button2Listener=new View.OnClickListener()
	{
		public void onClick(View v)
		{
			NECtransmit(38000,0,152,2);
		}
	};
	View.OnClickListener button3Listener=new View.OnClickListener()
	{
		public void onClick(View v)
		{
			NECtransmit(38000,0,176,2);
		}
	};
	void NECtransmit(int freq,int addr,int cmd,int rept)
	{
		final int total=110000, span=560, start1=9000, start2=4500, zero=560, one=1680, repeat1=9000, repeat2=2250, repeat3=total-repeat1-repeat2-span;
		int end=total,pattern[];
		List<Integer> p=new LinkedList<>();
		p.add(start1);
		p.add(start2);
		for(int i=7;i >= 0;i--)
		{
			p.add(span);
			p.add((addr >> i&1)==1?one:zero);
		}
		for(int i=7;i >= 0;i--)
		{
			p.add(span);
			p.add((addr >> i&1)==0?one:zero);
		}
		for(int i=7;i >= 0;i--)
		{
			p.add(span);
			p.add((cmd >> i&1)==1?one:zero);
		}
		for(int i=7;i >= 0;i--)
		{
			p.add(span);
			p.add((cmd >> i&1)==0?one:zero);
		}
		p.add(span);
		for(int i : p)
			end-=i;
		p.add(end);
		while(rept-->0)
		{
			p.add(repeat1);
			p.add(repeat2);
			p.add(span);
			p.add(repeat3);
		}
		pattern=new int[p.size()];
		for(int i=0;i<p.size();i++)
			pattern[i]=p.get(i);
		// A pattern of alternating series of carrier on and off periods measured in microseconds.
		// transmit the pattern at 38.4KHz
		mCIR.transmit(freq,pattern);
	}
}
