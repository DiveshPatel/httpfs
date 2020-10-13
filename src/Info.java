public class Info {
    public static void help(){
        System.out.println("httpfs is a simple file server.");
        System.out.println("usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
        System.out.println("-v Prints debugging messages.");
        System.out.println("");
        System.out.println("-p Specifies the port number that the server will listen and serve at.\n" +
                "Default is 8080.");
        System.out.println("");
        System.out.println("-d Specifies the directory that the server will use to read/write\n" +
                "requested files. Default is the current directory when launching the\n" +
                "application.");
    }
}
