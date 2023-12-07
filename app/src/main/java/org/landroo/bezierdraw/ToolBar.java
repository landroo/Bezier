package org.landroo.bezierdraw;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

public class ToolBar
{
	private static final String TAG = "ToolBarClass";
	
	public SlidingDrawer topView;
	public Rect topViewRect;

	public SlidingDrawer leftView;
	public Rect leftViewRect;
	
	private Context context;
	private Handler handler;
	
	private int displayWidth;
	private int displayHeight;

	
	// tool gallery
	public Gallery gallery = null;
	private imageListAdapter imageAdapter;
	private ArrayList<Bitmap> mBitmaps = new ArrayList<Bitmap>();
	
	public ToolBar(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;

		topView = (SlidingDrawer)getTopView(inflater);
		leftView = (SlidingDrawer)getLeftView(inflater);

	}

	// setup tool bar
	public void initGallery()
	{
		imageAdapter = new imageListAdapter(context);

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.deselect);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.move);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rotate);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.zoom);
		mBitmaps.add(bitmap);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.scale);
		mBitmaps.add(bitmap);

		imageAdapter.notifyDataSetChanged();

		gallery = (Gallery) topView.findViewById(R.id.gallery);
		gallery.setAdapter(imageAdapter);
		gallery.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				topView.animateClose();
				switch (arg2)
				{
				case 0:// deselect
					break;
				case 1:// move
					break;
				case 2:// rotate
					break;
				case 3:// zoom
					break;
				case 4:// scale
					break;
				}
			}
		});
	}
	
	// image list adapter for image gallery
	public class imageListAdapter extends BaseAdapter
	{
		private Context context;

		public imageListAdapter(Context c)
		{
			context = c;
		}

		public int getCount()
		{
			return mBitmaps.size();
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (mBitmaps == null) return null;

			ImageView iv = new ImageView(context);
			iv.setBackgroundResource(R.drawable.btn_states);

			try
			{
				Bitmap bitmap = mBitmaps.get(position);
				iv.setImageBitmap(bitmap);
			}
			catch (OutOfMemoryError e)
			{
				Log.e(TAG, "Out of memory error in imageListAdapter!");
				System.gc();
			}
			catch (Exception ex)
			{
				Log.e(TAG, ex.toString());
			}

			iv.setAdjustViewBounds(true);
			iv.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			return iv;
		}
	}

	// top drawer
	public View getTopView(LayoutInflater inflater)
	{
		int w = displayWidth * 3 / 5;
		int h = displayHeight / 5;
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.top_view, null);
		topView = (SlidingDrawer) view;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
		topView.setLayoutParams(params);
		topView.setTranslationX(displayWidth / 5);

		Bitmap bitmap = drawBack(w, h, 0, w / 10, true, w / 10, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);

		LinearLayout ll = (LinearLayout) view.getChildAt(1);
		ll.setBackgroundDrawable(drawable);

		topViewRect = new Rect(0, 0, w, h);

		return topView;
	}
	
	// left drawer
	public View getLeftView(LayoutInflater inflater)
	{
		int w = displayWidth * 2 / 5;
		int h = displayHeight * 6 / 8;
		if(displayWidth > displayHeight) w = displayWidth / 4;  
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.left_view, null);
		leftView = (SlidingDrawer) view;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
		leftView.setLayoutParams(params);
		leftView.setTranslationY(displayHeight / 6);

		Bitmap bitmap = drawBack(w, h, w / 10, 0, false, h / 20, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);

		LinearLayout ll = (LinearLayout) view.getChildAt(1);
		ll.setBackgroundDrawable(drawable);

		leftViewRect = new Rect(0, 0, w, h);

		return leftView;
	}
	
	// draw drawer background
	public static Bitmap drawBack(int w, int h, int xOff, int yOff, boolean gr, int rad, boolean border, Context context)
	{
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);

		int BLACK = 0xAA303437;
		int WHITE = 0xAAC5C6C7;

		int[] colors = new int[3];
		colors[0] = BLACK;
		colors[1] = WHITE;
		colors[2] = BLACK;

		LinearGradient gradient;
		if (gr) gradient = new LinearGradient(0, 0, w, 0, colors, null, android.graphics.Shader.TileMode.CLAMP);
		else gradient = new LinearGradient(0, 0, 0, h, colors, null, android.graphics.Shader.TileMode.CLAMP);
		paint.setShader(gradient);

		RectF rect = new RectF();
		rect.left = -xOff;
		rect.top = -yOff;
		rect.right = w;
		rect.bottom = h;
		float rx = dipToPixels(context, 20);
		float ry = dipToPixels(context, 20);

		canvas.drawRoundRect(rect, rx, ry, paint);

		if (border)
		{
			paint.setStyle(Paint.Style.STROKE);
			paint.setShader(null);
			paint.setColor(0xff000000);
			paint.setStrokeWidth(dipToPixels(context, 3));
			canvas.drawRoundRect(rect, rx, rx, paint);
		}

		return bitmap;
	}
	
	// calculate pixel size
	public static float dipToPixels(Context context, float dipValue)
	{
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}


}
