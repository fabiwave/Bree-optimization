package t2.BtreeStructures;

import java.io.IOException;
import java.util.Arrays;

public class Bnode {

    // Number of values in the node
    protected int occupancy;
    // Values in the node
    protected int[] values;
    // Children of this node
    protected int[] children;
    // Btree that has this node
    protected Btree btree;
    // Max amount of values to save in a node
    protected int max_values;
    // If this node is a leaf
    protected boolean is_leaf;


    public Bnode(int n, Btree btree) {
        this.max_values = n / 2 - 1;
        this.occupancy = 0;
        // Values is of length max_value + 1 to save an overflow
        this.values = new int[n / 2];
        // Children has an extra space to save an overflow
        this.children = new int[n / 2 + 1];
        // No value or pointer is represented by -1
        Arrays.fill(this.values, -1);
        Arrays.fill(this.children, -1);
        // By default, every node created this way is a leaf
        this.is_leaf = true;
        this.btree = btree;
    }

    protected Bnode(int[] compressed_data, Btree btree) {
        // Restore values according to the btree data
        int block_size = btree.get_block_size();
        this.max_values = block_size / 2 - 1;
        this.btree = btree;
        this.values = new int[block_size / 2];
        this.children = new int[block_size / 2 + 1];
        // Fill the children and values with default value
        Arrays.fill(this.values, -1);
        Arrays.fill(this.children, -1);
        this.occupancy = 0;
        // Copy the values in the given data, increasing occupancy
        for (int i = 0; i < block_size / 2; i++) {
            this.values[i] = compressed_data[i];
            if (this.values[i] != -1)
                this.occupancy++;
        }
        // Set as leaf by default
        this.is_leaf = true;
        // Copy the children in the given data, if there is at least
        // one, the node is not a leaf
        for (int i = 0; i < block_size / 2; i++) {
            this.children[i] = compressed_data[i + block_size / 2];
            if (this.children[i] != -1) {
                this.is_leaf = false;
            }
        }
    }

    public int[] get_values() {
        return this.values;
    }

    public int[] get_children() {
        return this.children;
    }

    public boolean add_value(int value) throws IOException {
        // Returns true when there's an overflow
        boolean result = false;
        // If this is a leaf, add it to values
        if (this.is_leaf) {
            // When an overflow occurs
            if (this.add_direct_value(value)) {
                result = true;
            }
        } else {
            // When this isn't a leaf, add the value to the
            // corresponding child
            Bnode child = null;
            boolean found = false;
            int child_pos = 0;
            int i = 0;
            // Search the corresponding child
            while (i < this.occupancy) {
                if (value < this.values[i]) {
                    child_pos = this.children[i];
                    found = true;
                    break;
                }
                i++;
            }
            // If it hasn't been found yet, the value belongs
            // in the last child
            if (!found) {
                child_pos = this.children[i];
            }
            // Decompress the child
            child = this.btree.decompress_child(child_pos);
            // Add it to the child and check if it overflows
            result = child.add_value(value);
            // If the child overflows, divide it
            if (result) {
                // If divide_node is true, this node has an
                // overflow, let it's parent handle it
                result = divide_node(child);
            }
            // Save to disk the updated child
            this.btree.update_child(child, child_pos);
        }
        // Return if this node has an overflow
        return result;
    }

    protected boolean divide_node(Bnode node) throws IOException {
        // Get the block size for a new node
        int block_size = btree.get_block_size();
        // Get the median value and index of the node to divide
        int divider = node.values.length / 2;
        int median = node.values[divider];
        // Erase the median value, as it will be added to this node
        node.values[divider] = -1;
        node.occupancy = divider;
        // Create data for the new node to be created
        int[] new_node_data = new int[block_size];
        Arrays.fill(new_node_data, -1);
        // Copy the values after the median to the new node's data
        int aux;
        for (int i = 0; i < divider - 1; i++) {
            aux = new_node_data[i];
            new_node_data[i] = node.values[divider + i + 1];
            node.values[divider + i + 1] = aux;
        }
        // Copy the children after the median + 1 to the new node's data
        for (int i = 1; i < divider + 1; i++) {
            aux = new_node_data[(block_size / 2) + i - 1];
            new_node_data[(block_size / 2) + i - 1] = node.children[divider + i];
            node.children[divider + i] = aux;
        }
        // Find where the median goes in this node
        int median_pos = (Arrays.binarySearch(
                this.values, 0, this.occupancy, median) + 1) * -1;
        // Move the children of this node to make room for the new one
        System.arraycopy(this.children, median_pos + 1,
                this.children, median_pos + 2,
                this.children.length - median_pos - 2);
        // Create the new child from the collected data
        Bnode new_child = new Bnode(new_node_data, this.btree);
        // Add the new child, creating a displacement between the
        // values and the nodes
        this.children[median_pos + 1] =
                this.btree.add_new_child(new_child);
        // Update the modified child on disk
        this.btree.update_child(
                node, this.children[median_pos]);
        // Add the median to this node, fixing the displacement
        // between nodes and values created before, and return if
        // this node overflowed
        return this.add_direct_value(median);

    }

    private boolean add_direct_value(int value) {
        // Add the value after all occupied
        this.values[this.occupancy] = value;
        // Update the occupancy
        this.occupancy++;
        // Reorder the values
        Arrays.sort(this.values, 0, this.occupancy);
        // Return if this node overflowed
        return this.occupancy > this.max_values;
    }

    public void print_self() {
        System.out.println("----------------");
        for (int i = 0; i < this.max_values; i++) {
            System.out.print("| ");
            System.out.print(this.values[i]);
        }
        System.out.println("|");
        for (int i = 0; i < this.max_values + 1; i++) {
            System.out.print(this.children[i]);
            System.out.print("  ");
        }
        System.out.println("");
        System.out.println("----------------");
    }

    public int search_in_node(int value) throws IOException {
        int index = -1;

        if (this.is_leaf) {
            for (int i = 0; i < this.values.length; i++) {
                if (this.values[i] == value) {
                    index = i;
                }
            }
        } else {
            int child_pos = -1;
            for (int i = 0; i < this.values.length; i++) {
                if (value < this.values[i]) {
                    child_pos = i;
                    break;
                } else if (value == this.values[i]) {
                    return i;
                }
            }
            if (child_pos == -1) {
                child_pos = this.occupancy;
            }
            Bnode child = this.btree.decompress_child(this.children[child_pos]);
            return child.search_in_node(value);
        }
        return index;
    }
}
