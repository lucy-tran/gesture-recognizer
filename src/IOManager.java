import edu.macalester.graphics.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Handles input/put for saving/loading gestures. Used for testing.
 * Created by bjackson on 10/31/2016.
 */
public class IOManager {

    public IOManager(){
    }

    /**
     * Loads a gesture from an xml file and returns a deque of the points
     * @param fileName Name of a file in the res folder (e.g. arrow.xml)
     * @return Queue of points contained in the gesture
     */
    public Deque<Point> loadGesture(String fileName){
        try {
            String path = getClass().getClassLoader().getResource(fileName).getPath();
            path = path.replace("%20", " ");
            File file = new File(path);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setIgnoringComments(true);
            documentBuilderFactory.setIgnoringElementContentWhitespace(true);
            documentBuilderFactory.setValidating(false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            NodeList tags = document.getElementsByTagName("Point");

            Deque<Point> gesture = new ArrayDeque<>();

            for(int i = 0; i < tags.getLength(); i++){
                Node node = tags.item(i);
                NamedNodeMap attributes = node.getAttributes();
                Point point = new Point(Double.parseDouble(attributes.getNamedItem("X").getNodeValue()), Double.parseDouble(attributes.getNamedItem("Y").getNodeValue()));
                gesture.add(point);
            }
            return gesture;
        }
        catch (Exception e){
            System.out.println("ERROR reading gesture file:");
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Saves a gesture to an xml file.
     * @param gesture Queue of gesture points
     * @param gestureName The name of the gesture
     * @param fileName The filename for the gesture. (e.g. arrow.xml)
     */
    public void saveGesture(Deque<Point> gesture, String gestureName, String fileName){
        try {
            // Find the res folder and append the filename to the path.
            Path path = Paths.get(getClass().getClassLoader().getResource("res-Readme.txt").toURI());
            String filePath =  path.getParent().resolve(fileName).toString();
            filePath = filePath.replace("%20", " ");

            File file = new File(filePath);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setIgnoringComments(true);
            documentBuilderFactory.setIgnoringElementContentWhitespace(true);
            documentBuilderFactory.setValidating(false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element gestureTag = document.createElement("Gesture");
            gestureTag.setAttribute("Name", gestureName);
            gestureTag.setAttribute("NumPts", Integer.toString(gesture.size()));
            document.appendChild(gestureTag);

            for(Point point : gesture){
                Element pointTag = document.createElement("Point");
                pointTag.setAttribute("X", Double.toString(point.getX()));
                pointTag.setAttribute("Y", Double.toString(point.getY()));
                gestureTag.appendChild(pointTag);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
            System.out.println("Saved file to "+filePath);
        }
        catch (Exception e){
            System.out.println("ERROR saving gesture file:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
