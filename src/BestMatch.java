import edu.macalester.graphics.*;

import java.util.Deque;

public class BestMatch extends Template{
    private String name;
    private double matchScore;

    public BestMatch(String name, double matchScore, Deque<Point> points){
        super(name, points);
        this.name = name;
        this.matchScore = matchScore;
    }

    @Override
    public String toString() {
        return name + ". Match score: " + matchScore;
    }
}
