package ImageCompression;

import java.awt.Color;
import java.awt.image.BufferedImage;



public class BTreeTriangularCoding {


    private BufferedImage image;
    private BTreeNode root;
    private int triangleSize;


    public BTreeTriangularCoding(BufferedImage image, int triangleSize) {
        this.image = image;

        if (triangleSize <= 0 || triangleSize > Math.min(image.getWidth(), image.getHeight())) {
            throw new IllegalArgumentException("Invalid triangle size");
        }
        
        this.triangleSize = triangleSize;
    }


    public void compress() {
        root = constructBTree(0, 0, image.getWidth(), image.getHeight(), true);
    }



    private BTreeNode constructBTree(int x, int y, int width, int height, boolean isLeft) {
        if (width <= triangleSize || height <= triangleSize) {
            int totalPixels = 0;
            int totalRed = 0, totalGreen = 0, totalBlue = 0;

            for (int i = x; i < x + width; i++) {
                for (int j = y; j < y + height; j++) {
                    if (i < image.getWidth() && j < image.getHeight()) {
                        Color pixelColor = new Color(image.getRGB(i, j));
                        totalRed += pixelColor.getRed();
                        totalGreen += pixelColor.getGreen();
                        totalBlue += pixelColor.getBlue();
                        totalPixels++;
                    }
                }
            }

            int avgRed = totalRed / totalPixels;
            int avgGreen = totalGreen / totalPixels;
            int avgBlue = totalBlue / totalPixels;
            Color avgColor = new Color(avgRed, avgGreen, avgBlue);

            return new BTreeNode(avgColor);
        } else {
            int halfWidth = width / 2;
            int halfHeight = height / 2;

            BTreeNode node = new BTreeNode(null);
            if (isLeft) {
                node.leftChild = constructBTree(x, y, halfWidth, height, true);
                node.rightChild = constructBTree(x + halfWidth, y, width - halfWidth, height, false);
            } else {
                node.leftChild = constructBTree(x, y, width, halfHeight, true);
                node.rightChild = constructBTree(x, y + halfHeight, width, height - halfHeight, false);
            }

            return node;
        }
    }

    

    public BufferedImage decompress() {
        BufferedImage decompressedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        reconstructImage(decompressedImage, root, 0, 0, decompressedImage.getWidth(), decompressedImage.getHeight(), true);
        return decompressedImage;
    }



    private void reconstructImage(BufferedImage image, BTreeNode node, int x, int y, int width, int height, boolean isLeft) {
        if (node.leftChild == null && node.rightChild == null) {
            Color color = node.averageColor;

            for (int i = x; i < x + width; i++) {
                for (int j = y; j < y + height; j++) {
                    if (i < image.getWidth() && j < image.getHeight()) {
                        image.setRGB(i, j, color.getRGB());
                    }
                }
            }
        } else {
            int halfWidth = width / 2;
            int halfHeight = height / 2;

            if (isLeft) {
                reconstructImage(image, node.leftChild, x, y, halfWidth, height, true);
                reconstructImage(image, node.rightChild, x + halfWidth, y, width - halfWidth, height, false);
            } else {
                reconstructImage(image, node.leftChild, x, y, width, halfHeight, true);
                reconstructImage(image, node.rightChild, x, y + halfHeight, width, height - halfHeight, false);
            }
        }
    }
}