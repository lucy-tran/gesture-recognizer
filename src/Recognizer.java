import edu.macalester.graphics.Point;

import java.util.*;

/**
 * Recognizer to recognize 2D gestures. Uses the $1 gesture recognition algorithm.
 */
public class Recognizer {
    private List<Template> templates;
    private final double SIZE = 250;
    private final double RESAMPLED_POINTS = 64;
    private Deque<Point> processedCandidate;

    /**
     * Constructs a recognizer object
     */
    public Recognizer() {
        templates = new ArrayList<>();
        processedCandidate = new ArrayDeque<>();
    }

    /**
     * Create a template to use for matching
     *
     * @param name   of the template
     * @param points in the template gesture's path
     */
    public void addTemplate(String name, Deque<Point> points) {
        Deque<Point> processedTemplate = processPoints(points);
        Template newTemplate = new Template(name, processedTemplate);
        templates.add(newTemplate);
    }

    public List<Template> getTemplates() {
        return templates;
    }

    /**
     * Recognizes the candidate gesture against all available templates. The best matching template has the minimum distanceAtBestAngle
     * with the processed candidate.
     *
     * @param candidate The gesture that needs to be recognized.
     * @return The template that best matches the candidate gesture.
     */
    public BestMatch recognize(Deque<Point> candidate) {
        processedCandidate = processPoints(candidate);
        Template bestTemplate = templates.stream()
                .min(Comparator.comparingDouble(template -> distanceAtBestAngle(processedCandidate, template.getPoints()))).get();
        double score = calculateScore(bestTemplate.getPoints());
        return new BestMatch(bestTemplate.getName(), score, bestTemplate.getPoints());
    }

    /**
     * Processes the set of points through the 4 steps described in the article.
     *
     * @param points The set of points that needs to be processed.
     * @return The set of processed points.
     */
    public Deque<Point> processPoints(Deque<Point> points) {
        Deque<Point> sampled = resample(points, RESAMPLED_POINTS);
        double indicativeAngle = indicativeAngle(sampled);
        Deque<Point> processedPoints = translateTo(
                scaleTo(
                        rotateBy(sampled, -indicativeAngle), SIZE), new Point(0,0));
        return processedPoints;
    }

    /**
     * Calculates the matching score between the processed candidate and a template.
     *
     * @param bestMatch The set of points representing a template.
     * @return The matching score.
     */
    public double calculateScore(Deque<Point> bestMatch) {
        System.out.println("Candidate: " + processedCandidate.size());
        System.out.println("Best match: " + bestMatch.size());
        double score = 1.0 - ((distanceAtBestAngle(processedCandidate, bestMatch) / (0.5 * Math.sqrt(2 * SIZE * SIZE))));
        System.out.println("score: " + score);
        return score;
    }

    /**
     * Resamples a deque of points by setting the number of points to n and equalizing the distance between points.
     *
     * @param originalPoints The set of points that is to be resampled.
     * @param n              The number of points after resampling process.
     * @return The resampled deque of points.
     */
    public Deque<Point> resample(Deque<Point> originalPoints, double n) {
        Deque<Point> resampledPoints = new ArrayDeque<>();
        double pathLength = pathLength(originalPoints);
        double resampleInterval = pathLength / (n - 1.0);
        Iterator<Point> iterator = originalPoints.iterator();
        Point previousPoint = iterator.next();
        resampledPoints.add(previousPoint);
        Point currentPoint = iterator.next();
        double accumulatedDistance = 0;
        double segmentDistance = 0;

        System.out.println(pathLength);

        while (iterator.hasNext()) {
            segmentDistance = currentPoint.distance(previousPoint);
            if ((segmentDistance + accumulatedDistance) < resampleInterval) {
                accumulatedDistance += segmentDistance;
                previousPoint = currentPoint;
                currentPoint = iterator.next();
            } else {
                Point newPoint = Point.interpolate(previousPoint, currentPoint, (resampleInterval - accumulatedDistance) / segmentDistance);
                resampledPoints.add(newPoint);
                previousPoint = newPoint;
                accumulatedDistance = 0;
            }
        }
        if (segmentDistance < resampleInterval) {
            resampledPoints.add(originalPoints.getLast());
        }
        return resampledPoints;
    }

    /**
     * Calculates the path length from the first point to the last point of a path.
     */
    public double pathLength(Deque<Point> path) {
        double pathLength = 0;
        Iterator<Point> iterator = path.iterator();
        Point firstPoint = path.getFirst();
        while (iterator.hasNext()) {
            Point currentPoint = iterator.next();
            pathLength += firstPoint.distance(currentPoint);
            firstPoint = currentPoint;
        }
        return pathLength;
    }

    /**
     * Calculates the angle from the x-axis to the line connecting centroid and the first point.
     */
    public double indicativeAngle(Deque<Point> originalPoints) {
        return getCentralPoint(originalPoints).subtract(originalPoints.getFirst()).angle();
    }

    /**
     * @return The centroid of a gesture.
     */
    public Point getCentralPoint(Deque<Point> originalPoints) {
        double xSum = 0;
        double ySum = 0;
        for (Point point : originalPoints) {
            xSum += point.getX();
            ySum += point.getY();
        }
        Point centroid = new Point(xSum / originalPoints.size(), ySum / originalPoints.size());
        return centroid;
    }

    /**
     * Rotates the gesture by angle.
     * @param originalPoints
     * @param angle
     * @return The rotated gesture.
     */
    public Deque<Point> rotateBy(Deque<Point> originalPoints, double angle) {
        Point central = getCentralPoint(originalPoints);
        Deque<Point> rotatedPoints = new ArrayDeque<>();
        for (Point point : originalPoints) {
            Point newPoint = point.rotate(angle, central);
            rotatedPoints.add(newPoint);
        }
        return rotatedPoints;
    }

    /**
     * Scales the gesture to a bounding box
     */
    public Deque<Point> scaleTo(Deque<Point> points, double size) {
        Point bounding = getBoundingBox(points);
        double width = bounding.getX();
        double height = bounding.getY();
        Deque<Point> scaledPoints = new ArrayDeque<>();
        for (Point point : points) {
            Point newPoint = point.scale(size / width, size / height);
            scaledPoints.add(newPoint);
        }
        return scaledPoints;
    }

    public Point getBoundingBox(Deque<Point> rotatedPath) {
        Point max = rotatedPath.peek();
        Point min = rotatedPath.peek();

        for (Point point : rotatedPath) {
            max = Point.max(point, max);
            min = Point.min(point, min);
        }
        return max.subtract(min);
    }

    /**
     * Translates the gesture to the new position.
     * @param points
     * @param newPosition The position that marks as the new position of centroid of the translated gesture.
     * @return
     */
    public Deque<Point> translateTo(Deque<Point> points, Point newPosition) {
        Point centroid = getCentralPoint(points);
        Deque<Point> translatedPoints = new ArrayDeque<>();
        for (Point point : points) {
            Point newPoint = point.add(newPosition).subtract(centroid);
            translatedPoints.add(newPoint);
        }
        return translatedPoints;
    }

    /**
     * Uses a golden section search to calculate rotation that minimizes the distance between the gesture and the template points.
     *
     * @param points
     * @param templatePoints
     * @return best distance
     */
    private double distanceAtBestAngle(Deque<Point> points, Deque<Point> templatePoints) {
        double thetaA = -Math.toRadians(45);
        double thetaB = Math.toRadians(45);
        final double deltaTheta = Math.toRadians(2);
        double phi = 0.5 * (-1.0 + Math.sqrt(5.0));// golden ratio
        double x1 = phi * thetaA + (1 - phi) * thetaB;
        double f1 = distanceAtAngle(points, templatePoints, x1);
        double x2 = (1 - phi) * thetaA + phi * thetaB;
        double f2 = distanceAtAngle(points, templatePoints, x2);
        while (Math.abs(thetaB - thetaA) > deltaTheta) {
            if (f1 < f2) {
                thetaB = x2;
                x2 = x1;
                f2 = f1;
                x1 = phi * thetaA + (1 - phi) * thetaB;
                f1 = distanceAtAngle(points, templatePoints, x1);
            } else {
                thetaA = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1 - phi) * thetaA + phi * thetaB;
                f2 = distanceAtAngle(points, templatePoints, x2);
            }
        }
        return Math.min(f1, f2);
    }

    /**
     * Returns the path-distance between the candidate and template points at a specific angle of the candidate.
     *
     * @param points
     * @param templatePoints
     * @param theta
     * @return
     */
    private double distanceAtAngle(Deque<Point> points, Deque<Point> templatePoints, double theta) {
        Deque<Point> rotatedPoints = rotateBy(points, theta);
        return pathDistance(rotatedPoints, templatePoints);
    }

    /**
     * The average distance between corresponding points of 2 gestures.
     * @param a The first gesture
     * @param b The second gesture
     * @return The average path distance between a and b.
     */
    public double pathDistance(Deque<Point> a, Deque<Point> b) {
        double size = a.size();
        double sum = 0;
        Iterator<Point> iteratorA = a.iterator();
        Iterator<Point> iteratorB = b.iterator();
        if (a.size() == b.size()) {
            while (iteratorA.hasNext()) {
                sum += iteratorA.next().distance(iteratorB.next());
            }
        }
        return sum / size;
    }
}