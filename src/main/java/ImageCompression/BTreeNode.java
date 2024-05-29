package ImageCompression;


import java.awt.Color;


class BTreeNode {
    Color averageColor;
    BTreeNode leftChild;
    BTreeNode rightChild;

    public BTreeNode(Color color) {
        this.averageColor = color;
        this.leftChild = null;
        this.rightChild = null;
    }
}