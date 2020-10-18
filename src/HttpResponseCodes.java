import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public enum HttpResponseCodes {
    OK(200, " OK"),
    BAD_REQUEST(400, " BAD REQUEST"),
    FORBIDDEN(403, " FORBIDDEN"),
    NOT_FOUND(404, " NOT FOUND"),
    INTERNAL_SERVER_ERROR(500, " INTERNAL SERVER ERROR");

    private int code;
    private String message;
    HttpResponseCodes(int errorCode, String errorMessage) {
        code = errorCode;
        message = errorMessage;
    }

    public String toString() {
        return "HTTP/1.0 " + code + message;
    }

//    HTTP/1.1 200 OK
//    Date: Mon, 27 Jul 2009 12:28:53 GMT
//    Server: Apache/2.2.14 (Win32)
//    Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT
//    Content-Length: 88
//    Content-Type: text/html
//    Connection: Closed

    public void printResponse(String body, PrintWriter out, boolean v) {
        StringBuilder sb = new StringBuilder();
        sb.append(toString() + "\r\n");
        DateFormat df = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss z", Locale.CANADA);
        Date date = new Date(System.currentTimeMillis());
        sb.append(df.format(date) + "\r\n");
        sb.append("Content Length: ");
        sb.append(sb.toString().length() + body.length());
        sb.append("\r\n\r\n");

        if(!body.isEmpty()) {
            sb.append(body);
        }
        out.println(sb.toString());
        out.flush();
        out.close();

        if(v) {
            System.out.println("Sending HTTP Response code: ");
            System.out.println(sb.toString());
        }
    }
}
