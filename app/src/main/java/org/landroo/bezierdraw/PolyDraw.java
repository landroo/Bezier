package org.landroo.bezierdraw;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

public class PolyDraw
{
	private static final String TAG = "PolyDraw";
	
	// colors
	public static final int COLOR_RED = 0xFFF15F74;
	public static final int COLOR_ORANGE = 0xFFF76D3C;
	public static final int COLOR_GRAY = 0xFF839098;
	public static final int COLOR_YELLOW = 0xFFF7D842;
	public static final int COLOR_PURPLE = 0xFF913CCD;
	public static final int COLOR_GREEN = 0xFF98CB4A;
	public static final int COLOR_BLUE = 0xFF5481E6;
	public static final int COLOR_CYAN = 0xFF2CA8C2;
	public static final int COLOR_BLACK = 0xFF303437;
	public static final int COLOR_WHITE = 0xFFC5C6C7;
	
	public static final int TOLERANCE = 30;
	
	public static final int POLYGON = 1;
	public static final int POLYLINE = 2;
	
	public static final int STATE_NONE = 0;
	public static final int STATE_NEW = 1;
	public static final int STATE_SELECT = 2;
	public static final int STATE_MOVE = 3;
	public static final int STATE_ROTATE = 4;
	public static final int STATE_ZOOM = 5;
	public static final int STATE_RESIZE = 6;
	
	public double SEGMENTS = 20;
	
	public ArrayList<PointF> midPoints = new ArrayList<PointF>();
	
	public ArrayList<PointF> points = new ArrayList<PointF>();	
	public ArrayList<PointF> control1 = new ArrayList<PointF>();
	public ArrayList<PointF> control2 = new ArrayList<PointF>();
	
	public ArrayList<PointF> tp = new ArrayList<PointF>();	
	public ArrayList<PointF> tc1 = new ArrayList<PointF>();
	public ArrayList<PointF> tc2 = new ArrayList<PointF>();
	
	// undo list
	private ArrayList<EditingStates> editStates = new ArrayList<EditingStates>();
	
	public int lineMode = POLYGON;
	
	public int midPoint = -1;
	public int selVertex = -1;
	public int selCont1 = -1;
	public int selCont2 = -1;
	
	public boolean move = false;
	
	//private int insertIndex;

	public int[] color_fill = new int[2];
	public int color_line = 0xFF000000;
	
	public Paint linePaint = new Paint();
	public Paint polyPaint = new Paint();
	public Paint controlPaint = new Paint();
	public Paint pointPaint = new Paint();
	public Paint contPaint = new Paint();
	public Paint rectPaint = new Paint();

	public int lineSize = 5;
	private int dotSize = 10;

	public float px;
	public float py;
	public float lastX;
	public float lastY;
	public float ox = -1;
	public float oy = -1;
	
	public float iw, ih;
	public RectF boundingRect = new RectF();
	
	public float mRotation = 0;// rotation angle in degree
	public float mLastRot = 0;// last rotation angle in degree
	public float mZoom = 0;
	
	public float u, v, l, t;
	
	private int state = STATE_NEW;
	
	public Bitmap fillBmp;
	
	public boolean deleted = false;
	
	private PolyDraw linked = null;// linked item
	private PolyDraw linker = null;// linker item
	public String linkedID = "";
	public String linkerID = "";
	public String id;
	public String info;
	
	private PathEffect effect;
	private PathEffect lineEffect = null;
	public int pathSize = 4;
	
	private PointF a1 = new PointF();
	private PointF a2 = new PointF();
	private PointF c1 = new PointF();
	private PointF c2 = new PointF();
	private PointF pnt;
	private double step = 1 / SEGMENTS;
	private Path path = new Path();
	
	public PolyDraw(float x, float y, int size)
	{
		this.id = "" + UUID.randomUUID();
		
		px = x;
		py = y;
		lineSize = size;
		
		linePaint.setColor(color_line);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(lineSize);
		linePaint.setAntiAlias(true);
		linePaint.setDither(true);
		linePaint.setStrokeJoin(Paint.Join.ROUND);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
		
		color_fill[0] = 0x55FF0000;
		color_fill[1] = 0x550000FF;
		LinearGradient gradient = new LinearGradient(0, 0, 0, 10, color_fill, null, android.graphics.Shader.TileMode.CLAMP);

		polyPaint.setStyle(Paint.Style.FILL);
		polyPaint.setShader(gradient);
		polyPaint.setAntiAlias(true);
		polyPaint.setDither(true);
		polyPaint.setStrokeJoin(Paint.Join.ROUND);
		polyPaint.setStrokeCap(Paint.Cap.ROUND);
		
		effect = new PathDashPathEffect(makePathPattern(pathSize * 4, pathSize * 2), pathSize * 4, 0.0f, PathDashPathEffect.Style.ROTATE);
		
		controlPaint.setAntiAlias(true);
		controlPaint.setDither(true);
		controlPaint.setColor(0xFF000000);
		controlPaint.setStyle(Paint.Style.STROKE);
		controlPaint.setStrokeJoin(Paint.Join.ROUND);
		controlPaint.setStrokeCap(Paint.Cap.ROUND);
		controlPaint.setStrokeWidth(1);
		
		pointPaint.setStyle(Paint.Style.FILL);
		pointPaint.setAntiAlias(true);
		pointPaint.setDither(true);
		
		contPaint.setAntiAlias(true);
		contPaint.setDither(true);
		contPaint.setStyle(Paint.Style.STROKE);
		contPaint.setStrokeWidth(3);

		rectPaint.setAntiAlias(true);
		rectPaint.setDither(true);
		rectPaint.setStyle(Paint.Style.STROKE);
		rectPaint.setStrokeWidth(5);
		
		setState(STATE_NEW);
	}
	
	public void setFillPattern(Bitmap bitmap)
	{
		fillBmp = bitmap;
		BitmapShader fillBMPshader = new BitmapShader(fillBmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		polyPaint.setShader(fillBMPshader);
		int a = (color_fill[0] >> 24) & 0xFF;
		polyPaint.setAlpha(a);
	}

	// draw polygon or polyline
	public void drawPolygon(Canvas canvas, float zx, float zy, ArrayList<PointF> pl, ArrayList<PointF> cl1, ArrayList<PointF> cl2) 
	{
		if(pl.size() <= 1) return;
		
		path.reset();
		
		path.moveTo(pl.get(0).x * zx, pl.get(0).y * zy);
		for(int i = 1; i < points.size(); i++)
		{
			a1.x = pl.get(i - 1).x * zx;
			a1.y = pl.get(i - 1).y * zy;
			a2.x = pl.get(i).x * zx;
			a2.y = pl.get(i).y * zy;
			c1.x = cl1.get(i).x * zx;
			c1.y = cl1.get(i).y * zy;
			c2.x = cl2.get(i).x * zx;
			c2.y = cl2.get(i).y * zy;
			
			for (double u = 0; u <= 1; u += step) 
			{ 
				pnt = bezPoint(u, a1, a2, c1, c2);
				path.lineTo(pnt.x, pnt.y);
			} 
		}
		// close it if it is polygon
		if(lineMode == POLYGON)
		{
			a1.x = pl.get(points.size() - 1).x * zx;
			a1.y = pl.get(points.size() - 1).y * zy;
			a2.x = pl.get(0).x * zx;
			a2.y = pl.get(0).y * zy;
			c1.x = cl1.get(0).x * zx;
			c1.y = cl1.get(0).y * zy;
			c2.x = cl2.get(0).x * zx;
			c2.y = cl2.get(0).y * zy;

			for (double u = 0; u <= 1; u += step) 
			{ 
				pnt = bezPoint(u, a1, a2, c1, c2);
				path.lineTo(pnt.x, pnt.y);
			} 
		}
		
		if(lineMode == POLYGON) canvas.drawPath(path, polyPaint);

		// draw line
		canvas.drawPath(path, linePaint);
	}
	
	private PointF bezPoint(double u, PointF a1, PointF a2, PointF c1, PointF c2)
	{
		PointF pnt = new PointF();
		pnt.x = (float)(Math.pow(u, 3) * (a2.x + 3 * (c1.x - c2.x) - a1.x) + 3 * Math.pow(u, 2) * (a1.x - 2 * c1.x + c2.x) + 3 * u * (c1.x - a1.x) + a1.x);
		pnt.y = (float)(Math.pow(u, 3) * (a2.y + 3 * (c1.y - c2.y) - a1.y) + 3 * Math.pow(u, 2) * (a1.y - 2 * c1.y + c2.y) + 3 * u * (c1.y - a1.y) + a1.y);
		
		return pnt;
	}
	
	// draw mid point of the lines
	public void drawMidPoints(Canvas canvas, float zx, float zy) 
	{
		// draw mid-point
		if(points.size() > 1) 
		{
			int index;
			
			// clear middle point array
			midPoints.clear();
			
			// calculate line middle points
			for(int i = 1; i < points.size(); i++) 
			{
				a1.x = points.get(i - 1).x * zx;
				a1.y = points.get(i - 1).y * zy;
				a2.x = points.get(i).x * zx;
				a2.y = points.get(i).y * zy;
				c1.x = control1.get(i).x * zx;
				c1.y = control1.get(i).y * zy;
				c2.x = control2.get(i).x * zx;
				c2.y = control2.get(i).y * zy;
				pnt = bezPoint(1 / SEGMENTS * 2, a1, a2, c1, c2);
				midPoints.add(pnt);
			}
			
			// close the polygon
			if(lineMode == POLYGON) 
			{ 
				a1.x = points.get(points.size() - 1).x * zx;
				a1.y = points.get(points.size() - 1).y * zy;
				a2.x = points.get(0).x * zx;
				a2.y = points.get(0).y * zy;
				c1.x = control1.get(0).x * zx;
				c1.y = control1.get(0).y * zy;
				c2.x = control2.get(0).x * zx;
				c2.y = control2.get(0).y * zy;
				pnt = bezPoint(1 / SEGMENTS * 2, a1, a2, c1, c2);
				midPoints.add(pnt);
			}
			
			
			RectF rect = new RectF(0, 0, dotSize * 2, dotSize * 2);
			
			index = 0;
			for(PointF pnt: midPoints) 
			{
				rect.left = pnt.x - dotSize;
				rect.top = pnt.y - dotSize;
				rect.right = pnt.x + dotSize;
				rect.bottom = pnt.y + dotSize;
				
				if(midPoint  == index) rectPaint.setColor(Color.RED);
				else rectPaint.setColor(Color.GREEN);
				
				canvas.drawRect(rect, rectPaint);
				index++;
			}
		}
	}
	
	// draw points
	public void drawVertices(Canvas canvas, float zx, float zy) 
	{
		int index = 0;
		
		for(PointF pnt: points) 
		{
			// selected
			if(selVertex == index) pointPaint.setColor(Color.RED);
			// last
			else if(index == points.size() - 1) pointPaint.setColor(Color.BLUE);
			// others
			else pointPaint.setColor(Color.BLACK);
			
			canvas.drawCircle(pnt.x * zx, pnt.y * zy, dotSize, pointPaint);
			index++;
		}
	}
	
	// draw control points
	public void drawControls(Canvas canvas, float zx, float zy) 
	{
		int idx = selVertex;
		if(idx == -1) idx = selCont1;
		if(idx == -1) idx = selCont2;
		
		if(idx != -1)
		{
			canvas.drawLine(control1.get(idx).x * zx, control1.get(idx).y * zy, points.get(idx).x * zx, points.get(idx).y * zy, controlPaint);
			canvas.drawLine(control2.get(idx).x * zx, control2.get(idx).y * zy, points.get(idx).x * zx, points.get(idx).y * zy, controlPaint);
			
			if(selCont2 == -1) contPaint.setColor(Color.BLACK);
			else contPaint.setColor(Color.RED);
			canvas.drawCircle(control2.get(idx).x * zx, control2.get(idx).y * zy, dotSize + 3, contPaint);

			if(selCont1 == -1) contPaint.setColor(Color.BLACK);
			else contPaint.setColor(Color.RED);
			canvas.drawCircle(control1.get(idx).x * zx, control1.get(idx).y * zy, dotSize + 3, contPaint);
		}
	}
	

	// clear
	public void clear() 
	{
		points.clear();
		midPoints.clear();
		control1.clear();
		control2.clear();
		midPoint = -1;
		selVertex = -1;
		selCont1 = -1;
		selCont2 = -1;
	}
	
	// add a new point or select an existing or a midpoint
	public void onDown(float x, float y)
	{
		//Log.i(TAG, "onDown");
		move = true;
		
		lastX = x; 
		lastY = y;
		
		if(ox == -1) ox = x;
		if(oy == -1) oy = y;
		
		if(state == STATE_NEW || state == STATE_SELECT)
		{
			if(midPoint == -1) 
			{ 
				// check if user tries to select an existing point.
				midPoint = getSelectedIndex(x, y, midPoints);
				//Log.i(TAG, "midPoint: " + midPoint);
				if(midPoint == -1) 
				{
					// check control points
					selCont1 = getSelectedIndex(x, y, control1);
					//Log.i(TAG, "selCont1: " + selCont1);
					if(selCont1 == -1) 
					{
						// check control points
						selCont2 = getSelectedIndex(x, y, control2);
						//Log.i(TAG, "selCont2: " + selCont2);
						if(selCont2 == -1)
						{
							// check vertices
							selVertex = getSelectedIndex(x, y, points);
							//Log.i(TAG, "selVertex: " + selVertex);
							if(selVertex == -1 && state == STATE_NEW)
							{ 
								points.add(new PointF(x, y));
								control1.add(new PointF(x, y));
								control2.add(new PointF(x, y));
								//Log.i(TAG, "add " + x +" " + y);
								
								calculateRect();
							}
							else if(selVertex != -1)
							{
								 setState(STATE_SELECT);
							}
						}
						else
						{
							selCont1 = -1;
						}
					}
					else
					{
						selCont2 = -1;
					}
				}
			}
		}
		
		if(midPoint == -1 && selVertex == -1 && selCont1 == -1 && selCont1 == -1)
			move = false;
		
		return;
	}
	
	// move corner point
	public void onMove(float x, float y) 
	{
		//Log.i(TAG, "onMove");
		if(!move) onDown(x, y);
		
		lastX = x; 
		lastY = y;
		
		if(midPoint != -1) 
		{
			points.add(midPoint + 1, new PointF(x, y));
			control1.add(midPoint + 1, new PointF(x, y));
			control2.add(midPoint + 1, new PointF(x, y));
			selVertex = midPoint + 1;
			midPoint = -1;
		} 
		else if(selCont1 != -1)
		{
			control1.get(selCont1).x = x;
			control1.get(selCont1).y = y;
			//Log.i(TAG, "selCont1: " + x + " " + y);
		}
		else if(selCont2 != -1)
		{
			control2.get(selCont2).x = x;
			control2.get(selCont2).y = y;
			//Log.i(TAG, "selCont2: " + x + " " + y);
		}
		else if(selVertex != -1) 
		{
			float dx = points.get(selVertex).x - x;
			float dy = points.get(selVertex).y - y;
			
			control1.get(selVertex).x -= dx;
			control1.get(selVertex).y -= dy;

			control2.get(selVertex).x -= dx;
			control2.get(selVertex).y -= dy;
			
			points.get(selVertex).x = x;
			points.get(selVertex).y = y;
		}

		return;
	}
	
	// finish drawing
	public void onUp(float x, float y)
	{
		//Log.i(TAG, "onUp");
		move = false;
		
		midPoint = -1;
		
		calculateRect();
		
		return;
	}
	
	// check point selected
	private int getSelectedIndex(double x, double y, ArrayList<PointF> points) 
	{
		int idx = -1;
		if(points == null || points.size() == 0) return idx;
		
		double dist;
		for(int i = 0; i < points.size(); i++) 
		{
			PointF pnt = points.get(i);
			dist = Utils.getDist(pnt.x, pnt.y, x, y);
			//Log.i(TAG, "x1:" + pnt.x + " y1: " + pnt.y + " x2: " + x + " y2: " + y +  " Dist: " + dist);
			if(dist < TOLERANCE) 
			{
				idx = i;
				break;
			}
		}
		
		return idx;
	}
	
	// delete a point
	public void deletePoint()
	{
		if(points.size() > 0)
		{
			// remove last vertex
			if(selVertex == -1) points.remove(points.size() - 1); 
			if(midPoint == -1) points.remove(midPoint);
			
			midPoint = -1;
			selVertex = -1;
			selCont1 = -1;
			selCont2 = -1;
		}
	}

	// rotate points
	public void rotate(float ang)
	{
		for(PointF pnt: points)
			pnt.set(Utils.rotatiePnt(u, v, pnt.x, pnt.y, ang));
		for(PointF pnt: control1)
			pnt.set(Utils.rotatiePnt(u, v, pnt.x, pnt.y, ang));
		for(PointF pnt: control2)
			pnt.set(Utils.rotatiePnt(u, v, pnt.x, pnt.y, ang));
	}
	
	// real time scale
	public void Scale(float zoom)
	{
		tp.clear();
		for(PointF pnt: points)
			tp.add(new PointF(pnt.x * zoom, pnt.y * zoom));
		tc1.clear();
		for(PointF pnt: control1)
			tc1.add(new PointF(pnt.x * zoom, pnt.y * zoom));
		tc2.clear();
		for(PointF pnt: control2)
			tc2.add(new PointF(pnt.x * zoom, pnt.y * zoom));
	}
	
	// real time scale
	public void applyScale()
	{
		for(PointF pnt: points)
			pnt.set(pnt.x * mZoom, pnt.y * mZoom);
		for(PointF pnt: control1)
			pnt.set(pnt.x * mZoom, pnt.y * mZoom);
		for(PointF pnt: control2)
			pnt.set(pnt.x * mZoom, pnt.y * mZoom);
	}		
/*	
	// apply new size
	public void applySize(float xRat, float yRat)
	{
		points.clear();
		points.addAll(tmppnt);
		tmppnt.clear();
	}
	
	// realtime resize
	public void ReSize(float xRat, float yRat)
	{
		tmppnt.clear();
		for(PointF pnt: points)
			tmppnt.add(new PointF(pnt.x * xRat, pnt.y * yRat));
	}
*/	
	// calculate bounding rectangle
	public RectF calculateRect()
	{
		RectF rect = new RectF();
		
		float f = Float.MAX_VALUE;
		for(PointF pnt: points)
			if(f > pnt.x) f = pnt.x;
		rect.left = f;
		
		f = 0;
		for(PointF pnt: points)
			if(f < pnt.x) f = pnt.x;
		rect.right = f;
		
		f = Float.MAX_VALUE;
		for(PointF pnt: points)
			if(f > pnt.y) f = pnt.y;
		rect.top = f;

		f = 0;
		for(PointF pnt: points)
			if(f < pnt.y) f = pnt.y;
		rect.bottom = f;
		
		//Log.i(TAG, "" + rect.left + " " + rect.top + " " + rect.right + " " + rect.bottom);
		boundingRect = rect;
		iw = rect.width();
		ih = rect.height();
		
		u = rect.left + iw / 2;
		v = rect.top + ih /2;
		
		l = rect.left;
		t = rect.top;
		
		color_fill[0] = 0x55FF0000;
		color_fill[1] = 0x550000FF;
		LinearGradient gradient = new LinearGradient(rect.left, rect.top, rect.right, rect.bottom, color_fill, null, android.graphics.Shader.TileMode.CLAMP);
		polyPaint.setShader(gradient);
		
		return rect;
	}

	// select the polygon or line x, y point, yx, zy zoom, ox, oy offset
	public boolean isInside(float x, float y, float zx, float zy, float ox, float oy)
	{
		boolean bIn = false;
		Float dist;
		
		// check the point is inside the polygon or on the line
		if(lineMode == POLYGON)
		{
			
			float[] pxa = new float[(int) ((points.size() + 1) * SEGMENTS)];
			float[] pya = new float[(int) ((points.size() + 1) * SEGMENTS)];
			pxa[0] = (points.get(0).x + ox) * zx;
			pya[0] = (points.get(0).y + oy) * zy;
			int cnt = 1;
			for(int i = 1; i < points.size(); i++)
			{
				a1.x = (points.get(i - 1).x + ox) * zx;
				a1.y = (points.get(i - 1).y + oy) * zy;
				a2.x = (points.get(i).x + ox) * zx;
				a2.y = (points.get(i).y + oy) * zy;
				c1.x = (control1.get(i).x + ox) * zx;
				c1.y = (control1.get(i).y + oy) * zy;
				c2.x = (control2.get(i).x + ox) * zx;
				c2.y = (control2.get(i).y + oy) * zy;				

				for (double u = 0; u <= 1; u += step) 
				{
					pnt = bezPoint(u, a1, a2, c1, c2);
					pxa[cnt] = pnt.x;
					pya[cnt] = pnt.y;
					cnt++;
				}				
			}
			a1.x = (points.get(points.size() - 1).x + ox) * zx;
			a1.y = (points.get(points.size() - 1).y + oy) * zy;
			a2.x = (points.get(0).x + ox) * zx;
			a2.y = (points.get(0).y + oy) * zy;
			c1.x = (control1.get(0).x + ox) * zx;
			c1.y = (control1.get(0).y + oy) * zy;
			c2.x = (control2.get(0).x + ox) * zx;
			c2.y = (control2.get(0).y + oy) * zy;
			pnt = bezPoint(1 / SEGMENTS * 2, a1, a2, c1, c2);
			for (double u = 0; u <= 1; u += step) 
			{
				pnt = bezPoint(u, a1, a2, c1, c2);
				pxa[cnt] = pnt.x;
				pya[cnt] = pnt.y;
				cnt++;
			}				
			//Log.i(TAG, "" + cnt);
			bIn = Utils.ponitInPoly(cnt, pxa, pya, x, y);
		}
		else
		{
			pnt = new PointF(x, y);
			PointF p1 = new PointF();	
			PointF p2;
			p1.x = (points.get(0).x + ox) * zx;
			p1.y = (points.get(0).y + oy) * zy;
			for(int i = 1; i < points.size(); i++)
			{
				a1.x = (points.get(i - 1).x + ox) * zx;
				a1.y = (points.get(i - 1).y + oy) * zy;
				a2.x = (points.get(i).x + ox) * zx;
				a2.y = (points.get(i).y + oy) * zy;
				c1.x = (control1.get(i).x + ox) * zx;
				c1.y = (control1.get(i).y + oy) * zy;
				c2.x = (control2.get(i).x + ox) * zx;
				c2.y = (control2.get(i).y + oy) * zy;				
				
				for (double u = 0; u <= 1; u += step) 
				{
					p2 = p1;
					p1 = bezPoint(u, a1, a2, c1, c2);
					dist = Utils.pointLineDist(p1, p2, pnt);
					if(dist < TOLERANCE)
					{
						bIn = true;
						break;
					}
				}
				if(bIn) break;
			}
		}
		// check the control points
		if(bIn == false)
		{
			for(int i = 0; i < control1.size(); i++)
			{
				a1.x = (control1.get(i).x + ox) * zx;
				a1.y = (control1.get(i).y + oy) * zy;
				dist = (float) Utils.getDist(a1.x, a1.y, x, y);
				if(dist < TOLERANCE)
				{
					bIn = true;
					break;
				}
			}
		}
		if(bIn == false)
		{
			for(int i = 0; i < control2.size(); i++)
			{
				a1.x = (control2.get(i).x + ox) * zx;
				a1.y = (control2.get(i).y + oy) * zy;
				dist = (float) Utils.getDist(a1.x, a1.y, x, y);
				if(dist < TOLERANCE)
				{
					bIn = true;
					break;
				}
			}
		}
		
		return bIn;
	}
	
	public void setLinker(PolyDraw linker)
	{
		this.linker = linker;
		this.linkerID = linker.id;
	}
	
	public void setLinked(PolyDraw linked)
	{
		this.linked = linked;
		if(linked != null)
		{
			this.linkedID = linked.id;
			linked.setLinker(this);
		}
	}
	
	// create selection border pattern
	public static Path makePathPattern(float w, float h)
	{
		Path path = new Path();
		path.moveTo(0, 0);
		path.lineTo(w / 2, 0);
		path.lineTo(w, h);
		path.lineTo(w / 2, h);
		path.close();

		return path;
	}
	
	public void setState(int mode)
	{
		state = mode;
		switch(state)
		{
		case STATE_NONE:
			linePaint.setColor(color_line);
			linePaint.setPathEffect(lineEffect);
			linePaint.setStrokeWidth(lineSize);
			break;
		case STATE_NEW:
			linePaint.setColor(COLOR_ORANGE);
			linePaint.setPathEffect(effect);
			linePaint.setStrokeWidth(pathSize);
			break;
		case STATE_SELECT:
			linePaint.setColor(Color.BLACK);
			linePaint.setPathEffect(effect);
			linePaint.setStrokeWidth(pathSize);
			break;
		case STATE_MOVE:
			linePaint.setColor(COLOR_RED);
			linePaint.setPathEffect(effect);
			linePaint.setStrokeWidth(pathSize);
			break;
		case STATE_ROTATE:
			linePaint.setColor(COLOR_GREEN);
			linePaint.setPathEffect(effect);
			linePaint.setStrokeWidth(pathSize);
			break;
		case STATE_ZOOM:
			linePaint.setColor(COLOR_BLUE);
			linePaint.setPathEffect(effect);
			linePaint.setStrokeWidth(pathSize);
			break;
		case STATE_RESIZE:
			linePaint.setColor(COLOR_PURPLE);
			linePaint.setPathEffect(effect);
			linePaint.setStrokeWidth(pathSize);
			break;
		}
	}
	
	public int getState()
	{
		return state;
	}
	
	public String getStateName()
	{
		String s = "";
		switch(state)
		{
		case STATE_NONE:
			s = "none";
			break;
		case STATE_NEW:
			s = "new";
			break;
		case STATE_SELECT:
			s = "select";
			break;
		case STATE_MOVE:
			s = "move";
			break;
		case STATE_ROTATE:
			s = "rotate";
			break;
		case STATE_ZOOM:
			s = "zoom";
			break;
		case STATE_RESIZE:
			s = "resize";
			break;
		}
		return s;
	}
	
	
	// 0-STATE_NONE, 1-STATE_NEW, 2-STATE_SELECT, 3-STATE_MOVE, 4-STATE_ROTATE, 5-STATE_ZOOM, 6-STATE_RESIZE
	public void nextState()
	{
		this.state++;
		if (this.state > 6) this.state = 0;
	}
	
	// states class
	public class EditingStates 
	{
		public ArrayList<PointF> points = new ArrayList<PointF>();
		public ArrayList<PointF> anchor1 = new ArrayList<PointF>();
		public ArrayList<PointF> anchor2 = new ArrayList<PointF>();
		
		public boolean midpointSelected = false;
		public int insertIndex;

		public EditingStates(ArrayList<PointF> points, ArrayList<PointF> anchor1, ArrayList<PointF> anchor2, boolean midpointselected, int insertingindex) 
		{
			this.points.addAll(points);
			this.anchor1.addAll(anchor1);
			this.anchor2.addAll(anchor2);
			this.midpointSelected = midpointselected;
			this.insertIndex = insertingindex;
		}
		
		public EditingStates copy()
		{
			EditingStates edt = new EditingStates(points, anchor1, anchor2, midpointSelected, insertIndex);
			
			return edt;
		}
	}
	
	public PolyDraw getLinked()
	{
		return linked;
	}
}
