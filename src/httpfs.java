import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class httpfs {
    public static void main(String[] args) {
        boolean v = false;
        int p = 8080;
        String d = Paths.get(".").toAbsolutePath().normalize().toString(); // get current working directory

        // check if user needs any help
        try {
            for (String arg : args) {
                if (arg.contains("help")) {
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
                        d = Paths.get(args[i + 1]).toAbsolutePath().normalize().toString();
                        if(Files.notExists(Paths.get(d))) {
                            File file = new File(d);
                            file.mkdir();
                            System.out.println("New directory created at " + d);
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
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            boolean isRequestGet = false;

            int read = -1;
            byte[] buffer = new byte[5*1024];
            byte[] readData;
            StringBuilder request = new StringBuilder();
            String readDataText;

            try {
                while ((read = clientSocket.getInputStream().read(buffer)) >= 0) {
                    readData = new byte[read];
                    System.arraycopy(buffer, 0, readData, 0, read);
                    readDataText = new String(readData,"UTF-8");
                    request.append(readDataText);

                    if(readDataText.contains("GET")) {
                        isRequestGet = true;
                        break;
                    }
                    else if(readDataText.contains("\r\n\r\n")) {
                        break;
                    }
                    else {
                        out.println(HttpResponseCodes.BAD_REQUEST.getResponse(""));
                        out.flush();
                        out.close();
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                out.println(HttpResponseCodes.INTERNAL_SERVER_ERROR.getResponse(""));
                out.flush();
                out.close();
                System.exit(0);
            }

            String[] requestData = request.toString().split("\r\n");
            String[] methodPathData = requestData[0].split(" ");
            String userSpecifiedDir = d + Paths.get(methodPathData[1]).normalize().toString(); // Append d for secure access
            if(methodPathData[1].contains("..")) { // Secure access
                out.println(HttpResponseCodes.FORBIDDEN.getResponse(""));
                out.flush();
                out.close();
                System.exit(0);
            }

            File file = new File(userSpecifiedDir);
            Path path = file.toPath();

            String body = "";
            if(isRequestGet) {
                if(Files.isDirectory(path)) {
                    body = "There are " + file.listFiles().length + " in the directory.";
                }
                else if (Files.isRegularFile(path)) {
                    body = FileIO.read(userSpecifiedDir);
                }
                else {
                    out.println(HttpResponseCodes.NOT_FOUND.getResponse(""));
                    out.flush();
                    out.close();
                    System.exit(0);
                }
            }
            else {
                if(!Files.isDirectory(path)) {
                    String data = request.substring(request.indexOf(("\r\n\r\n"))).trim();
                    FileIO.write(userSpecifiedDir, data);
                }
                else {
                    out.println(HttpResponseCodes.FORBIDDEN.getResponse(""));
                    out.flush();
                    out.close();
                    System.exit(0);
                }
            }

            out.println(HttpResponseCodes.OK.getResponse(body));
            in.close();
            out.flush();
            out.close();

        }
        catch (IOException e) {
            System.out.println("Error creating server socket, please verify that port number is valid");
            System.exit(0);
        }
    }
}
