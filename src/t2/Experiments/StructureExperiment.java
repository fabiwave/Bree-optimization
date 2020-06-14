package t2.Experiments;

import t2.BinarySearchStructures.BinarySearch;
import t2.BtreeStructures.Btree;

import java.io.IOException;

public class StructureExperiment {

    public static void main(String[] args) throws IOException {

        /** Creation of the Btree and value search */
        System.out.println("-- Creating btree --");
        System.out.println("- Btree structure -");
        Btree tree = new Btree(4);
        for (int i = 0; i < 10; i++) {
            tree.add_value(i);
        }
        tree.read_file();
        /** To change the value to search in the tree change the parameter in the next method*/
        System.out.println("Result of search: " + tree.search_in_btree(1));
        tree.close_dm();

        /** Creation of the file for BinarySearch and value search */
        System.out.println("-- Creating file for binary search --");
        System.out.println("- File structure -");
        BinarySearch binary = new BinarySearch("./binary-search.txt", 10, 4);
        binary.generate_numbers();
        binary.readfile();
        /** To change the value to search in the binary search change the parameter in the next method*/
        System.out.println("Result of search: " + binary.binary_search(1));
        binary.close_file();

    }

}
