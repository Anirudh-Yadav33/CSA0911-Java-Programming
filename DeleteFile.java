import java.io.File;
import java.io.IOException;

class DeleteFile {
    public static void main(String[] args) {
        try {
            File f = new File("data.txt");

            if (f.exists()) {
                f.delete();
                System.out.println("File deleted.");
            }

            f.createNewFile();
            System.out.println("File created again.");

        } catch (IOException e) {
            System.out.println("Error occurred.");
        }
    }
}