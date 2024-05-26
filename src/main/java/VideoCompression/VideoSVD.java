package VideoCompression;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgproc;

public class VideoSVD {

    /**
     * 
     * @param videoFilePath string to the video
     * @param k             number of eigen values to be taken for the compression
     */

    public static void compress(String videoFilePath, String outPath ,int k) {

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath);
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outPath, grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setVideoCodec(grabber.getVideoCodec()); 
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setFormat("mp4"); // Output format
        recorder.setFrameRate(grabber.getFrameRate()); // Set frame rate
        recorder.setVideoBitrate(grabber.getVideoBitrate()); // Set video bitrate
    


        
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Mat compressedFrame;
        

        try {


            grabber.start();
            recorder.start();
            
            Frame frame;
            while ((frame = grabber.grab()) != null) {

                RealMatrix U = null, S = null, V = null;
                Mat mat = converter.convert(frame);
                Mat gray = new Mat();
                
                if(mat == null)continue;

        
        
                opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

                int rows = gray.rows();
                int cols = gray.cols();
                
                RealMatrix matrix = to_RealMatrix(mat, rows, cols);
                SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
                U = svd.getU();
                S = svd.getS();
                V = svd.getV();

                compressedFrame = joinMat(U, S, V, k, gray.type());
                

                Mat coloredFrame = new Mat();
                opencv_imgproc.cvtColor(compressedFrame, coloredFrame, opencv_imgproc.COLOR_GRAY2BGR);
                Frame processedFrame = converter.convert(coloredFrame);
                recorder.record(processedFrame);

            }

            grabber.stop();
            recorder.stop();

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

    

}
