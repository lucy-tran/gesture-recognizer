import edu.macalester.graphics.Point;

import java.util.Deque;


public class Template {
    private String name;
    private Deque<Point> points;

    public Template(String name, Deque<Point> points){
        this.name = name;
        this.points = points;
    }

    public Deque<Point> getPoints(){
        return points;
    }

    public String getName(){
        return name;
    }
}
