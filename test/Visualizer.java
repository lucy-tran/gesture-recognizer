 import edu.macalester.graphics.Point;
 import edu.macalester.graphics.Rectangle;
 import edu.macalester.graphics.*;

import edu.macalester.graphics.CanvasWindow;

import java.awt.*;
import java.util.Deque;
import java.util.Iterator;


public class Visualizer extends CanvasWindow {

    private Recognizer recognizer;
    private Deque<Point> originalPoints;

    public Visualizer(){
        super("$1 Recognizer Visualizer", 600, 600);

        recognizer = new Recognizer();
        IOManager ioManager = new IOManager();
        originalPoints = ioManager.loadGesture("arrowTest.xml");

        drawLegend();
        drawAxes();
        visualizeSteps();
    }

    private void visualizeSteps(){
        drawPath(originalPoints, Color.BLACK);

        int n = 25;
        Deque<Point> resampled = recognizer.resample(originalPoints, n);
        drawPath(resampled, Color.BLUE);

        Deque<Point> rotated = recognizer.rotateBy(resampled, -recognizer.indicativeAngle(resampled));
        drawPath(rotated, Color.RED);

        Deque<Point> scaled = recognizer.scaleTo(rotated, 100);
        drawPath(scaled, Color.GREEN);

        Deque<Point> translated = recognizer.translateTo(scaled, new Point(0.0,0.0));
        drawPath(translated, Color.MAGENTA);
    }

    private void drawLegend(){
        GraphicsGroup legend = new GraphicsGroup();
        legend.add(createLegendRow(Color.BLUE, "Resampled"), 0, 0);
        legend.add(createLegendRow(Color.RED, "Rotated"), 0, 25);
        legend.add(createLegendRow(Color.GREEN, "Scaled"), 0, 50);
        legend.add(createLegendRow(Color.MAGENTA, "Translated"), 0, 75);
        add(legend, 10, 10);
    }

    private void drawAxes(){
        Line xAxis = new Line(getWidth()/2.0-10, getHeight()/2.0, getWidth(), getHeight()/2.0);
        xAxis.setStrokeColor(Color.LIGHT_GRAY);
        add(xAxis);
        Line yAxis = new Line(getWidth()/2.0, getHeight()/2.0-10, getWidth()/2.0, getHeight());
        yAxis.setStrokeColor(Color.LIGHT_GRAY);
        add(yAxis);
        GraphicsText text = new GraphicsText("(0,0)", getWidth()/2-20, getHeight()/2-20);
        text.setFillColor(Color.LIGHT_GRAY);
        add(text);
    }

    private GraphicsGroup createLegendRow(Color color, String label){
        GraphicsGroup row = new GraphicsGroup();
        Rectangle rect = new Rectangle(0,0,20,20);
        rect.setFilled(true);
        rect.setFillColor(color);
        row.add(rect);
        GraphicsText text = new GraphicsText(label, 30, 0);
        double height = text.getHeight();
        text.setY(height);
        row.add(text);
        return row;
    }


    //For debugging
    private void drawPath(Deque<Point> path, Color color){
        Iterator<Point> it = path.iterator();
        GraphicsGroup group = new GraphicsGroup(getWidth()/2.0, getHeight()/2.0);
        while(it.hasNext()){
            Point p = it.next();
            Ellipse e = new Ellipse(p.getX(), p.getY(), 5, 5);
            e.setFilled(true);
            e.setStroked(false);
            e.setFillColor(color);
            group.add(e);
        }
        add(group);
    }

    public static void main(String[] args){
        Visualizer vis = new Visualizer();
    }
}
