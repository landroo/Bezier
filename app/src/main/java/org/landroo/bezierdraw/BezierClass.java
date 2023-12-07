package org.landroo.bezierdraw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

public class BezierClass
{
	public static final double SEGMENTS = 10;
	
	//Reference objects
	private PointF anchor1;
	private PointF anchor2;
	private PointF control1;
	private PointF control2;
	
	private Paint pointPaint1;
	private Paint pointPaint2;
	private Paint linePaint1;
	private Paint linePaint2;
	
	public BezierClass(PointF anchorPoint1, PointF anchorPoint2, PointF controlPoint1, PointF controlPoint2)
	{
		anchor1 = anchorPoint1;
		anchor2 = anchorPoint2;
		control1 = controlPoint1;
		control2 = controlPoint2;
		
		pointPaint1 = new Paint();
		pointPaint1.setAntiAlias(true);
		pointPaint1.setDither(true);
		pointPaint1.setColor(0xFF000000);
		pointPaint1.setStyle(Paint.Style.FILL);
		pointPaint1.setStrokeJoin(Paint.Join.ROUND);
		pointPaint1.setStrokeCap(Paint.Cap.ROUND);
		
		pointPaint2 = new Paint();
		pointPaint2.setAntiAlias(true);
		pointPaint2.setDither(true);
		pointPaint2.setColor(0xFFFF0000);
		pointPaint2.setStyle(Paint.Style.FILL);
		pointPaint2.setStrokeJoin(Paint.Join.ROUND);
		pointPaint2.setStrokeCap(Paint.Cap.ROUND);
		
		linePaint1 = new Paint();
		linePaint1.setAntiAlias(true);
		linePaint1.setDither(true);
		linePaint1.setColor(0xFF000000);
		linePaint1.setStyle(Paint.Style.STROKE);
		linePaint1.setStrokeJoin(Paint.Join.ROUND);
		linePaint1.setStrokeCap(Paint.Cap.ROUND);
		linePaint1.setStrokeWidth(1);
		
		linePaint2 = new Paint();
		linePaint2.setAntiAlias(true);
		linePaint2.setDither(true);
		linePaint2.setColor(0xFF000000);
		linePaint2.setStyle(Paint.Style.STROKE);
		linePaint2.setStrokeJoin(Paint.Join.ROUND);
		linePaint2.setStrokeCap(Paint.Cap.ROUND);
		linePaint2.setStrokeWidth(7);
		
	}
	
	public void drawCurve(Canvas canvas)
	{
		Path curve = drawPath();
		canvas.drawPath(curve, linePaint2);
	}
	
	public void drawControl(Canvas canvas)
	{
		canvas.drawLine(control1.x, control1.y, anchor2.x, anchor2.y, linePaint1);
		canvas.drawLine(control2.x, control2.y, anchor2.x, anchor2.y, linePaint1);
		
		canvas.drawCircle(anchor1.x, anchor1.y, 10, pointPaint1);
		canvas.drawCircle(anchor2.x, anchor2.y, 10, pointPaint1);
		
		canvas.drawCircle(control1.x, control1.y, 10, pointPaint2);
		canvas.drawCircle(control2.x, control2.y, 10, pointPaint2);
	}
	
	public Path drawPath()
	{
		double step = 1 / SEGMENTS;
		Path line = new Path();
		
		line.moveTo(anchor1.x, anchor1.y);
		
		PointF a1 = anchor1;
		PointF a2 = anchor2;
		
		PointF c1 = control1;
		PointF c2 = control2;
//		double dist = Utils.getDist(anchor2.x, anchor2.y, c1.x, c1.y);
//		if(dist < 50) c1 = anchor2;
//		dist = Utils.getDist(anchor2.x, anchor2.y, c2.x, c2.y);
//		if(dist < 50) c2 = anchor2;
		
		double posx;
		double posy;
		
		//This loops draws each step of the curve
		for (double u = 0; u <= 1; u += step) 
		{ 
			posx = Math.pow(u, 3) * (a2.x + 3 * (c1.x - c2.x) - a1.x) + 3 * Math.pow(u, 2) * (a1.x - 2 * c1.x + c2.x) + 3 * u * (c1.x - a1.x) + a1.x;
			posy = Math.pow(u, 3) * (a2.y + 3 * (c1.y - c2.y) - a1.y) + 3 * Math.pow(u, 2) * (a1.y - 2 * c1.y + c2.y) + 3 * u * (c1.y - a1.y) + a1.y;
			line.lineTo((float)posx, (float)posy);
		} 
		
		//As a final step, make sure the curve ends on the second anchor
		line.lineTo(anchor2.x, anchor2.y);
		
		return line;
	}
	
	public void setControl(PointF controlPoint, int point)
	{
		if(point == 1) control1 = controlPoint;
		if(point == 2) control2 = controlPoint;
	}
	
	
}
