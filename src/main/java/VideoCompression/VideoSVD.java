package VideoCompression;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgproc;

public class VideoSVD {

    /**
     * 
     * @param videoFilePath string to the video
     * @param k             number of eigen values to be taken for the compression
     */

    public static void compress(String videoFilePath, int k) {

        FFmpegFrameGrabber grabber;
        OpenCVFrameConverter.ToMat converter;
        CanvasFrame canvas;
        Mat compressedFrame;

        try {
            grabber = new FFmpegFrameGrabber(videoFilePath);
            converter = new OpenCVFrameConverter.ToMat();
            canvas = new CanvasFrame("Processed Video");

            grabber.start();
            canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

            Frame frame;

            while ((frame = grabber.grab()) != null) {

                RealMatrix U = null, S = null, V = null;
                Mat mat = converter.convert(frame);
                Mat gray = new Mat();
                canvas.showImage(frame);


                //int rows = gray.rows();
                //int cols = gray.cols();
//
                //opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
//
                //
//
                //RealMatrix matrix = to_RealMatrix(mat, rows, cols);
                //SVD(matrix, U, S, V);
                //compressedFrame = joinMat(U, S, V, k, gray.type());
//
                //canvas.showImage(converter.convert(compressedFrame));
            }

            grabber.stop();
            canvas.dispose();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 
     * @param U    Result of matrices in the descomposition
     * @param S    Result of matrices in the descomposition
     * @param V    Result of matrices in the descomposition
     * @param k    egen values to be taken
     * @param type auxiliar param to get the tipe of the processed frame
     * 
     * 
     * @return the compressed frame
     */
    private static Mat joinMat(RealMatrix U, RealMatrix S, RealMatrix V, int k, int type) {

        RealMatrix Uk, Sk, Vk;

        if (k > 0) {
            Uk = U.getSubMatrix(0, U.getRowDimension() - 1, 0, k - 1);
            Sk = S.getSubMatrix(0, k - 1, 0, k - 1);
            Vk = V.getSubMatrix(0, V.getRowDimension() - 1, 0, k - 1);
        } else {
            Uk = U;
            Sk = S;
            Vk = V;
        }

        RealMatrix compressedMatrix = Uk.multiply(Sk).multiply(Vk.transpose());

        int rows = compressedMatrix.getRowDimension();
        int cols = compressedMatrix.getColumnDimension();

        Mat compressedFrame = new Mat(rows, cols, type);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                compressedFrame.ptr(i, j).putDouble(compressedMatrix.getEntry(i, j));
            }
        }

        return compressedFrame;
    }

    /**
     * 
     * @param rawData T
     * @param rows
     * @param cols
     * 
     * 
     * @return
     */
    private static RealMatrix to_RealMatrix(Mat rawData, int rows, int cols) {

        double[][] data = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = rawData.ptr(i, j).get() & 0xFF; // Get pixel value
            }
        }

        return MatrixUtils.createRealMatrix(data);
    }

    /**
     * 
     * @param frame the frame bieng processed at the moment
     * 
     * @param U     Variables to pass by refference the result
     * @param S     Variables to pass by refference the result
     * @param V     Variables to pass by refference the result
     */
    private static void SVD(RealMatrix frame, RealMatrix U, RealMatrix S, RealMatrix V) {

        SingularValueDecomposition svd = new SingularValueDecomposition(frame);
        U = svd.getU();
        S = svd.getS();
        V = svd.getV();
    }

}
