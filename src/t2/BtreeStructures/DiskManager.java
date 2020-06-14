package t2.BtreeStructures;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class DiskManager {
    private final Path bnode_path;
    private final Btree btree;
    private int current_file_length;
    private final int int_size;
    private RandomAccessFile file;
    private boolean open;

    public DiskManager(String relative_path, Btree btree) throws IOException {
        this.int_size = Integer.BYTES;
        this.btree = btree;
        this.bnode_path = Paths.get(relative_path).toAbsolutePath();
        try {
            Files.createFile(this.bnode_path);
        } catch (FileAlreadyExistsException e) {
            Files.delete(this.bnode_path);
            Files.createFile(this.bnode_path);
        }
        this.current_file_length = 0;
    }

    public void compress_node(
            Bnode node, int target_position) throws IOException {
        if (!this.open) {
            throw new IOException("File isn't open yet");
        }
        int node_size = btree.get_block_size() * int_size;
        int[] children = node.get_children();
        int[] values = node.get_values();
        ByteBuffer buffer = ByteBuffer.allocate(node_size);
        for (int i = 0; i < btree.get_block_size() / 2; i++) {
            buffer.putInt(values[i]);
        }
        for (int i = 0; i < btree.get_block_size() / 2; i++) {
            buffer.putInt(children[i]);
        }
        file.seek(((long) target_position) * int_size);
        file.write(buffer.array());
    }

    public int compress_node_to_end(Bnode node) throws IOException {
        int pos = this.current_file_length;
        compress_node(node, pos);
        this.current_file_length += btree.get_block_size();
        return pos;
    }

    public Bnode decompress_node(int target_position) throws IOException {
        if (!this.open) {
            throw new IOException("File isn't open yet");
        }
        file.seek(((long) target_position) * int_size);
        byte[] raw_data = new byte[int_size * btree.get_block_size()];
        file.readFully(raw_data);
        ByteBuffer data = ByteBuffer.wrap(raw_data);
        data.position(0);
        int[] real_data = new int[btree.get_block_size()];
        for (int i = 0; i < real_data.length; i++) {
            real_data[i] = data.getInt();
        }
        real_data = Arrays.copyOf(real_data, real_data.length);
        return new Bnode(real_data, this.btree);
    }

    public void open_file() throws IOException {
        this.file = new RandomAccessFile(this.bnode_path.toFile(), "rwd");
        this.open = true;
    }

    public void close_file() throws IOException {
        this.file.close();
        this.open = false;
    }

    protected void read_file() throws IOException {
        int current_loc = 0;
        while (current_loc < this.current_file_length) {
            decompress_node(current_loc).print_self();
            current_loc += this.btree.get_block_size();
        }
    }

}
