package com.example.worker.Indexing;

import java.util.ArrayList;
import java.util.List;

class BTreeNode {

    List<String> keys;
    List<String> documentIds;
    List<BTreeNode> children;
    BTreeNode parent;
    boolean isLeaf;
    BTreeNode nextLeaf;
    BTreeNode prevLeaf;
    String dbName;

    public BTreeNode() {
        this.keys = new ArrayList<>();
        this.documentIds = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = true;
        this.nextLeaf = null;
        this.prevLeaf = null;
        this.parent = null;
    }
}