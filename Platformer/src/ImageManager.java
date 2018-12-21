import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageManager
{
    private String directory = "Images/";
    private HashMap<String, ArrayList<BufferedImage>> imageMap;
    private GraphicsConfiguration graphicsConfiguration;

    public ImageManager(String imagesConfigFile)
    {
        //Setup the image map and graphics configuration
        imageMap = new HashMap<>();
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphicsConfiguration = graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration();
        loadImagesFromFile(imagesConfigFile);
    }

    /**
     * Load a single or sequence of images located within the given file under the given directory
     * (Images/ by default). For a single image, the format of the file is as follows "image".
     * The key/name of the image in the imageMap will be set to the "image" minus the .ext
     * (if it even has an extension). For a sequence of images, the format of the file is as follows
     * "[sequence name: image1, image2]". In this case, the key/name of the images will be set to
     * "sequence name". Lines that do not adhere to this format will be skipped. Lines
     * beginning with // will be regarded as comments and blank lines will also be skipped.
     * @param fileName The name of the file to load images from.
     */
    private void loadImagesFromFile(String fileName)
    {
        //Inform the user of the file reading
        System.out.println("Reading file: " + directory + fileName);
        try
        {
            //Create an InputStream and BufferedReader to read the file
            InputStream inputStream = this.getClass().getResourceAsStream(directory + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            //Loop until the end of the file is reached
            String line;
            while ((line = br.readLine()) != null)
            {
                //Determine what action to take based off the line read
                if (line.startsWith("//") || (line.length() == 0)) //This line is a comment or blank line
                {
                    continue;
                }
                else if (line.startsWith("[")) //This line is a sequence of images
                {
                    loadImageSequence(line);
                }
                else //The line is a single image
                {
                    loadSingleImage(line);
                }
            }

            //Close the BufferedReader
            br.close();

            //Inform the user the ImageLoader is done reading
            System.out.println("Finished reading file: " + directory + fileName);
        }
        catch (IOException e) {
            System.out.println("Error reading file: " + directory + fileName + " " + e);
            e.printStackTrace();
        }
    }

    /**
     * Load the single specified image from the line, and store it in the imageMap.
     * @param line The line containing the file to be loaded.
     * @return True or false (success or fail)
     */
    private boolean loadSingleImage(String line)
    {
        //Get the image name (remove an .extension, if any)
        String imageName = line; //Set the imageName to the line by default
        if (line.contains("."))
        {
            imageName = line.substring(0, line.indexOf('.'));
        }

        //Check that the imageMap does not already contain this file or one using this name
        if (imageMap.containsKey(imageName))
        {
            System.out.println("ImageMap already contains: " + imageName);
            return false;
        }

        //Load the image
        BufferedImage image = loadImage(line);

        //Store the new image in the imageMap if it is not null
        if (image != null)
        {
            //Add the new image to an new array list
            ArrayList<BufferedImage> imageList = new ArrayList<>();
            imageList.add(image);
            imageMap.put(imageName, imageList);
            System.out.println("Stored " + imageName + " [" + line + "]");
            return true;
        }

        //Something went wrong
        return false;
    }

    /**
     * Load a sequence of images from the line and store it in the imageMap.
     * @param line The line containing the sequence of images.
     * @return True or false (success or fail)
     */
    private boolean loadImageSequence(String line)
    {
        //Get the image name
        String imageName = line.substring(1, line.indexOf(':')); //Skip the first open bracket

        //Check that the imageMap does not already contain this file or one using this name
        if (imageMap.containsKey(imageName))
        {
            System.out.println("ImageMap already contains: " + imageName);
            return false;
        }

        //Split the line into an array of strings using the commas
        line = line.substring(line.indexOf(":") + 1, line.indexOf("]"));
        line = line.trim();
        String[] lines = line.split(",");

        //Load the images
        ArrayList<BufferedImage> imageList = new ArrayList<>();
        BufferedImage loadedImage;
        for (int i = 0; i < lines.length; i++)
        {
            //Load the image and store it in the imageList
            loadedImage = loadImage(lines[i].trim()); //Trim whitespace off the lines

            //Check that the image is not null
            if (loadedImage != null)
            {
                imageList.add(loadedImage);
            }
            else //Do not load any more images if a single image was null
            {
                return false;
            }
        }

        //Put the image sequence in the imageMap
        imageMap.put(imageName, imageList);
        System.out.println("Stored " + imageName + " [" + line + "]");
        return true;
    }

    /**
     * Loads the specified image from the line and ensures the image becomes a managed image
     * using the computer's graphics configuration.
     * @param line The line containing the file to be loaded.
     * @return The new managed BufferedImage, or null on failure.
     */
    private BufferedImage loadImage(String line)
    {
        try
        {
            //Read in the image and store it in a new BufferedImage
            BufferedImage readImage = ImageIO.read(getClass().getResource(directory + line));

            //Create a new copy of the image to ensure it becomes a managed image
            int transparency = readImage.getColorModel().getTransparency();
            BufferedImage copy = graphicsConfiguration.createCompatibleImage(readImage.getWidth(),
                    readImage.getHeight(), transparency);
            //Create a graphics context to draw the image onto
            Graphics2D g2d = copy.createGraphics();
            g2d.drawImage(readImage, 0, 0, null);
            g2d.dispose();

            //Return the new image
            return copy;
        }
        catch (IOException e)
        {
            System.out.println("Error loading image [" + line + "]");
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Unable to find image [" + line + "]");
        }

        //Something went wrong
        return null;
    }


    public ArrayList<BufferedImage> getImages(String imageKey)
    {
        //Return the stored array list, check for null
        if (imageMap.containsKey(imageKey))
        {
            return (imageMap.get(imageKey));
        }

        System.out.println("No images found under '" + imageKey + "'");
        return null;
    }

    /**
     * Determine whether the imageMap contains the given key.
     * @param imageName The key value to be checked.
     * @return True or false (if the key is mapped).
     */
    public boolean imageExists(String imageName)
    {
        //See if the imageName is a key used in the imageMap
        return imageMap.containsKey(imageName);
    }
}

