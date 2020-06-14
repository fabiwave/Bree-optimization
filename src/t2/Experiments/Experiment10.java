package t2.Experiments;

import java.io.IOException;
import java.util.HashMap;

import t2.BinarySearchStructures.BinarySearch;
import t2.BtreeStructures.Btree;

public class Experiment10 {
    public static void main(String[] args) throws IOException {
        HashMap<Integer, Long> btree_values = new HashMap<>();
        HashMap<Integer, Long> bb_values = new HashMap<>();
        System.out.println("-- BTREE STRUCTURE --");
        for (int j = 8; j <= 8192; j = j * 2) {
            Btree tree = new Btree(j);
            for (int i = 0; i < 10; i++) {
                tree.add_value(i);
            }
            long init_btree = System.nanoTime();
            boolean result_btree = tree.search_in_btree(1);
            long fin_btree = System.nanoTime();
            long total_btree = fin_btree - init_btree;
            btree_values.put(j, total_btree);
            System.out.println("Search for n = 10 and b = " + j);
            System.out.println("The result of the search was " + result_btree + " and it took " + total_btree + " nanoseconds");
        }

        System.out.println("-- BINARY SEARCH STRUCTURE --");
        for (int j = 8; j <= 8192; j = j * 2) {
            BinarySearch binary = new BinarySearch("./binary-search.txt", 10, j);
            binary.generate_numbers();
            long init_binary = System.nanoTime();
            boolean result_binary = binary.binary_search(1);
            long fin_binary = System.nanoTime();
            long total_binary = fin_binary - init_binary;
            bb_values.put(j, total_binary);
            System.out.println("Search for n = 10 and b = " + j);
            System.out.println("The result of the search was " + result_binary + " and it took " + total_binary + " nanoseconds");
            System.out.println("");
        }
        System.out.println("-- Summary Btree Search --");
        System.out.println(btree_values);
        System.out.println("-- Summary Binary Search --");
        System.out.println(bb_values);
    }
}