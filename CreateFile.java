import java.io.FileWriter;
import java.io.IOException;

class CreateFile {
    public static void main(String[] args) {
        try {
            FileWriter f = new FileWriter("data.txt");

            f.write("Hello, this is my first file.");
            f.write("\nWelcome to Java File Handling.");

            f.close();

            System.out.println("Data written successfully.");
        } catch (IOException e) {
            System.out.println("Error occurred.");
        }
    }
}