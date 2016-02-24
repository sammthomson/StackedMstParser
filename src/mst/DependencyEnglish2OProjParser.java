package mst;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class DependencyEnglish2OProjParser {
    /*
     * arg[0]=modelfile
     * arg[1]=port
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Usage: DependencyEnglish2OProjParser <model-file> <port>");
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
        final DependencyPipe pipe = options.secondOrder ?
                new DependencyPipe2O(options) : new DependencyPipe(options);
        final DependencyParser dp = new DependencyParser(pipe, options);
        System.err.print("\tLoading model for Dependency Parser...\n");
        dp.loadModel(options.modelName);
        System.err.println("done.");
        pipe.printModelStats(dp.params);
        pipe.closeAlphabets();

        // Start server and run forever
        final ServerSocket parseServer = new ServerSocket(port);
        while (true) {
            System.err.println("Waiting for Connection on Port: "+port);
            try {
                final Socket clientSocket = parseServer.accept();
                System.err.println("Connection Accepted From: " + clientSocket.getInetAddress());
                final BufferedReader br =
                        new BufferedReader(new InputStreamReader(new DataInputStream(clientSocket.getInputStream())));
                final PrintWriter outputWriter =
                        new PrintWriter(new PrintStream(clientSocket.getOutputStream()));
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    if(inputLine.trim().equals("*"))
                        break;
                    final String output = dp.parsePosTaggedLine(inputLine);
                    outputWriter.print(output);
                    outputWriter.flush();
                }
                outputWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
