package mst;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class SecondOrderProjectiveSocketServer {
    /**
     * arg[0]=modelfile
     * arg[1]=port
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Usage: SecondOrderProjectiveSocketServer <model-file> <port>");
            System.exit(1);
        }
        final String modelFile = args[0].trim();
        final int port = Integer.parseInt(args[1].trim());

        //INITIALIZE PARSER
        final ParserOptions options = new ParserOptions(new String[] {
            "test",
            "separate-lab",
            "model-name:" + modelFile,
            "decode-type:proj",
            "order:2",
            "format:CONLL"
        });
        final DependencyPipe pipeline = new DependencyPipe2O(options);
        final DependencyParser parser = new DependencyParser(pipeline, options);
        System.err.print("\tLoading model for Dependency Parser...\n");
        parser.loadModel(options.modelName);
        System.err.println("done.");
        pipeline.printModelStats(parser.params);
        pipeline.closeAlphabets();

        // Start server and run forever
        final ServerSocket parseServer = new ServerSocket(port);
        while (true) {
            System.err.println("Waiting for Connection on Port: "+port);
            try {
                final Socket clientSocket = parseServer.accept();
                System.err.println("Connection Accepted From: " + clientSocket.getInetAddress());
                final BufferedReader socketIn =
                        new BufferedReader(new InputStreamReader(new DataInputStream(clientSocket.getInputStream())));
                final PrintWriter socketOut =
                        new PrintWriter(new PrintStream(clientSocket.getOutputStream()));
                String inputLine;
                while ((inputLine = socketIn.readLine()) != null) {
                    if(inputLine.trim().equals("*"))
                        break;
                    final String output = parser.parsePosTaggedLine(inputLine);
                    socketOut.print(output);
                    socketOut.flush();
                }
                socketOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
