package mst;


import java.io.*;
import java.net.*;

/*
 * args[0]=input file
 * args[1]=output file
 * args[2]=server hostname
 * args[3]=server port
 */
public class DependencyClient {
    public static void main(String[] args) throws IOException {
        if(args.length != 4) {
            System.err.println("Usage: DependencyClient <input-pos-tagged-file> <output-conll-formatted-file> <server-hostname> <server-port>");
            System.exit(1);
        }
        final String inputFile = args[0];
        final String outputFile = args[1];
        final String hostname = args[2].trim();
        final int port = Integer.parseInt(args[3].trim());
        final Socket kkSocket = new Socket(hostname, port);
        final BufferedReader socketIn = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        final PrintWriter socketOut = new PrintWriter(kkSocket.getOutputStream(),true);

        // read input file and send to server
        final BufferedReader inputFileReader = new BufferedReader(new FileReader(inputFile));
        String inputLine;
        while ((inputLine = inputFileReader.readLine()) != null) {
            socketOut.println(inputLine);
            socketOut.flush();
        }
        socketOut.println("*");  // signal end of request
        inputFileReader.close();

        // read response from server and copy to output file
        final BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(outputFile));
        String outputLine;
        while ((outputLine = socketIn.readLine()) != null) {
            outputFileWriter.write(outputLine.trim() + "\n");
            System.err.print(".");
        }
        socketOut.close();
        socketIn.close();
        kkSocket.close();
        outputFileWriter.flush();
        outputFileWriter.close();
    }
}
