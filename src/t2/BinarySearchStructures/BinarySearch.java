package t2.BinarySearchStructures;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class BinarySearch {
    private final Path generator_path;
    private final int b;
    public int n;
    private boolean open;
    private RandomAccessFile file;
    private int int_size;
    private int current_position;
    private int end_of_file;


    public BinarySearch(String generator_path, int n, int b) throws IOException {
        this.int_size = Integer.BYTES;
        this.generator_path = Paths.get(generator_path).toAbsolutePath();
        this.b = 2 * b;
        this.n = n;
        try {
            Files.createFile(this.generator_path);
            System.out.println("File for binary search created");
        } catch (FileAlreadyExistsException e) {
            // We overwrite the existing file
            Files.delete(this.generator_path);
            Files.createFile(this.generator_path);
        }
        this.current_position = 0;
        this.end_of_file = 0;
    }

    public void generate_numbers() throws IOException {
        this.open_file();
        ByteBuffer buffer = ByteBuffer.allocate(this.n * int_size);
        for (int i = 0; i < this.n; i++) {
            buffer.putInt(i);
            this.current_position++;
        }
        file.write(buffer.array());
        this.end_of_file = this.current_position;
        file.seek(0);

    }

    public boolean binary_search(int value) throws IOException {
        int index = -1;
        int first = 0;
        int last = this.n - 1;

        while (first <= last) {

            int mid = ((last - first) / 2) + first;
            set_current_position(mid);
            int[] array = decompress_data(current_position);
            int candidate = array[0];
            if (candidate == value) {
                index = mid;
                break;
            } else {
                if (value < candidate) {
                    last = mid - 1;
                } else {
                    first = mid + 1;
                }
            }
        }
        return (index  != -1);
    }

    public void set_current_position(int x) {
        this.current_position = x;
    }

    public int[] decompress_data(int target_position) throws IOException {
        if (!this.open) {
            throw new IOException("File isn't open yet");
        }
        file.seek(((long) target_position) * int_size);
        byte[] raw_data;
        int[] real_data;
        if(target_position + this.b > this.n){
            int size = this.n - target_position;
            raw_data = new byte[size * int_size];
            real_data = new int[size];
        }
        else{
            raw_data = new byte[int_size * this.b];
            real_data = new int[this.b];

        }
        file.readFully(raw_data);
        ByteBuffer data = ByteBuffer.wrap(raw_data);
        data.position(0);

        for (int i = 0; i < real_data.length; i++) {
            real_data[i] = data.getInt();
        }
        real_data = Arrays.copyOf(real_data, real_data.length);
        return real_data;

    }

    public void open_file() throws IOException {
        this.file = new RandomAccessFile(this.generator_path.toFile(), "rwd");
        this.open = true;
    }

    public void close_file() throws IOException {
        this.file.close();
        this.open = false;
    }

    public void readfile() throws IOException {
        if (!this.open) {
            throw new IOException("File isn't open yet");
        }
        int target_position  = 0;
        file.seek(((long) target_position) * int_size);
        byte[] raw_data = new byte[n * int_size];
        int[] real_data = new int[n];
        file.readFully(raw_data);
        ByteBuffer data = ByteBuffer.wrap(raw_data);
        data.position(0);

        for (int i = 0; i < real_data.length; i++) {
            real_data[i] = data.getInt();
            System.out.println(i);
        }
    }

}
