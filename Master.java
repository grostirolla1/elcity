package pkg3lesautoelastic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

/**
 * Read chunks of defined size from a text file and send to the processing class
 *
 * @author grostirolla
 */
public class Master {

    private int chunksize;
    private int slaves;
    private int maxnodes;
    private ArrayList<Point> point_interess;
    private ObjectInputStream[] ois;
    private OutputStreamWriter[] osw;
    private ServerSocket serversocket;
    private Socket[] slavessocket;
    private int port;
    private String compath;
    private int vmsbyhost;
    private int sockets_on = 0;
    private String location_history;
    private String ip;
    private int minnodes;
    private String logs_dir;
    private long time_begin;
    private long time_end;
    private long tcon_avg;

    /**
     *
     * @param ip the master valid IP address
     * @param port the socket port
     * @param chunksize the size that the chunks must be splitted
     * @param ininodes the initial amount of nodes
     * @param minnodes the minimum amount of nodes
     * @param maxnodes the maximum amount of nodes
     * @param vmsbyhost the amount VMs to be instantiated on each host
     * @param compath the path where the messages will be exchanged
     * @param location_history the path to the location history file
     * @param logs_dir the path where the logs will be saved
     * @param points_interest the points with controllable things
     * @throws IOException
     */
    public Master(String ip, int port, int chunksize, int ininodes, int minnodes, int maxnodes, int vmsbyhost, String compath, String location_history, String logs_dir, ArrayList<Point> points_interest) throws IOException {
        this.chunksize = chunksize;
        this.point_interess = points_interest;
        this.port = port;
        this.serversocket = new ServerSocket(port);
        this.compath = compath;
        this.location_history = location_history;
        this.vmsbyhost = vmsbyhost;
        this.slaves = ininodes * vmsbyhost;
        this.maxnodes = maxnodes;
        this.slavessocket = new Socket[maxnodes * vmsbyhost];
        this.ois = new ObjectInputStream[maxnodes * vmsbyhost];
        this.osw = new OutputStreamWriter[maxnodes * vmsbyhost];
        this.ip = ip;
        this.minnodes = minnodes;
        this.logs_dir = logs_dir;
    }

    public ArrayList<MultiKeyMap> start() throws FileNotFoundException, IOException, InterruptedException, ParseException, ClassNotFoundException {
        System.out.println("MASTER STARTED");
        long t1 = System.currentTimeMillis();

        ArrayList<MultiKeyMap> returns = new ArrayList<MultiKeyMap>();
        System.out.println("Socket server running at"
                + " IP: " + getIp()
                + " Port: " + serversocket.getLocalPort()
                + " Default Timeout: " + serversocket.getSoTimeout());
        System.out.println("Reading File: " + location_history + "in chunks of: " + chunksize);
        BufferedReader br = new BufferedReader(new FileReader(location_history));
        int countchunks = 0;
        int countlines = 0;
        int count = 0;

        //start by connecting initial slaves
        receive_connection();

        try {
            tcon_avg = 0;
            String line = br.readLine();
            String[] all = new String[chunksize];
            while (line != null) {
                all[count] = line;
                line = br.readLine();
                count++;

                //Finish reading chinksize lines, must split chunk among slaves to process
                // line == null to process last values that dont divide exactly by the chunk size
                if (count == chunksize || line == null) {
                    //if null need to remove the last element
                    if (line == null) {
                        String[] tmpall = Arrays.copyOfRange(all, 0, count - 1);
                        all = new String[tmpall.length];
                        all = tmpall;
                    }
                    countchunks++;
                    countlines += count;
                    count = 0;

                    //add or remove resources
                    update_sockets();

                    long tcon1 = System.currentTimeMillis();
                    for (int i = 0; i < slaves; i++) {
                        //split chunk among slaves
                        if (i < slaves - 1) {
                            String[] tmp_chunk = Arrays.copyOfRange(all, i * (all.length / slaves), (i * (all.length / slaves) + (all.length / slaves)));
                            for (String str : tmp_chunk) {
                                osw[i].write(str);
                                osw[i].write("\n");
                            }

                        }
                        if (i == slaves - 1) {
                            String[] tmp_chunk = Arrays.copyOfRange(all, i * (all.length / slaves), all.length);
                            for (String str : tmp_chunk) {
                                osw[i].write(str);
                                osw[i].write("\n");
                            }

                        }

                        osw[i].write("\n");
                        osw[i].flush();
                        long tcon2 = System.currentTimeMillis();
                        System.out.println("Communication Time: " + (tcon2 - tcon1));
                        tcon_avg += (tcon2 - tcon1);
                    }
                    //Send chunks to all slaves
                    //Wait all slaves return output
                    for (int i = 0; i < slaves; i++) {
                        ArrayList<MultiKeyMap> tmparray = (ArrayList<MultiKeyMap>) ois[i].readObject();
                        for (MultiKeyMap multiKeyMap : tmparray) {
                            returns.add(multiKeyMap);
                        }
                    }

                    all = new String[chunksize];
                }
            }
            System.out.println("Finished the send to slaves process");
        } finally {
            br.close();
        }
        long t2 = System.currentTimeMillis();

        long t3 = System.currentTimeMillis();
        /*
        * Here we have to merge all the returns from slaves.
        * Each Slave return an ArrayList<MultiKeyMap> which are 
        * already added to the variable returns, that is a MultiKeyMap.
        * A MultiKeyMap containing the following structure
        * <Point,Calendar,ArrayList> where the Points are contained in the
        * points_interest, the calendar is a time truncated to the format yyyy-MM-dd
        * HH:mm and the ArrayList contains Integer values of all users that passed
        * near the respective point
        * 
        * We created one csv file per point
        * Each point contains: "Day(yyyy-MM-dd),Time(HH:mm),Users(User-User-....)\n"
         */

        DateFormat dir_date_format = new SimpleDateFormat("MM-dd-HH-mm-ss");
        DateFormat format_day = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat format_time = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String timenow = dir_date_format.format(date).toString();
        File dir = new File(logs_dir + timenow);
        dir.mkdir();
        if (!dir.exists()) {
            System.out.println("Cannot Create Directory");
        } else {
            System.out.println("Saving Logs");

            File file_points = new File(dir + "/points.info");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file_points));
            bw.write("Point,Latitude,Longitude,Heat");
            bw.newLine();
            for (int i = 0; i < point_interess.size(); i++) {
                bw.write(i + ",");
                bw.write(point_interess.get(i).getLatitude() + ",");
                bw.write(point_interess.get(i).getLongitude() + ",");
                bw.write(point_interess.get(i).getHeat() + "");
                bw.newLine();
            }
            bw.close();

            int countpoint = 0;
            int countTmap = 0;
            int sumsize = 0;

            for (Point P : point_interess) {
                File file_log = new File(dir+ "/P" + countpoint + ".log");
                bw = new BufferedWriter(new FileWriter(file_log));
                bw.write("Day,Time,Users");
                bw.newLine();
                TreeMap<Calendar, ArrayList<Integer>> times_ordenated = new TreeMap<Calendar, ArrayList<Integer>>();
                for (MultiKeyMap map : returns) {
                    MapIterator it = map.mapIterator();
                    while (it.hasNext()) {
                        Object key = it.next();
                        Point pt = (Point) (((MultiKey) key).getKey(0));
                        if (P.compareTo(pt) == 0) {
                            ArrayList<Integer> value = (ArrayList<Integer>) it.getValue();
                            GregorianCalendar cal = (GregorianCalendar) ((MultiKey) key).getKey(1);
                            times_ordenated.put(cal, value);
                        }
                    }
                    sumsize += map.size();
                    countTmap++;
                }

                //now we have a treemap ordenated of the current interrest_point by date
                Set use_time = times_ordenated.keySet();
                for (Object object : use_time) {
                    //Each point contains: "Day(yyyy-MM-dd),Time(HH:mm),Users(User-User-....)\n"
                    Calendar time = (Calendar) object;
                    String day_on = format_day.format(time.getTime()).toString();
                    String time_on = format_time.format(time.getTime()).toString();
                    bw.write(day_on + "," + time_on + ",");
                    ArrayList<Integer> allusers = times_ordenated.get(time);
                    String userstmp = "";
                    for (Integer user : allusers) {
                        userstmp = userstmp.concat(user + "-");
                    }
                    userstmp = userstmp.substring(0, userstmp.length() - 1);
                    bw.write(userstmp);
                    bw.newLine();
                }

                bw.close();
                System.out.print(countpoint+" Point "+P);
                System.out.println(" - Inputs: "+times_ordenated.size());
                countpoint++;
            }
            System.out.println("CountPoint " + countpoint);
            System.out.println("Count TMap " + countTmap);
            System.out.println("TMap sumsize " + sumsize);
        }
        long t4 = System.currentTimeMillis();
//
        File logmaster = new File(dir + "/timesMaster.info");
        BufferedWriter bw = new BufferedWriter(new FileWriter(logmaster));
        bw.write("Read and Send Time: " + (t2 - t1));
        bw.newLine();
        bw.write("Process Return and Save logs Time: " + (t4 - t3));
        bw.newLine();
        bw.write("Average Communication Time:" + tcon_avg / countchunks);
        bw.newLine();
        double dtemp = (t2 - t1);
        bw.write("Average Time By Row: " + (dtemp / countlines));
        bw.newLine();
        bw.write("Total Execution Time: " + (t4 - t1));
        bw.newLine();
        bw.write("Total Chunks " + countchunks);
        bw.newLine();
        bw.write("Total Lines " + countlines);
        bw.close();

        return returns;
    }

    /**
     * @return the chunksize
     */
    public int getChunksize() {
        return chunksize;
    }

    /**
     * @param chunksize the chunksize to set
     */
    public void setChunksize(int chunksize) {
        this.chunksize = chunksize;
    }

    /**
     * @return the slaves
     */
    public int getSlaves() {
        return slaves;
    }

    /**
     * @return the maxnodes
     */
    public int getMaxnodes() {
        return maxnodes;
    }

    /**
     * @return the point_interess
     */
    public ArrayList<Point> getPoint_interess() {
        return point_interess;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    //release the resource, return -1 if error and 0 if ok
    private int release_resource() throws IOException {

        while (getSockets_on() > this.slaves - getVmsbyhost()) {
            System.out.println("Tentou liberar Socket " + (getSockets_on() - 1));
            System.out.println("slavessocket[sockets_on] closed?: " + slavessocket[getSockets_on() - 1].isClosed());
            osw[getSockets_on() - 1].close();
            ois[getSockets_on() - 1].close();
            slavessocket[getSockets_on() - 1].close();
            sockets_on--;
        }
        File release = new File(getCompath());
        release = new File(getCompath() + "liberarecurso.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(release));
        bw.write("libera_recurso");
        bw.close();
        this.slaves -= this.getVmsbyhost();

        return 0;

    }

    private int receive_connection() throws IOException, InterruptedException {
        //receive connections
        while (getSockets_on() < slaves) {
            File file = new File(getCompath() + "serverok.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            while (!file.canWrite()) {
                Thread.sleep(500);
            }
            bw.write(getIp());
            bw.close();

            //connect
            slavessocket[getSockets_on()] = serversocket.accept();
            System.out.println("ACCEPT CONNECTION: " + slavessocket[getSockets_on()].getInetAddress().getCanonicalHostName());
            ois[getSockets_on()] = new ObjectInputStream(slavessocket[getSockets_on()].getInputStream());
            osw[getSockets_on()] = new OutputStreamWriter(slavessocket[getSockets_on()].getOutputStream());
            System.out.println("NEW SOCKET NAME: " + slavessocket[getSockets_on()].getInetAddress().getHostName());
            sockets_on++;
//            Thread.sleep(1000);
        }
        System.out.println("Sockets esperados se conectaram");
        return 0;
    }

    /*return +1 if new resources  -1 if load is low 0 if no notif
      if allow removal, set remove true as input
     */
    private int update_sockets() throws IOException, InterruptedException {
        //>>>>>>>>>>>>>>>>>vou ver se tem algum arquivo
        File dir = new File(getCompath());
        File fList[] = dir.listFiles();
        for (int x = 0; x < fList.length; x++) {
            String nomearquivo = fList[x].getName();
            if (nomearquivo.equalsIgnoreCase("novorecurso.txt")) {
                fList[x].delete();
                if (getSockets_on() < maxnodes * getVmsbyhost()) {

                    this.slaves += this.getVmsbyhost();
                    receive_connection();
                } else {
                    System.out.println("Maximum amount of VMs reached");
                }
                return 1;
            } else if (nomearquivo.equalsIgnoreCase("poucacarga.txt")) {
                fList[x].delete();
                //close sockets and subtract max slaves
                if (getSockets_on() > getMinnodes() * getVmsbyhost()) {
                    release_resource();
                } else {
                    System.out.println("Minimum amount of VMs reached");
                }
                return -1;
            }
        }

        return 0;
    }

    /**
     * @return the compath
     */
    public String getCompath() {
        return compath;
    }

    /**
     * @return the vmsbyhost
     */
    public int getVmsbyhost() {
        return vmsbyhost;
    }

    /**
     * @return the sockets_on
     */
    public int getSockets_on() {
        return sockets_on;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return the minnodes
     */
    public int getMinnodes() {
        return minnodes;
    }

}
