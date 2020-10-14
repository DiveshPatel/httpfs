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
        String s = "C:\\Users\\dives\\Workspace\\httpfs\\src";

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
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

            boolean isRequestGet = false;

            int red = -1;
            byte[] buffer = new byte[5*1024];
            byte[] redData;
            StringBuilder request = new StringBuilder();
            String redDataText;
            while ((red = clientSocket.getInputStream().read(buffer)) > -1) {
                redData = new byte[red];
                System.arraycopy(buffer, 0, redData, 0, red);
                redDataText = new String(redData,"UTF-8");
                request.append(redDataText);
            }

            System.out.println("BORKE");

//            StringBuilder request = new StringBuilder();
//            String inputLine = in.readLine();
//            request.append(inputLine + "\r\n");
//            boolean isRequestGet = false;
//            if(inputLine.contains("GET")) {
//                isRequestGet = true;
//            }
//            else if(inputLine.contains("POST")) {
//                isRequestGet = false;
//            }
//            else
//            {
//                System.out.println("400 Bad Request");
//                Info.help();
//                System.exit(0);
//            }
//
//            if(!isRequestGet) {
//                while ((inputLine = in.readLine()) != null) {
//                    request.append(inputLine + "\r\n");
//                    if (inputLine.contains("Content-Length"))
//                        break;
//                }
//            }
            in.close();

            String[] requestData = request.toString().split("\r\n");
            String[] methodPathData = requestData[0].split(" ");

            System.out.println(request);

            String userSpecifiedDir = d + Paths.get(methodPathData[1]).normalize().toString(); // Append d for secure access
            if(methodPathData[1].contains("..")) { // Secure access
                out.println("HTTP/1.0 403 FORBIDDEN \r\n\r\n Attempted to connect to root directory");
                out.flush();
                out.close();
                System.exit(0);
            }

            File file = new File(userSpecifiedDir);
            Path path = file.toPath();
            if(isRequestGet) {
                if(Files.isDirectory(path)) {
                    out.print("There are " + file.listFiles().length + " in the directory \r\n");
                    out.flush();
                    out.close();
                    System.out.println(file.listFiles().length);
                }
                else if (Files.isRegularFile(path)) {
                    System.out.println(FileIO.read(userSpecifiedDir));
                }
            }
            else {
                if(!Files.isDirectory(path)) {
                    //FileIO.write();
                }
            }

        } catch (IOException e) {
            System.out.println("Error creating server socket, please verify that port number is valid");
            System.exit(0);
        }
    }
}
