import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import rs.pumpkin.open_attachment_handler.exception.InvalidFileTypeException;
import rs.pumpkin.open_attachment_handler.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtilsTest {

    @Test
    void resizeToSquare_ShouldProduceExactSquareForNonSquarePng() throws IOException {
        byte[] source = sampleImage(400, 200, "png");

        byte[] resized = ImageUtils.resizeToSquare(source, "png", 120);

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(resized));
        Assertions.assertNotNull(result);
        Assertions.assertEquals(120, result.getWidth());
        Assertions.assertEquals(120, result.getHeight());
    }

    @Test
    void resizeToSquare_ShouldProduceExactSquareForNonSquareJpg() throws IOException {
        byte[] source = sampleImage(150, 600, "jpg");

        byte[] resized = ImageUtils.resizeToSquare(source, "jpg", 256);

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(resized));
        Assertions.assertNotNull(result);
        Assertions.assertEquals(256, result.getWidth());
        Assertions.assertEquals(256, result.getHeight());
    }

    @Test
    void resizeToSquare_ShouldRejectNonResizableExtension() {
        Assertions.assertThrows(
                InvalidFileTypeException.class,
                () -> ImageUtils.resizeToSquare(new byte[0], "svg", 100)
        );
    }

    @Test
    void resizeToSquare_ShouldRejectNonPositiveEdge() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ImageUtils.resizeToSquare(new byte[0], "png", 0)
        );
    }

    @Test
    void applyExifOrientation_ShouldSwapDimensionsForNinetyDegreeRotations() {
        BufferedImage landscape = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);

        // 6 = rotate 90 CW, 8 = rotate 90 CCW -> width/height swap
        BufferedImage rotatedCw = ImageUtils.applyExifOrientation(landscape, 6);
        Assertions.assertEquals(20, rotatedCw.getWidth());
        Assertions.assertEquals(40, rotatedCw.getHeight());

        BufferedImage rotatedCcw = ImageUtils.applyExifOrientation(landscape, 8);
        Assertions.assertEquals(20, rotatedCcw.getWidth());
        Assertions.assertEquals(40, rotatedCcw.getHeight());
    }

    @Test
    void applyExifOrientation_ShouldKeepDimensionsForNormalAndFlips() {
        BufferedImage landscape = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);

        // 1 = normal, 3 = 180 deg -> dimensions unchanged
        Assertions.assertSame(landscape, ImageUtils.applyExifOrientation(landscape, 1));

        BufferedImage rotated180 = ImageUtils.applyExifOrientation(landscape, 3);
        Assertions.assertEquals(40, rotated180.getWidth());
        Assertions.assertEquals(20, rotated180.getHeight());
    }

    @Test
    void applyExifOrientation_ShouldIgnoreUnknownOrientationCodes() {
        BufferedImage landscape = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);

        Assertions.assertSame(landscape, ImageUtils.applyExifOrientation(landscape, 0));
        Assertions.assertSame(landscape, ImageUtils.applyExifOrientation(landscape, 99));
    }

    @Test
    void isResizable_ShouldRecognizeRasterImageTypes() {
        Assertions.assertTrue(ImageUtils.isResizable("jpg"));
        Assertions.assertTrue(ImageUtils.isResizable("JPEG"));
        Assertions.assertTrue(ImageUtils.isResizable("png"));
        Assertions.assertFalse(ImageUtils.isResizable("heic"));
        Assertions.assertFalse(ImageUtils.isResizable("svg"));
        Assertions.assertFalse(ImageUtils.isResizable(null));
    }

    private byte[] sampleImage(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}
