import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class httpfs {
    public static void main(String[] args) {

        boolean v = false;
        int p = 8080;
        String d = Paths.get(".").toAbsolutePath().normalize().toString(); // get current working directory

        // check if user needs any help
        try {
            for(int i=0; i < args.length; ++i) {
                if(args[i].contains("help")) {
                    Info.help();
                    System.exit(0);
                }
            }
        }
        catch(ArrayIndexOutOfBoundsException exception) {
            Info.help();
            System.exit(0);
        }

        // Parse the command line args
        try {
            for (int i = 0; i < args.length; ++i) {
                switch (args[i]) {
                    case "-v":
                        v = true;
                        break;
                    case "-p":
                        p = Integer.parseInt(args[i + 1]);
                        break;
                    case "-d":
                        d = args[i + 1];
                        String directory = Paths.get(".").toAbsolutePath().normalize().toString() + "/" + d;
                        if(Files.notExists(Paths.get(directory))) {
                            File file = new File(directory);
                            file.mkdir();
                            System.out.println("New directory created at " + directory);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid command");
            Info.help();
            System.exit(0);
        }

        // Create the server socket
        try {
            ServerSocket serverSocket = new ServerSocket(p);
            System.out.println("Listening on port " + p);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connected established with " +  clientSocket.toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder request = new StringBuilder();
            String inputLine = in.readLine();
            request.append(inputLine + "\n");

            boolean isRequestGet = inputLine.contains("GET") ? true : false;

            if(!isRequestGet) {
                while ((inputLine = in.readLine()) != null) {
                    request.append(inputLine + "\n");
                    System.out.println(inputLine + "hi");
                    if (inputLine.contains("Content-Length"))
                        break;
                }
            }
            System.out.println("");
            System.out.println(request.toString());

        } catch (IOException e) {
            System.out.println("Error creating server socket, please verify that port number is valid");
            System.exit(0);
        }
    }
}
