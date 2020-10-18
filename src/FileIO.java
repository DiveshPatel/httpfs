import java.io.*;
import java.util.Scanner;

public class FileIO {

    public static void write(String fileName, String content) throws IOException {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        BufferedWriter out;
        out = new BufferedWriter(new FileWriter(fileName));
        out.write(content);
        out.close();
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
