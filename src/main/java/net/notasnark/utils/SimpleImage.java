/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

public class SimpleImage implements ImageObserver {
    private Image image = null;

    private String fontName = "Verdana";

    /**
     * Create a new blank image.
     *
     * @param width
     *            Width of the image.
     * @param height
     *            Height of the image.
     */
    public SimpleImage(int width, int height) {
        System.setProperty("java.awt.headless", "true");
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public SimpleImage(Image image) {
        this.image = image;
    }

    public SimpleImage(File file) {
        image = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
        MediaTracker tracker = new MediaTracker(new Container());
        tracker.addImage(image, 1);
        try {
            tracker.waitForID(1);
        } catch (InterruptedException e) {
            // Don't care.
        }
    }

    public static Image createImage(int width, int height, String colour) {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();

        if (colour.startsWith("#")) {
            colour = colour.substring(1);
        }

        int red = Integer.parseInt(colour.substring(0, 2), 16);
        int green = Integer.parseInt(colour.substring(2, 4), 16);
        int blue = Integer.parseInt(colour.substring(4, 6), 16);
        int alpha = 255;
        if (colour.length() == 8) {
            alpha = Integer.parseInt(colour.substring(6, 8), 16);
        }

        g.setColor(new Color(red, green, blue, alpha));
        g.fillRect(0, 0, width, height);

        return image;
    }

    private static int var(int colour, int variance) {
        colour += Die.die(variance) - Die.die(variance);
        if (colour < 0) colour = 0;
        if (colour > 255) colour = 255;
        return colour;
    }

    public static Image createImage(int width, int height, String colour, int v) {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();

        if (colour.startsWith("#")) {
            colour = colour.substring(1);
        }

        int red = Integer.parseInt(colour.substring(0, 2), 16);
        int green = Integer.parseInt(colour.substring(2, 4), 16);
        int blue = Integer.parseInt(colour.substring(4, 6), 16);
        int alpha = 255;
        if (colour.length() == 8) {
            alpha = Integer.parseInt(colour.substring(6, 8));
        }

        for (int y=0; y < height; y++) {
            for (int x=0; x < height; x++) {
                g.setColor(new Color(var(red, v), var(green, v), var(blue, v), alpha));
                g.fillRect(x, y, 1, 1);
            }
        }

        return image;
    }

    public static Image createImage(int width, int height, URL url)
            throws MalformedURLException {
        Image image = Toolkit.getDefaultToolkit().getImage(url);

        MediaTracker tracker = new MediaTracker(new Container());
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            // Don't care.
        }
        image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        tracker.addImage(image, 1);
        try {
            tracker.waitForID(1);
        } catch (Exception e) {
            // Don't care.
        }

        return image;
    }

    /**
     * Gets a resized version of this image.
     *
     * @param width     New width.
     * @param height    New height.
     * @return          New resized version of this image.
     */
    public SimpleImage resize(int width, int height) {
        Image i = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bimage.createGraphics();
        g.drawImage(i, 0, 0, null);
        g.dispose();

        return new SimpleImage(bimage);
    }


    public SimpleImage crop(int x, int y, int width, int height) {
        SimpleImage		cropped = new SimpleImage(width, height);

        BufferedImage	bimage = getBufferedImage();

        BufferedImage   copy = bimage.getSubimage(x, y, width, height);
        cropped.paint(copy, 0, 0, width, height);

        return cropped;
    }

    /**
     * Create a new plain image with the specified colour. The colour is of the
     * form '#rrggbb' or '#rrggbbaa' where aa is the alpha value. The hash is
     * optional.
     *
     * @param width
     *            Width of the image.
     * @param height
     *            Height of the image.
     * @param colour
     *            RGB or RGBA colour string.
     */
    public SimpleImage(int width, int height, String colour) {
        image = createImage(width, height, colour);
    }

    public SimpleImage(int width, int height, int colour) {
        image = createImage(width, height, Integer.toHexString(colour));
    }

    public Image getImage() {
        return image;
    }

    public BufferedImage getBufferedImage() {
        return getBufferedImage(true);
    }

    public BufferedImage getBufferedImage(boolean alpha) {
        int 		  type = alpha?BufferedImage.TYPE_INT_ARGB:BufferedImage.TYPE_INT_RGB;

        BufferedImage bimage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), type);

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    /**
     * Gets the width of this image in pixels.
     *
     * @return	Width in pixels.
     */
    public int getWidth() {
        return image.getWidth(null);
    }

    /**
     * Gets the height of this image in pixels.
     *
     * @return	Height in pixels.
     */
    public int getHeight() {
        return image.getHeight(null);
    }

    public int getColour(int x, int y) {
        BufferedImage bimage = getBufferedImage();

        return bimage.getRGB(x, y);
    }

    public void paint(URL url, int x, int y, int width, int height)
            throws MalformedURLException {
        Graphics g = image.getGraphics();
        Image i = createImage(width, height, url);
        g.drawImage(i, x, y, this);
    }

    public void paint(Image i, int x, int y, int width, int height) {
        Graphics g = image.getGraphics();
        g.drawImage(i, x, y, width, height, this);
    }

    /**
     * Get a colour from a string described a hex RGB value.
     *
     * @param colour
     *            Colour, in the form RRGGBB in hex notation.
     * @return A Java Color object.
     */
    private Color getColour(String colour) {
        Color color;
        int r = 0;
        int g = 0;
        int b = 0;
        int	alpha = 255;

        try {
            if (colour.startsWith("#")) {
                r = Integer.parseInt(colour.substring(1, 3), 16);
                g = Integer.parseInt(colour.substring(3, 5), 16);
                b = Integer.parseInt(colour.substring(5, 7), 16);
                if (colour.length() > 7) {
                    alpha = Integer.parseInt(colour.substring(7, 9), 16);
                }
            } else {
                StringTokenizer tokens = new StringTokenizer(colour);
                double d = 0.0;
                // Red.
                d = Double.parseDouble(tokens.nextToken());
                r = (int) (255 * d);
                // Green.
                d = Double.parseDouble(tokens.nextToken());
                g = (int) (255 * d);
                // Blue.
                d = Double.parseDouble(tokens.nextToken());
                b = (int) (255 * d);
            }
            color = new Color(r, g, b, alpha);
        } catch (NumberFormatException e) {
            // Bad colour.
            System.out.println("Bad colour [" + colour + "]");
            color = new Color(0, 0, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Bad colour [" + colour + "] [" + r + "," + g
                    + "," + b + "] (" + e.getMessage() + ")");
            color = new Color(0, 0, 0);
        }

        return color;
    }

    public static String getDarker(String colour) {
        return getDarker(colour, 1);
    }

    /**
     * Gets a darker shade of the given colour.
     *
     * @param colour	Colour to get shade of, in #rrggbb notation.
     * @return			Darker version of this colour.
     */
    public static String getDarker(String colour, int tint) {
        try {
            if (colour.startsWith("#")) {
                int r = Integer.parseInt(colour.substring(1, 3), 16);
                int g = Integer.parseInt(colour.substring(3, 5), 16);
                int b = Integer.parseInt(colour.substring(5, 7), 16);

                r = (r * tint) / (tint + 1);
                g = (g * tint) / (tint + 1);
                b = (b * tint) / (tint + 1);

                colour = String.format("#%02x%02x%02x", r, g, b);
            }
        } catch (NumberFormatException e) {
            // Bad colour.
            System.out.println("Bad colour [" + colour + "]");
        }

        return colour;
    }

    public static String getLighter(String colour) {
        return getLighter(colour, 1);
    }

    /**
     * Gets a lighter shade of the given colour.
     *
     * @param colour	Colour to get shade of, in #rrggbb notation.
     * @return			Lighter version of this colour.
     */
    public static String getLighter(String colour, int tint) {
        try {
            if (colour.startsWith("#")) {
                int r = Integer.parseInt(colour.substring(1, 3), 16);
                int g = Integer.parseInt(colour.substring(3, 5), 16);
                int b = Integer.parseInt(colour.substring(5, 7), 16);

                r = (255 * tint + r) / (tint + 1);
                g = (255 * tint + g) / (tint + 1);
                b = (255 * tint + b) / (tint + 1);

                colour = String.format("#%02x%02x%02x", r, g, b);
            }
        } catch (NumberFormatException e) {
            // Bad colour.
            System.out.println("Bad colour [" + colour + "]");
        }

        return colour;
    }

    /**
     * Draw a filled circle at the specified pixel location, with the given
     * radius and colour.
     *
     * @param x
     *            X position of the circle's centre.
     * @param y
     *            Y position of the circle's centre.
     * @param radius
     *            Radius in pixels.
     * @param colour
     *            Colour as a RRGGBB string.
     */
    public void circle(int x, int y, int radius, String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(getColour(colour));
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    public void line(double x0, double y0, double x1, double y1) {
        line((int) x0, (int) y0, (int) x1, (int) y1, "#000000", 0f);
    }

    public void line(int x0, int y0, int x1, int y1) {
        line(x0, y0, x1, y1, "#000000", 0f);
    }

    public void line(double x0, double y0, double x1, double y1, String colour, float width) {
        line((int) x0, (int) y0, (int) x1, (int) y1, colour, width);
    }

    public void line(double x0, double y0, double x1, double y1, String colour) {
        line((int) x0, (int) y0, (int) x1, (int) y1, colour, 0f);
    }

    public void line(int x0, int y0, int x1, int y1, String colour, float width) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setStroke(new BasicStroke(width));
        g.setColor(getColour(colour));
        g.drawLine(x0, y0, x1, y1);
    }

    public void roundedLine(int x0, int y0, int x1, int y1, String colour, float width) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(getColour(colour));
        g.drawLine(x0, y0, x1, y1);
    }

    public void dot(int x, int y, String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setStroke(new BasicStroke(1));
        g.setColor(getColour(colour));
        g.drawLine(x, y, x, y);
    }

    /**
     * Draw a rectangle outline of the given colour.
     *
     * @param x         X-coordinate from top left.
     * @param y         Y-coordinate from top letf.
     * @param w         Width in pixels.
     * @param h         Height in pixels.
     * @param colour    Colour.
     */
    public void rectangle(int x, int y, int w, int h, String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setColor(getColour(colour));
        g.drawRect(x, y, w, h);
    }

    /**
     * Draw a filled rectangle of the given colour.
     *
     * @param x         X-coordinate from top left.
     * @param y         Y-coordinate from top letf.
     * @param w         Width in pixels.
     * @param h         Height in pixels.
     * @param colour    Colour.
     */
    public void rectangleFill(int x, int y, int w, int h, String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setColor(getColour(colour));
        g.fillRect(x, y, w, h);
    }

    /**
     * Draws an isosceles triangle outline. The triangle always has a flat
     * base, and is either pointing up (negative height) or down (positive
     * height). The length of the base is twice the given width.
     *
     * @param x			X coordinate of left edge.
     * @param y			Y coordinate of left edge.
     * @param w			Half width.
     * @param h			Height.
     * @param colour	Colour to use for outline.
     */
    public void triangle(int x, int y, int w, int h, String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setColor(getColour(colour));
        int[]	xp = new int[3];
        int[]	yp = new int[3];

        xp[0] = x;
        yp[0] = y;

        xp[1] = x + 2 * w;
        yp[1] = y;

        xp[2] = x + w;
        yp[2] = y + h;

        g.drawPolygon(xp, yp, 3);
    }

    /**
     * Draws an isosceles filled triangle. The triangle always has a flat
     * base, and is either pointing up (negative height) or down (positive
     * height). The length of the base is twice the given width.
     *
     * @param x			X coordinate of left edge.
     * @param y			Y coordinate of left edge.
     * @param w			Half width.
     * @param h			Height.
     * @param colour	Colour to use for outline.
     */
    public void triangleFill(int x, int y, int w, int h, String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setColor(getColour(colour));
        int[]	xp = new int[3];
        int[]	yp = new int[3];

        xp[0] = x;
        yp[0] = y;

        xp[1] = x + 2 * w;
        yp[1] = y;

        xp[2] = x + w;
        yp[2] = y + h;

        g.fillPolygon(xp, yp, 3);
    }

    public void text(int x, int y, String text, int style, int size,
                     String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(getColour(colour));
        g.setFont(new Font(fontName, style, size));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.drawString(text, x, y);
    }

    public int getTextWidth(String text, int style, int size) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        Font font = new Font(fontName, style, size);

        FontRenderContext frc = g.getFontRenderContext();
        Rectangle2D box = font.getStringBounds(text, frc);
        return (int)(box.getMaxX() - box.getMinX());
/*
		FontMetrics metrics = g.getFontMetrics(font);
		if (metrics == null)
			return 0;
		return metrics.stringWidth(text);
*/
    }

    public void arc(int x, int y, int size, String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(getColour(colour));
        g.drawArc(x, y, size, size, -45, 270);
    }

    /**
     * Draw an empty circle at the specified point with a given radius.
     *
     * @param x			X position of centre.
     * @param y			Y position of centre.
     * @param radius	Radius in pixels.
     * @param colour	Colour.
     */
    public void circleOutline(int x, int y, int radius, String colour) {
        circleOutline(x, y, radius, colour, (float) 1.0);
    }

    /**
     * Draw an empty circle at the specified point with a given radius and thickness
     *
     * @param x			X position of centre.
     * @param y			Y position of centre.
     * @param radius	Radius in pixels.
     * @param colour	Colour.
     * @param width		Width of outline.
     */
    public void circleOutline(int x, int y, int radius, String colour, float width) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(getColour(colour));
        g.setStroke(new BasicStroke(width));
        g.drawArc(x-radius, y-radius, radius * 2, radius * 2, 0, 360);
    }

    private String format = "png";

    public void save(File path, Image image) throws IOException {
        int 			type = BufferedImage.TYPE_INT_RGB;
        BufferedImage	bimage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), type);

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);

        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        //JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        //encoder.encode(bimage);
        ImageIO.write(bimage, format, out);
    }

    public void save(File path, BufferedImage bimage) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        //JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        //encoder.encode(bimage);
        ImageIO.write(bimage, format, out);
    }

    public void save(File path) throws IOException {
        save(path, true);
    }

    public void save(File path, boolean alpha) throws IOException {
        BufferedImage bimage = getBufferedImage(alpha);

        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        ImageIO.write(bimage, format, out);
    }

    public ByteArrayOutputStream save() throws IOException {
        return save(true);
    }

    public ByteArrayOutputStream save(boolean alpha) throws IOException {
        BufferedImage				bimage = getBufferedImage(alpha);
        ByteArrayOutputStream		out = new ByteArrayOutputStream();

        ImageIO.write(bimage, format, out);

        return out;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int,
     *      int, int, int)
     */
    public boolean imageUpdate(Image img, int info, int x, int y, int width,
                               int height) {
        if ((info & ImageObserver.FRAMEBITS) > 0) {
            return false;
        }
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * Draw a regular filled hexagon of a given height.
     *
     * @param x			X coordinate of left vertex.
     * @param y			Y coordinate of top edge.
     * @param h			Height of hexagon.
     * @param colour	Fill colour of hexagon.
     */
    public void hexByHeight(int x, int y, int h, String colour) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(getColour(colour));

        double ROOT3 = Math.sqrt(3);

        int[]	xp = new int[6];
        int[]	yp = new int[6];

        // Height of the hexagon is 'h'
        double r = (h * 1.0) / ROOT3;
        // x offset between top right vertex and right vertex is 'o'
        double	o = r / 2;

        xp[0] = (int) (x + o);		// Done
        yp[0] = (int) y;			// Done

        xp[1] = (int) (x + o + r);	// Done
        yp[1] = (int) y;			// Done

        xp[2] = (int) (x + r * 2); 	// Done
        yp[2] = (int) (y + h / 2);		// Done

        xp[3] = (int) (x + o + r);	// Done
        yp[3] = (int) (y + h);	// Done

        xp[4] = (int) (x + o);
        yp[4] = (int) (y + h);	// Done

        xp[5] = (int) x;
        yp[5] = (int) (y + h / 2);

        g.fillPolygon(xp, yp, 6);
    }


}
