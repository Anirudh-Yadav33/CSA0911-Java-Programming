import java.io.*;

class CountFile {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("data.txt"));

            int characters = 0;
            int lines = 0;
            String line;

            while ((line = br.readLine()) != null) {
                characters += line.length();
                lines++;
            }

            br.close();

            System.out.println("Number of characters: " + characters);
            System.out.println("Number of lines: " + lines);
        } catch (IOException e) {
            System.out.println("Error occurred.");
        }
    }
}