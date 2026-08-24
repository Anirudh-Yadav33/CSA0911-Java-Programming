import java.io.FileReader;
import java.io.IOException;

class ReadFile {
    public static void main(String[] args) {
        try {
            FileReader f = new FileReader("data.txt");

            int ch;
            while ((ch = f.read()) != -1) {
                System.out.print((char) ch);
            }

            f.close();
        } catch (IOException e) {
            System.out.println("Error occurred.");
        }
    }
}