package net.happygod.infrared;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.hardware.ConsumerIrManager;
import android.view.View;
import android.widget.*;
import android.util.Log;

import java.util.*;

public class MainActivity extends Activity
{
	private static final String TAG="ConsumerIrTest";
	TextView mFreqsText;
	ConsumerIrManager mCIR;
	/**
	 * Initialization of the Activity after it is first created.  Must at least
	 * call {@link android.app.Activity#setContentView setContentView()} to
	 * describe what is to be displayed in the screen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);
		// Get a reference to the ConsumerIrManager
		mCIR=(ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
		// See assets/res/any/layout/consumer_ir.xml for this
		// view layout definition, which is being set here as
		// the content of our screen.
		setContentView(R.layout.activity_main);
		// Set the OnClickListener for the button so we see when it's pressed.
		findViewById(R.id.send_button).setOnClickListener(mSendClickListener);
		findViewById(R.id.get_freqs_button).setOnClickListener(mGetFreqsClickListener);
		mFreqsText=(TextView)findViewById(R.id.freqs_text);
	}
	View.OnClickListener mSendClickListener=new View.OnClickListener()
	{
		public void onClick(View v)
		{
			if(!mCIR.hasIrEmitter())
			{
				Log.e(TAG,"No IR Emitter found\n");
				return;
			}
			int
			addr=Integer.parseInt(((EditText)findViewById(R.id.addr)).getText().toString()),
			cmd=Integer.parseInt(((EditText)findViewById(R.id.cmd)).getText().toString()),
			rept=Integer.parseInt(((EditText)findViewById(R.id.rept)).getText().toString());
			NECtransmit(38000,addr,cmd,rept);
		}
	};
	View.OnClickListener mGetFreqsClickListener=new View.OnClickListener()
	{
		public void onClick(View v)
		{
			StringBuilder b=new StringBuilder();
			if(!mCIR.hasIrEmitter())
			{
				mFreqsText.setText("No IR Emitter found!");
				Log.e(TAG,"No IR Emitter found!\n");
				return;
			}
			// Get the available carrier frequency ranges
			ConsumerIrManager.CarrierFrequencyRange[] freqs=mCIR.getCarrierFrequencies();
			b.append("IR Carrier Frequencies:\n");
			for(ConsumerIrManager.CarrierFrequencyRange range : freqs)
			{
				b.append(String.format("    %d - %d\n",range.getMinFrequency(),range.getMaxFrequency()));
			}
			mFreqsText.setText(b.toString());
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
