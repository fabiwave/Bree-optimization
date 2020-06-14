package t2.BtreeStructures;

import java.io.IOException;
import java.util.Arrays;

public class Btree {

    Bnode root;
    int root_pos;
    int height;
    int n;
    DiskManager dm;

    public Btree(int b) throws IOException {
        // Each node saves b children, so it requires 2 * b space on disk
        this.n = 2 * b;
        // Create a new manager
        this.dm = new DiskManager("./bnodes.txt", this);
        // Create the root of the btree
        this.root = new Bnode(n, this);
        // As there's only the root, the height is 1
        this.height = 1;
        // Open the file to save the tree
        this.dm.open_file();
        // Save the root to disk
        this.root_pos = this.add_new_child(this.root);
    }

    public int get_block_size() {
        return this.n;
    }

    public void close_dm() throws IOException {
        this.dm.close_file();
    }

    public void read_file() throws IOException {
        this.dm.read_file();
    }

    public void add_value(int value) throws IOException {
        // If the root has to be divided
        if (this.root.add_value(value)) {
            // Create data for a new node, filled with -1s
            int block_size = this.get_block_size();
            int[] new_node_data = new int[block_size];
            Arrays.fill(new_node_data, -1);
            // Add the current root as the first child
            new_node_data[block_size / 2] = this.root_pos;
            // Create the node
            Bnode new_root = new Bnode(new_node_data, this);
            // Divide the root node with the new node
            boolean result = new_root.divide_node(this.root);
            // If this is true, something went horribly wrong
            if (result) {
                throw new IOException("Something went wrong," +
                        "brand new root is saying it overflowed.");
            }
            // If result is false, the old root has already divided
            // and is a child of the new root, so we change root
            this.root = new_root;
            // We save the new root to disk
            this.root_pos = add_new_child(new_root);
            // And we increase the height of the tree
            this.height++;
        }
        this.update_child(root, root_pos);
    }

    protected Bnode decompress_child(int child_pos) throws IOException {
        return this.dm.decompress_node(child_pos);
    }

    protected int add_new_child(Bnode child) throws IOException {
        return this.dm.compress_node_to_end(child);
    }

    protected void update_child(Bnode child, int child_pos) throws IOException {
        this.dm.compress_node(child, child_pos);
    }

    public boolean search_in_btree(int value) throws IOException {
        int index = root.search_in_node(value);
        return (index != -1);
    }
}