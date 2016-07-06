/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3lesautoelastic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.collections4.map.MultiKeyMap;

/**
 *
 * @author grostirolla
 */
public class Slave {

//    private static final String ENCODING = "ISO-8859-1";
    private int port;
    private String compath;
    private String logs_dir;
    private String serverIP;
    private Socket connection;
    private final int timeout = 3;
    private int nthreads = 1;
    private ArrayList<Point> points_interess;
    private double mindistance;
    private Task[] threadsChunk;
    private ObjectOutputStream oo;
    private BufferedReader in;
    private long avg_tproc = 0;
    private int count_proc = 0;

    /**
     *
     * @param port the socket port
     * @param compath the path where the messages will be exchanged
     * @param nthreads the amount of threads that can be created to process each
     * chunk
     * @param points_interest the points with controllable things
     * @param mindistance the minimum distance to be considered for on/off
     */
    public Slave(int port, String compath, String logs_dir, int nthreads, ArrayList<Point> points_interest, double mindistance) {
        this.port = port;
        this.compath = compath;
        this.logs_dir = logs_dir;
        this.nthreads = nthreads;
        this.points_interess = points_interest;
        this.mindistance = mindistance;
        this.threadsChunk = new Task[nthreads];
    }

    public void start() throws InterruptedException, FileNotFoundException, IOException, ParseException {
        System.out.println("SLAVE STARTED");
        long t1 = System.currentTimeMillis();

        File dir = new File(compath);

        boolean lock = true;
//        W8 master OK
        while (lock) {
            System.out.println("Waiting for server available indication");
            Thread.sleep(1000);
            File[] allfiles = dir.listFiles();
            for (File currFile : allfiles) {
                if (currFile.getName().equalsIgnoreCase("serverok.txt") && currFile.canRead()) {
                    System.out.println("Server indicates that is ok to connect");
                    BufferedReader in = new BufferedReader(new FileReader(compath + "serverok.txt"));
                    this.serverIP = in.readLine();
                    System.out.println("IP: " + this.serverIP);
                    in.close();
                    currFile.delete();
                    lock = false;
                    break;
                }
            }
        }
        //Master send ok, start communication, receive chunk, process
        int counttimeout = 0;

        System.out.println("Connection details:"
                + " IP " + serverIP
                + " PORT " + port);
        do {
//            System.out.println("LOOP CONNECT " + counttimeout);
            connection = new Socket(serverIP, port);
            counttimeout++;
            Thread.sleep(500);
        } while (connection.isClosed() && counttimeout < timeout);
        if (connection.isClosed() || counttimeout >= timeout) {
            System.out.println("Trouble with sockets");
            System.exit(-1);
        }

        System.out.println("Connected");
        //CONECTION OK, Open Streams
        oo = new ObjectOutputStream(connection.getOutputStream()); //para retornar o multikey
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));//para receber o chunk

        avg_tproc = 0;
        long t2 = System.currentTimeMillis();

        while (!connection.isClosed()) {
            String line = in.readLine();
            int countrcv = 1;
            ArrayList<String> arrayLines = new ArrayList<String>();
            try {
                while (line.length() > 2) {
                    arrayLines.add(line);
                    line = in.readLine();
                    countrcv++;
                }
                arrayLines.remove(arrayLines.size() - 1);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(countrcv + " Line with Error (If null probably the last)" + line);
                System.out.println("Finalizing APP");
                break;
            }

            String[] input = new String[arrayLines.size()];
            input = arrayLines.toArray(input);
            int chunksize = input.length;
            ArrayList<MultiKeyMap> returns = new ArrayList<MultiKeyMap>();
            long tproc1 = System.currentTimeMillis();

            for (int i = 0; i < nthreads; i++) {
                threadsChunk[i] = new Task();
                if (i < nthreads - 1) {
                    returns.add(threadsChunk[i].run(Arrays.copyOfRange(input, i * (chunksize / nthreads), (i * (chunksize / nthreads) + chunksize / nthreads)), points_interess, mindistance));
                }
                if (i == nthreads - 1) {
                    returns.add(threadsChunk[i].run(Arrays.copyOfRange(input, i * (chunksize / nthreads), chunksize), points_interess, mindistance));
                }
            }

            for (int i = 0; i < nthreads; i++) {
                threadsChunk[i].join();
            }
            long tproc2 = System.currentTimeMillis();
            count_proc++;
            avg_tproc += tproc2 - tproc1;

            if (!connection.isClosed()) {
                oo.writeObject(returns);
                oo.flush();
            } else {
                System.out.println("Connection is gone");
            }

        }
        long t4 = System.currentTimeMillis();

        File log = new File(logs_dir + "/timesSlave" + connection.getInetAddress().getHostName() + ".info");
        BufferedWriter bw = new BufferedWriter(new FileWriter(log));

        bw.write("Waiting Connection Time: " + (t2 - t1));
        bw.newLine();
        bw.write("Total Duration Time " + (t4 - t1));
        bw.newLine();
        bw.write("Processing Time: " + (t4 - t2));
        bw.newLine();
        bw.write("Average Processing Time: " + (avg_tproc / count_proc));
        bw.close();

    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the compath
     */
    public String getCompath() {
        return compath;
    }

    /**
     * @param compath the compath to set
     */
    public void setCompath(String compath) {
        this.compath = compath;
    }

    /**
     * @return the serverIP
     */
    public String getServerIP() {
        return serverIP;
    }

    /**
     * @param serverIP the serverIP to set
     */
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    /**
     * @return the connection
     */
    public Socket getConnection() {
        return connection;
    }

    /**
     * @return the nthreads
     */
    public int getNthreads() {
        return nthreads;
    }

    /**
     * @param nthreads the nthreads to set
     */
    public void setNthreads(int nthreads) {
        this.nthreads = nthreads;
    }
}
