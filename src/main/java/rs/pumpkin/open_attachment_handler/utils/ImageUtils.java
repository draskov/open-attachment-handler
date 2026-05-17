package rs.pumpkin.open_attachment_handler.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rs.pumpkin.open_attachment_handler.exception.InternalException;
import rs.pumpkin.open_attachment_handler.exception.InvalidFileTypeException;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageUtils {

    /** Raster image extensions the JDK's ImageIO can both decode and re-encode. */
    private static final Set<String> RESIZABLE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    /** EXIF orientation value meaning "no rotation/flip needed". */
    private static final int ORIENTATION_NORMAL = 1;

    /**
     * Tells whether an attachment with the given extension can be resized. Vector (svg)
     * and formats without a bundled ImageIO codec (heic) are not resizable.
     */
    public static boolean isResizable(String extension) {
        return extension != null && RESIZABLE_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Scales the source image so it fully covers a square of {@code edge} pixels, then
     * center-crops the overflow, producing an exactly {@code edge}x{@code edge} image.
     * <p>
     * The EXIF orientation tag is honoured before resizing, so photos shot in portrait
     * on a phone are not rotated sideways in the result.
     *
     * @param source    raw bytes of the source image
     * @param extension source image extension (jpg, jpeg or png)
     * @param edge      side length, in pixels, of the resulting square image
     * @return bytes of the resized, center-cropped image, encoded in the source format
     */
    public static byte[] resizeToSquare(byte[] source, String extension, int edge) {
        if (!isResizable(extension)) {
            throw new InvalidFileTypeException(String.format(
                    "Image extension '%s' cannot be resized. Resizable extensions are: %s",
                    extension, RESIZABLE_EXTENSIONS
            ));
        }
        if (edge <= 0) {
            throw new IllegalArgumentException("Target square edge must be a positive number of pixels.");
        }

        try {
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(source));
            if (decoded == null) {
                throw new InternalException(
                        "Could not decode image; it may be corrupt or not a supported raster image."
                );
            }

            // ImageIO ignores the EXIF orientation tag, so apply it ourselves first.
            BufferedImage original = applyExifOrientation(decoded, readExifOrientation(source));

            int sourceWidth = original.getWidth();
            int sourceHeight = original.getHeight();

            // Scale so the image fully covers the square, then center-crop the overflow.
            double scale = Math.max((double) edge / sourceWidth, (double) edge / sourceHeight);
            int scaledWidth = (int) Math.ceil(sourceWidth * scale);
            int scaledHeight = (int) Math.ceil(sourceHeight * scale);
            int offsetX = (edge - scaledWidth) / 2;
            int offsetY = (edge - scaledHeight) / 2;

            boolean png = "png".equalsIgnoreCase(extension);
            BufferedImage canvas = new BufferedImage(
                    edge, edge,
                    png ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = canvas.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(original, offsetX, offsetY, scaledWidth, scaledHeight, null);
            graphics.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            String formatName = png ? "png" : "jpg";
            if (!ImageIO.write(canvas, formatName, output)) {
                throw new InternalException("No image writer available for format: " + formatName);
            }
            return output.toByteArray();
        } catch (IOException ex) {
            throw new InternalException(
                    String.format("Error resizing image. Exception message: %s", ex.getMessage()), ex
            );
        }
    }

    /**
     * Reads the EXIF orientation tag (1-8) from the given image bytes. Returns
     * {@link #ORIENTATION_NORMAL} when no readable orientation is present, so callers
     * can treat the result as "rotate by this code" unconditionally.
     */
    private static int readExifOrientation(byte[] source) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(source));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null) {
                Integer orientation = directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
                if (orientation != null && orientation >= 1 && orientation <= 8) {
                    return orientation;
                }
            }
        } catch (Exception ex) {
            // Missing or unreadable metadata is expected (e.g. most PNGs); fall back to normal.
            log.trace("Could not read EXIF orientation; assuming normal orientation. Message: {}",
                    ex.getMessage());
        }
        return ORIENTATION_NORMAL;
    }

    /**
     * Returns a copy of {@code image} with the given EXIF orientation code (1-8) applied,
     * so the pixels are upright. Orientation {@code 1} (or any unknown value) returns the
     * image unchanged. Codes 5-8 rotate by 90 degrees and therefore swap width and height.
     */
    public static BufferedImage applyExifOrientation(BufferedImage image, int orientation) {
        if (orientation <= ORIENTATION_NORMAL || orientation > 8) {
            return image;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        AffineTransform transform = new AffineTransform();

        switch (orientation) {
            case 2: // flip horizontally
                transform.scale(-1.0, 1.0);
                transform.translate(-width, 0);
                break;
            case 3: // rotate 180 degrees
                transform.translate(width, height);
                transform.rotate(Math.PI);
                break;
            case 4: // flip vertically
                transform.scale(1.0, -1.0);
                transform.translate(0, -height);
                break;
            case 5: // transpose: rotate 90 CW then flip horizontally
                transform.rotate(-Math.PI / 2);
                transform.scale(-1.0, 1.0);
                break;
            case 6: // rotate 90 degrees clockwise
                transform.translate(height, 0);
                transform.rotate(Math.PI / 2);
                break;
            case 7: // transverse: rotate 90 CCW then flip horizontally
                transform.scale(-1.0, 1.0);
                transform.translate(-height, 0);
                transform.translate(0, width);
                transform.rotate(3 * Math.PI / 2);
                break;
            case 8: // rotate 90 degrees counter-clockwise
                transform.translate(0, width);
                transform.rotate(3 * Math.PI / 2);
                break;
            default:
                return image;
        }

        boolean dimensionsSwapped = orientation >= 5;
        BufferedImage corrected = new BufferedImage(
                dimensionsSwapped ? height : width,
                dimensionsSwapped ? width : height,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = corrected.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, transform, null);
        graphics.dispose();
        return corrected;
    }
}
