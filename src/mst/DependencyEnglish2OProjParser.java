package mst;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static edu.umass.cs.mallet.base.util.IoUtils.contentsAsString;


public class DependencyEnglish2OProjParser {
    /*
     * arg[0]=modelfile
     * arg[1]=tempdir
     * arg[2]=port
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 3) {
            System.out.println("Usage: DependencyEnglish2OProjParser <model-file> <tmpdir> <port>");
            System.exit(1);
        }
        final String modelFile = args[0].trim();
        final String tempDir = args[1];
        final int port = Integer.parseInt(args[2].trim());

        //INITIALIZE PARSER
        System.setProperty("java.io.tmpdir", tempDir);
        final ParserOptions options = new ParserOptions(new String[] {
            "test",
            "separate-lab",
            "model-name:" + modelFile,
            "decode-type:proj",
            "order:2",
            "format:CONLL"
        });
        System.err.println("Default temp directory:" + System.getProperty("java.io.tmpdir"));
        final DependencyPipe pipe = options.secondOrder ?
                new DependencyPipe2O(options) : new DependencyPipe(options);
        final DependencyParser dp = new DependencyParser(pipe, options);
        System.err.print("\tLoading model for Dependency Parser...\n");
        dp.loadModel(options.modelName);
        System.err.println("done.");
        pipe.printModelStats(dp.params);
        pipe.closeAlphabets();

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
                String doc = "";
                while ((inputLine = br.readLine()) != null) {
                    if(inputLine.trim().equals("*"))
                        break;
                    doc = doc + Util.getCoNLLFormat(inputLine);
                }
                // WRITE INPUT TO TEMP FILE
                final File tempFile = File.createTempFile("serverInputFile", ".txt");
                // Delete temp file when program exits.
                tempFile.deleteOnExit();
                // Write output to temp file
                final BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
                out.write(doc);
                out.close();
                final File parseFile = File.createTempFile("parsedServerFile",".txt");
                options.testfile = tempFile.getAbsolutePath();
                options.outfile = parseFile.getAbsolutePath();
                dp.outputParses(null);
                final String output = contentsAsString(new File(parseFile.getAbsolutePath()));
                tempFile.delete();
                parseFile.delete();
                outputWriter.print(output);
                outputWriter.flush();
                outputWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
