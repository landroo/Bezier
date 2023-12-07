package org.landroo.bezierdraw;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.landroo.ui.UI;
import org.landroo.ui.UIInterface;

import org.landroo.bezierdraw.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.AdapterView.OnItemClickListener;

public class BezierMainActivity extends Activity implements UIInterface
{
	private static final String TAG = "BezierMainActivity";
	private static final int SCROLL_SIZE = 10;
	private static final int SCROLL_ALPHA = 500;
	private static final int SCROLL_INTERVAL = 10;

	private int displayWidth;
	private int displayHeight;
	private int pictureWidth;
	private int pictureHeight;

	private List<PolyDraw> polyItemList = new ArrayList<PolyDraw>();// main item list
	private PolyDraw selItem;

	private BezierView bezierView;
	private UI ui = null;
	private ScaleView scaleView;

	private float sX = 0;
	private float sY = 0;
	private float mX = 0;
	private float mY = 0;

	private float zoomX = 1;
	private float zoomY = 1;

	private float xPos;
	private float yPos;

	private int round = 1;

	private float rotation;
	private float rx;
	private float ry;
	
	private int tileSize = 80;
	private Bitmap backBitmap;
	private Drawable backDrawable;// background bitmap drawable					
	private int backColor = Color.GRAY;// background color
	private String backFileName = "grid.png";// background bitmap name
	private boolean staticBack = false;// fix or scrollable background
	
	private ToolBar toolBar;
	
	private Paint scrollPaint = new Paint();
	private int scrollAlpha = SCROLL_ALPHA;
	
	private Paint infoPaint = new Paint();
	
	private float scrollX = 0;
	private float scrollY = 0;
	private float scrollMul = 1;
	private Timer scrollTimer = null;
	
	private float xDest;
	private float yDest;
	private int halfX;
	private int halfY;	
	private boolean scrollTo = false;
	
	private double startAng;
	private double startDist;
	
	// common handler
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			int typ = msg.what / 10;
			int sub = msg.what % 10;
			
			xPos = scaleView.xPos();
			yPos = scaleView.yPos();
			
			switch (typ)
			{
			case 1:// 
				break;
			}
		}
	};
	
	// main view
	private class BezierView extends ViewGroup
	{
		public BezierView(Context context)
		{
			super(context);
		}

		// draw items
		@Override
		protected void dispatchDraw(Canvas canvas)
		{
			drawBack(canvas);
			drawItems(canvas);
			drawScrollBars(canvas);
			drawInfo(canvas);
			
			super.dispatchDraw(canvas);
		}

		// set the sliding windows
		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b)
		{
			View child;
	    	// top slider bar
	    	child = this.getChildAt(0);
	    	child.layout(toolBar.topViewRect.left, toolBar.topViewRect.top, toolBar.topViewRect.right, toolBar.topViewRect.bottom);
	    	
	    	// left slider bar
	    	child = this.getChildAt(1);
	    	child.layout(toolBar.leftViewRect.left, toolBar.leftViewRect.top, toolBar.leftViewRect.right, toolBar.leftViewRect.bottom);

		}

		// measure sliding windows
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			setMeasuredDimension(displayWidth, displayHeight);
	    	View child;

	    	// top slider bar
	    	child = this.getChildAt(0);
    		measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
    		
    		// left slider bar
	    	child = this.getChildAt(1);
    		measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
		}
	}

	// app entry point
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);

		Display display = getWindowManager().getDefaultDisplay();
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
		
		rx = displayWidth / 2;
		ry = displayHeight / 2;
		
		bezierView = new BezierView(this);
		setContentView(bezierView);

		ui = new UI(this);
		
		LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		toolBar = new ToolBar(this, bezierView, inflater, displayWidth, displayHeight, handler);
		
		bezierView.addView(toolBar.topView);
		bezierView.addView(toolBar.leftView);
		if(toolBar.leftView.isOpened()) toolBar.leftView.close();
		//toolBar.leftView.setVisibility(View.INVISIBLE);
		toolBar.initGallery();

		pictureWidth = (int) displayWidth * 3;
		pictureHeight = (int) displayHeight * 3;
		scaleView = new ScaleView(displayWidth, displayHeight, pictureWidth, pictureHeight, bezierView);
		
		backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grid);
		backDrawable = new BitmapDrawable(backBitmap);
		backDrawable.setBounds(0, 0, backBitmap.getWidth(), backBitmap.getHeight());

		scrollPaint.setColor(Color.GRAY);
		scrollPaint.setAntiAlias(true);
		scrollPaint.setDither(true);
		scrollPaint.setStyle(Paint.Style.STROKE);
		scrollPaint.setStrokeJoin(Paint.Join.ROUND);
		scrollPaint.setStrokeCap(Paint.Cap.ROUND);
		scrollPaint.setStrokeWidth(SCROLL_SIZE);
		
		infoPaint.setColor(0xFF444444);
		if(displayWidth < displayHeight) infoPaint.setTextSize(displayWidth / 20);
		else infoPaint.setTextSize(displayHeight / 20);
		
		scrollTimer = new Timer();
		scrollTimer.scheduleAtFixedRate(new ScrollTask(), 0, SCROLL_INTERVAL);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// main touch event
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return ui.tapEvent(event);
	}

	// on finger down
	@Override
	public void onDown(float x, float y)
	{
		//Log.i(TAG, "onDown");
		scrollAlpha = SCROLL_ALPHA;
		
		scaleView.onDown(x, y);

		sX = x / zoomX;
		sY = y / zoomY;

		mX = x / zoomX;
		mY = y / zoomY;
		
		for(PolyDraw item:polyItemList)
		{	
			if(item != null)
			{
				switch(item.getState())
				{
				case PolyDraw.STATE_SELECT:
					selItem.onDown((mX * zoomX - (selItem.px * zoomX + xPos)) / zoomX, (mY * zoomY - (selItem.py * zoomY + yPos)) / zoomY);
					break;
				case PolyDraw.STATE_MOVE:
					if(round > 1)
					{
						item.px = (((int)(item.px / round)) * round);
						item.py = (((int)(item.py / round)) * round);
					}
					break;
				case PolyDraw.STATE_ROTATE:
					//if(startAng == 0)
						//startAng = Utils.getAng(item.px * zoomX + xPos + item.iw / 2, item.py * zoomY + yPos + item.ih / 2, mX * zoomX, mY * zoomY);
						startAng = Utils.getAng((item.px + item.u) * zoomX + xPos, (item.py + item.v) * zoomY + yPos, mX * zoomX, mY * zoomY);
					break;
				case PolyDraw.STATE_ZOOM:
					// calculate the distance between the center of the item and touch point
					//if(startDist == 0)
						startDist = Utils.getDist(item.px * zoomX + xPos + item.iw / 2, item.py * zoomY + yPos + item.ih / 2, mX * zoomX, mY * zoomY);
					break;					
				}
			}
		}

		return;
	}

	// on finger up
	@Override
	public void onUp(float x, float y)
	{
		//Log.i(TAG, "onUp");
		scaleView.onUp(x, y);
		
		for(PolyDraw item:polyItemList)
		{	
			switch(item.getState())
			{
			case PolyDraw.STATE_NEW:
			case PolyDraw.STATE_SELECT:
				selItem.onUp(x, y);
				break;
			case PolyDraw.STATE_MOVE:
				break;
			case PolyDraw.STATE_ROTATE:
				item.calculateRect();
				//item.setState(PolyDraw.STATE_ZOOM);
				break;
			case PolyDraw.STATE_ZOOM:
				//item.applyScale();
				item.calculateRect();
				item.setState(PolyDraw.STATE_NONE);
				break;				
			}
		}
		
		startAng = 0;
		startDist = 0;
		
		if (selItem != null)
			bezierView.postInvalidate();
	}

	// single tap
	@Override
	public void onTap(float x, float y)
	{
		//Log.i(TAG, "onTap");
		scrollAlpha = SCROLL_ALPHA;
		
		xPos = scaleView.xPos();
		yPos = scaleView.yPos();
		
		// select item
		selItem = selectItem((int)(x - xPos), (int)(y - yPos));
		// create a new or modify item
		if (selItem != null)
		{
			selItem.onDown((mX * zoomX - (selItem.px * zoomX + xPos)) / zoomX, (mY * zoomY - (selItem.py * zoomY + yPos)) / zoomY);
		}
		else
		{
			PolyDraw item = new PolyDraw(-xPos / zoomX, -yPos / zoomY, 5);
			item.setState(PolyDraw.STATE_NEW);
			selItem = item;
			selItem.onDown((mX * zoomX - (selItem.px * zoomX + xPos)) / zoomX, (mY * zoomY - (selItem.py * zoomY + yPos)) / zoomY);
			polyItemList.add(item);
		}
		
		bezierView.postInvalidate();
		
		return;
	}

	// keep touch
	@Override
	public void onHold(float x, float y)
	{
		scrollAlpha = SCROLL_ALPHA;
/*		if(selItem != null && selectItem((int)(x - xPos), (int)(y - yPos), false) == null)
		{
			selItem.setState(PolyDraw.STATE_NONE);
			selItem = null;
			bezierView.postInvalidate();
		}*/
	}

	// move the finger
	@Override
	public void onMove(float x, float y)
	{
		scrollAlpha = SCROLL_ALPHA;
		
		boolean found = false;

		mX = x / zoomX;
		mY = y / zoomY;
		
		for(PolyDraw item:polyItemList)
		{	
			switch(item.getState())
			{
			case PolyDraw.STATE_SELECT:
				if(item.midPoint != -1 || item.selVertex != -1 || item.selCont1 != -1 || item.selCont2 != -1)
				{
					found = true;
					float px = (mX * zoomX - (selItem.px * zoomX + xPos)) / zoomX;
					float py = (mY * zoomY - (selItem.py * zoomY + yPos)) / zoomY;
					if (round > 1)
					{
						px = (((int) (px / round)) * round);
						py = (((int) (py / round)) * round);
					}
					item.onMove(px, py);
					item.calculateRect();
				}
				break;
			case PolyDraw.STATE_MOVE:
				if(item.isInside(x - xPos, y - yPos, zoomX, zoomY, item.px, item.py))
				{
					found = true;
					moveSelectedItems(item);
				}
				break;
			case PolyDraw.STATE_ROTATE:
				//if(!item.isInside(x - xPos, y - yPos, zoomX, zoomY, item.px, item.py))
				{
					found = true;
					double dAng = Utils.getAng((item.px + item.u) * zoomX + xPos, (item.py + item.v) * zoomY + yPos, mX * zoomX, mY * zoomY);
					float max = round;
					if(max > 90) max = 90;
					if(max > 1) dAng = (((int)(dAng / max)) * max);
					item.mRotation += (float)(dAng - startAng);
					item.rotate((float)((dAng - startAng) * Utils.DEGTORAD));
					startAng = dAng;
				}
				break;				
			case PolyDraw.STATE_ZOOM:
				found = true;
				float px = (item.px + item.u) * zoomX + xPos;
				float py = (item.py + item.v) * zoomY + yPos;
				
				// swap to grid
				if(round > 1)
				{
					px = (((int)(px / round)) * round);
					py = (((int)(py / round)) * round);
				}
				
				double dDist = Utils.getDist(px, py, mX * zoomX, mY * zoomY) / ((item.iw + item.ih) / 2);
				if(dDist > 0.1f && dDist < 2)
				{
					item.mZoom = (float)dDist;

					//item.px -= (item.iw * dDist - item.iw) / 2;
					//item.py -= (item.ih * dDist - item.ih) / 2;
					
					item.Scale(item.mZoom);
				}
				startDist = dDist;
				break;
			}
		}


		if (!found) 
			scaleView.onMove(x, y);
		else 
			bezierView.postInvalidate();

		sX = mX;
		sY = mY;
	}

	// 
	@Override
	public void onSwipe(int direction, float velocity, float x1, float y1, float x2, float y2)
	{
		scaleView.onSwipe(direction, velocity, x1, y1, x2, y2);
	}

	@Override
	public void onDoubleTap(float x, float y)
	{
/*		if(selItem != null)
		{
			selItem.setState(PolyDraw.STATE_NONE);
			selItem = null;
			bezierView.postInvalidate();
		}*/
	}

	@Override
	public void onZoom(int mode, float x, float y, float distance, float xdiff, float ydiff)
	{
		scaleView.onZoom(mode, x, y, distance, xdiff, ydiff);

		zoomX = scaleView.getZoomX();
		zoomY = scaleView.getZoomY();
	}

	@Override
	public void onRotate(int mode, float x, float y, float angle)
	{
		return;
	}

	@Override
	public void onFingerChange()
	{
		return;
	}

	// draw background
	private void drawBack(Canvas canvas)
	{
		if(backDrawable != null)
		{
			// static back or tiles
			if(staticBack)
			{
				backDrawable.setBounds(0, 0, (int)(displayWidth), (int)(displayHeight));
				backDrawable.draw(canvas);
			}
			else for(float x = 0; x < pictureWidth; x += tileSize)
			{
				for(float y = 0; y < pictureHeight; y += tileSize)
				{
					// distance of the tile center from the rotation center
					final float dis = (float)Utils.getDist(rx * zoomX, ry * zoomY, (x + tileSize / 2) * zoomX, (y + tileSize / 2) * zoomY);
					// angle of the tile center from the rotation center
					final float ang = (float)Utils.getAng(rx * zoomX, ry * zoomY, (x + tileSize / 2) * zoomX, (y + tileSize / 2) * zoomY);
					
					// coordinates of the block after rotation
					final float cx = dis * (float)Math.cos((rotation + ang) * Utils.DEGTORAD) + rx * zoomX + xPos;
					final float cy = dis * (float)Math.sin((rotation + ang) * Utils.DEGTORAD) + ry * zoomY + yPos;
					
					if(cx >= -tileSize && cx <= displayWidth + tileSize && cy >= -tileSize && cy <= displayHeight + tileSize)
					{
						backDrawable.setBounds(0, 0, (int)(tileSize * zoomX) + 1, (int)(tileSize * zoomY) + 1);
	
						canvas.save();
						//canvas.rotate(tile.tilRot, ((tile.offPosX + tile.tilPosX) * zoomX) + xPos + tile.stoneBitmap.getWidth() * (zoomX) / 2, 
						//		((tile.offPosY + tile.tilPosY) * zoomY) + yPos + tile.stoneBitmap.getHeight() * zoomY / 2);
						canvas.rotate(rotation, rx * zoomX + xPos, ry * zoomY + yPos);
						canvas.translate(x * zoomX + xPos, y * zoomY + yPos);
						backDrawable.draw(canvas);
						canvas.restore();
					}
				}
			}
		}
		else
		{
			canvas.drawColor(backColor);
		}
	}
	
	// draw polygons
	private void drawItems(Canvas canvas)
	{
		float dx;
		float dy;

		if (scaleView != null)
		{
			xPos = scaleView.xPos();
			yPos = scaleView.yPos();
		}

		for (PolyDraw item : polyItemList)
		{
			if (item != null)
			{
				dx = item.px * zoomX + xPos;
				dy = item.py * zoomY + yPos;
				
				if (dx >= -(item.boundingRect.left + item.iw) * zoomX && dx <= displayWidth - item.boundingRect.left * zoomX && dy >= -(item.boundingRect.top + item.ih) * zoomY && dy <= displayHeight - item.boundingRect.top * zoomY)
				{
					canvas.save();
					canvas.translate(dx, dy);
					
					if(item.getState() == PolyDraw.STATE_SELECT || item.getState() == PolyDraw.STATE_NEW)
					{
						// draw polygon or line
						item.drawPolygon(canvas, zoomX, zoomY, item.points, item.control1, item.control2);
						
						// draw middle points
						item.drawMidPoints(canvas, zoomX, zoomY);
						// draw corner points
						item.drawVertices(canvas, zoomX, zoomY);
						// draw bezier control points
						item.drawControls(canvas, zoomX, zoomY);
					}
					else if(item.getState() == PolyDraw.STATE_MOVE || item.getState() == PolyDraw.STATE_NONE)
					{
						// draw polygon or line
						item.drawPolygon(canvas, zoomX, zoomY, item.points, item.control1, item.control2);
					}
					else if(item.getState() == PolyDraw.STATE_ROTATE)
					{
						// draw polygon or line
						item.drawPolygon(canvas, zoomX, zoomY, item.points, item.control1, item.control2);
						
						// center
						canvas.drawCircle(item.u * zoomX, item.v * zoomY, 10, item.pointPaint);// mid point
					}
					else if(item.getState() == PolyDraw.STATE_ZOOM)
					{
						// draw polygon or line
						item.drawPolygon(canvas, zoomX, zoomY, item.tp, item.tc1, item.tc2);
						
						// center
						canvas.drawCircle(item.u * zoomX, item.v * zoomY, 10, item.pointPaint);
					}
					
					//canvas.drawRoundRect(item.boundingRect, 20, 20, item.linePaint);

					canvas.restore();
					
					if(item.getState() == PolyDraw.STATE_ROTATE)
					{
						canvas.drawLine(dx + item.u * zoomX, dy + item.v * zoomY, mX * zoomX, mY * zoomY, item.linePaint);
						// touch point
						canvas.drawCircle(mX * zoomX, mY * zoomY, 10, item.pointPaint);
					}
					else if(item.getState() == PolyDraw.STATE_ZOOM)
					{
						canvas.drawLine(dx + item.u * zoomX, dy + item.v * zoomY, mX * zoomX, mY * zoomY, item.linePaint);
						// touch point
						canvas.drawCircle(mX * zoomX, mY * zoomY, 10, item.pointPaint);
					}					
				}
			}
		}

		return;
	}
	
	// show item information
	private void drawInfo(Canvas canvas)
	{
		//canvas.drawText("" + xPos, 10, 80, infoPaint);
		//canvas.drawText("" + (item.px - item.boundingRect.left + item.getWidth()), 10, 70, infoPaint);
		if(selItem != null)
		{
			canvas.drawText(selItem.getStateName(), 10, 40, infoPaint);
		}
	}
	
	// TODO select or deselect one or all item
    private PolyDraw selectItem(int x, int y)
    {
    	boolean redraw = false;
    	boolean found = false;
    	PolyDraw poly = null;
    	Boolean sel = false;
    	// check the tap is inside an item for(NoteItem item:noteItemList)
    	for(int i = polyItemList.size() - 1; i > -1; i--)
    	{
    		poly = polyItemList.get(i);
    		sel = poly.isInside(x, y, zoomX, zoomY, poly.px, poly.py);
    		if(sel)
    		{
    			redraw = true;
   				found = true;
   				switch(poly.getState())
   				{
        		case PolyDraw.STATE_NONE:
        		case PolyDraw.STATE_NEW:
        			poly.setState(PolyDraw.STATE_SELECT);
        			break;
        		case PolyDraw.STATE_SELECT:
        			if(poly.selVertex == -1 && poly.selCont1 == -1 && poly.selCont2 == -1)
        				poly.setState(PolyDraw.STATE_MOVE);
        			break;
        		case PolyDraw.STATE_MOVE:
        			poly.setState(PolyDraw.STATE_ROTATE);
        			break;
        		case PolyDraw.STATE_ROTATE:
        			poly.setState(PolyDraw.STATE_ZOOM);
        			break;
        		default:
        			poly.setState(PolyDraw.STATE_NONE);
        			found = false;
   				}
   				//Log.i(TAG, "" + poly.getState());
   				break;
    		}
    	}

    	// if outside then deselect all item
    	boolean isDel = false;
    	if(!found)
    	{
        	for(PolyDraw item: polyItemList)
        	{
        		// what to do on close item
        		switch(item.getState())
        		{
        		case PolyDraw.STATE_RESIZE:
        			break;
        		case PolyDraw.STATE_ROTATE:
        			break;
        		case PolyDraw.STATE_ZOOM:
        			break;
        		case PolyDraw.STATE_NEW:
        			break;
        		default:
        			item.setState(PolyDraw.STATE_NONE);
        		}
        		// delete empty items
        		if(item.points.size() == 1)
        		{
        			//isDel = true;
        			//item.deleted = true;
        		}

        	}
        	
        	if(isDel) deleteItem(null);
        	
        	//selectLastItem(null);
        	
        	//if(toolbarClass.leftView.isOpened()) toolbarClass.leftView.close();
        	//toolbarClass.leftView.setVisibility(View.INVISIBLE);
        	
        	selItem = null;
        	
        	redraw = true;
    	}
    	
    	if(redraw) bezierView.postInvalidate();
    	
    	return poly;
    }
    
	private synchronized void deleteItem(PolyDraw selItem)
	{
		if(selItem == null)
		{
			List<PolyDraw> newList = new ArrayList<PolyDraw>();
			List<PolyDraw> list = new ArrayList<PolyDraw>();
			for(PolyDraw li:polyItemList)
			{
				if(li.getState() == PolyDraw.STATE_SELECT)
				{
					list.add(li);
					if(li.linkerID.equals("") == false) updLiked(li.linkerID);
				}
				else if(li.deleted == false) newList.add(li);
				else if(li.linkerID.equals("") == false) updLiked(li.linkerID);
			}
			polyItemList.removeAll(list);
			polyItemList = newList; 
		}
		else
		{
			List<PolyDraw> newList = new ArrayList<PolyDraw>();
			for(PolyDraw li:polyItemList)
			{
				if(li != selItem && li.deleted == false) newList.add(li);
				else if(li.linkerID.equals("") == false) updLiked(li.linkerID);
			}
			polyItemList.remove(selItem);
			polyItemList = newList;
		}
		
		selItem = null;
		
		return;
	}
	
	private void updLiked(String id)
	{
		for(PolyDraw linker:polyItemList)
		{
			Log.i(TAG, linker.id + " " + id);
			if(linker.id.equals(id))
			{
				linker.setLinked(null);
				break;
			}
		}

		return;
	}
	
	// show position indicators
	private void drawScrollBars(Canvas canvas)
	{
		float x, y;
		float xSize = displayWidth / ((pictureWidth * zoomX) / displayWidth);
		float ySize = displayHeight / ((pictureHeight * zoomY) / displayHeight);

		x = (displayWidth / (pictureWidth * zoomX)) * -xPos;
		y = displayHeight - SCROLL_SIZE - 2;
		canvas.drawLine(x, y, x + xSize, y, scrollPaint);

		x = displayWidth - SCROLL_SIZE - 2;
		y = (displayHeight / (pictureHeight * zoomY)) * -yPos;
		canvas.drawLine(x, y, x, y + ySize, scrollPaint);
	}
	
	// move linked items in recursion
	private void moveSelectedItems(PolyDraw item)
	{
		moveItem(item);
		if(item.getLinked() != null) moveSelectedItems(item.getLinked());
	}
	
	// on move - relocate item
	private void moveItem(PolyDraw item)
	{
		float dx = mX - sX;
		float dy = mY - sY;
		
		if(round > 1)
		{
			dx = (((int)(mX / round)) * round) - (((int)(sX / round)) * round);
			dy = (((int)(mY / round)) * round) - (((int)(sY / round)) * round);
		}
				
		scrollX = 0;
		scrollY = 0;
		
		// item inside the paper
		if(xPos + ((item.px + item.l) * zoomX) + dx > pictureWidth && dx > 0) dx = 0;
		if(xPos + ((item.px + item.l) * zoomX) + dx + round < 0 && dx < 0) dx = 0;
		if(yPos + ((item.py + item.t) * zoomY) + dy > pictureHeight && dy > 0) dx = 0;
		if(yPos + ((item.py + item.t) * zoomY) + dy + round < 0 && dy < 0) dy = 0;
		
		float scx = scrollX;
		float scy = scrollY;

		// scroll background under item
		if(xPos + (item.px + item.l + item.iw) * zoomX >= displayWidth) scrollX = -1f;// scroll left
		if(xPos + (item.px + item.l) * zoomX <= 0) scrollX = 1f;// scroll right
		if(yPos + (item.py + item.t + item.ih) * zoomY >= displayHeight) scrollY = -1f;// scroll up
		if(yPos + (item.py + item.t) * zoomY <= 0) scrollY = 1f;// scroll down
	
		if(scx != scrollX) scrollMul = 1;
		if(scy != scrollY) scrollMul = 1;
		
		item.px += dx;
		item.py += dy;
		
		return;
	}

	
    // scroll task for scrolling background
	class ScrollTask extends TimerTask
	{
		public void run()
		{
			boolean redraw = false;
			
			// scroll to selected object
			if(scrollMul < .05f)
			{
				scrollTo = false;
				scrollX = 0;
				scrollY = 0;
			}
			else
			{
				if(scrollTo)
				{
					if((int)Math.abs(xDest - xPos) < halfX || (int)Math.abs(yDest - yPos) < halfY) scrollMul -= 0.05f;
					else scrollMul += 0.05f;
					redraw = true;
				}
			}
			
			// left and top scroll in zoomed
			if(xPos + scrollX < displayWidth - pictureWidth * zoomX || xPos + scrollX > 0) scrollX = 0;
			if(yPos + scrollY < displayHeight - pictureHeight * zoomY || yPos + scrollY > 0) scrollY = 0;
			
			// auto scroll paper
			if (scrollX != 0 || scrollY != 0)
			{
				xPos += scrollX * scrollMul;
				yPos += scrollY * scrollMul;
			
				if(scrollTo == false)
				{

					for(PolyDraw item:polyItemList)
					{
						if(item.getState() == PolyDraw.STATE_MOVE)
						{
							item.px -= scrollX * scrollMul / zoomX;
							item.py -= scrollY * scrollMul / zoomY;
							redraw = true;
						}
					}
					
					if(scrollMul < 10) scrollMul += 0.05f;
				}
				
				scaleView.setPos(xPos, yPos);
			}
	    	
	    	if(scrollAlpha > 32)
	    	{
	    		scrollAlpha--;
	    		if(scrollAlpha > 255) scrollPaint.setAlpha(255);
	    		else scrollPaint.setAlpha(scrollAlpha);
	    		redraw = true;
	    	}
	    	
	    	if(redraw) bezierView.postInvalidate();
		}
	}
	
	// scroll to center the linked item
	private void scrollToItem(PolyDraw item)
	{
		// position of the linked item
		xDest = (displayWidth / 2) - (item.px * zoomX) - (item.iw / 2);
		yDest = (displayHeight / 2) - (item.py * zoomY) - (item.ih / 2);
		//scaleView.setPos(xDest, yDest);
		
		// velocity
		halfX = (int) Math.abs((xDest - xPos) / 2);
		halfY = (int) Math.abs((yDest - yPos) / 2);

		// x, y steps
		scrollX = (xDest - xPos) / 200;
		scrollY = (yDest - yPos) / 200;
		
		//Log.i(TAG, "" + xDest + " " + yDest + " " + xPos + " " + yPos + " " + (xDest - (xPos / zoomX)) + " " + (yDest - (yPos / zoomY)));
		//Log.i(TAG, "" + scrollX + " " + scrollY + " " + halfX + " " + halfY);

		scrollMul = .05f;
		
		scrollTo = true;
	}
}