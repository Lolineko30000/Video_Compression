package BTree;
import java.util.ArrayList;

public class BTree{
    public ArrayList<BTreeNode> nodes;
    public ArrayList<Boolean> structure;
    // public String structure;

    public BTree(){
        nodes = new ArrayList<>();
        structure = new ArrayList<Boolean>();
    }

    public void addNode(BTreeNode T){
        nodes.add(T);
        structure.add(Boolean.TRUE);
        // structure += "0";
    }

    public void addNode(){
        // structure += "1";
        structure.add(Boolean.FALSE);

    }
}