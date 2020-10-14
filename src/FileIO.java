import java.io.*;
import java.util.Scanner;

public class FileIO {

    public static void write(String fileName, String content) {
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(fileName));
            out.write(content);
            out.close();
        } catch (IOException e) {
            System.out.println("Error! Could not write to file " + fileName);
            System.exit(0);
        }
    }

    public static String read(String fileName) {
        StringBuilder body = new StringBuilder();
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                body.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error! Could not read file.");
            System.exit(0);
        }
        return body.toString();
    }
}
