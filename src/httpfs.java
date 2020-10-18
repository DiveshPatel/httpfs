import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
                            file.mkdirs();
                            System.out.println("New directory created at " + d);
                        }
                        break;
                    default:
                        if(!(args[i-1].equals("-p") || args[i-1].equals("-d"))) {
                            throw new Exception();
                        }
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid command");
            Info.help();
            System.exit(0);
        }

        // Create the server socket
        try {
            System.out.println("Working directory: " + d);
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
                        HttpResponseCodes.BAD_REQUEST.printResponse("", out, v);
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                HttpResponseCodes.INTERNAL_SERVER_ERROR.printResponse("", out, v);
                System.exit(0);
            }

            if(v) {
                System.out.println("Client Request: " + request.toString());
            }

            String[] requestData = request.toString().split("\r\n");
            String[] methodPathData = requestData[0].split(" ");
            if(methodPathData[1].contains("..")) { // Secure access
                HttpResponseCodes.FORBIDDEN.printResponse("", out, v);
                System.exit(0);
            }


            String userSpecifiedDir = d + Paths.get(methodPathData[1]).normalize().toString(); // Append d for secure access
            File file = new File(userSpecifiedDir);
            Path path = file.toPath();

            String body = "";
            if(isRequestGet) {
                if(v) {
                    System.out.println("Performing GET Request: " + userSpecifiedDir);
                }
                if(Files.isDirectory(path)) {
                    ArrayList<String> fileList = new ArrayList<>();
                    for(File f: file.listFiles()) {
                        if (f.isDirectory()) {
                            fileList.add(f.getName() + "/");
                        }
                        else {
                            fileList.add(f.getName());
                        }
                    }
                    body = fileList.toString().isEmpty()? "No Files Found" : "Files: " + fileList.toString();
                }
                else if (Files.isRegularFile(path)) {
                    body = FileIO.read(userSpecifiedDir);
                }
                else {
                    HttpResponseCodes.NOT_FOUND.printResponse("", out, v);
                    System.exit(0);
                }
            }
            else {
                if(v) {
                    System.out.println("Performing POST Request: " + userSpecifiedDir);
                }
                if(!Files.isDirectory(path)) {
                    String data = request.substring(request.indexOf(("\r\n\r\n"))).trim();
                    FileIO.write(userSpecifiedDir, data);
                    if(v)
                        System.out.println("With Data: " + data);
                }
                else {
                    HttpResponseCodes.FORBIDDEN.printResponse("", out, v);
                    System.exit(0);
                }
            }

            HttpResponseCodes.OK.printResponse(body, out, v);
            in.close();
        }
        catch (IOException e) {
            System.out.println("Error creating server socket, please verify that port number is valid");
            System.exit(0);
        }
    }
}
