package VideoCompress;

import java.awt.image.BufferedImage;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.scale.AWTUtil;

public class SortedImage implements Comparable<SortedImage> {

	public final double timestamp;
	public final BufferedImage data;

	public SortedImage(PictureWithMetadata p) {
		data = AWTUtil.toBufferedImage(p.getPicture(), p.getOrientation());
		timestamp = p.getTimestamp();
	}

	@Override
	public int compareTo(SortedImage o2) {
		return Double.compare(timestamp, o2.timestamp);
	}

}







// {
// TreeMap<Double, BufferedImage> reorderBuffer = new TreeMap<Double, BufferedImage>();
// AWTFrameGrab8Bit fg = AWTFrameGrab8Bit.createAWTFrameGrab8Bit(NIOUtils.readableFileChannel("filename"));
// PictureWithMetadata8Bit pmd = fg.getNativeFrameWithMetadata();
// // Either convert it to BuffferImage right away or clone using Picture8Bit.cloneCropped() method
// // the underlying buffer will be reused for the next frame.
// BufferedImage img = pmd.getPicture() == null ? null : AWTUtil.toBufferedImage8Bit(pmd.getPicture());
// reorderBuffer.put(pmd.getTimestamp(), img);

// // Allow for 5 frames in reorder buffer, that will cover the most common case of I-B-B-B-P
// if (reorderBuffer.size() >= 5) {
//   double leastPts = reorderBuffer.keySet().iterator().next();
//   BufferedImage toOutput = reorderBuffer.get(leastPts);
// }

// }