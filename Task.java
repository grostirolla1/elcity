/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3lesautoelastic;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

/**
 *
 * @author grostirolla
 *
 */
public class Task extends Thread {

    /**
     *
     * @param chunk the array of string to be processed
     * @param points_interest the points where things are located
     * @param distance the distance to consider on/off
     * @return A MultiKeyMap containing the following structure
     * <Point,Calendar,ArrayList> where the Points are contained in the
     * points_interest, the calendar is a time truncated to the format yyyy-MM-dd
     * HH:mm and the ArrayList contains Integer values of all users that passed
     * near the respective point
     */
    public MultiKeyMap run(String[] chunk, ArrayList<Point> points_interest, double distance) throws InterruptedException, ParseException {
//        System.out.println("TAMANHO DO CHUNK: " + chunk.length);
        Point[] gps_points = new Point[chunk.length];

        //<point, calendar, arraylist<int>>
        MultiKeyMap output = new MultiKeyMap();

        //extract points from chunk
        for (int i = 0; i < chunk.length; i++) {
            String strtmp = chunk[i].substring(chunk[i].indexOf("POINT(") + 6, chunk[i].length() - 1);
            String[] coordinates_chunk = strtmp.split(" ");
            double lati_chunk = Double.parseDouble(coordinates_chunk[0]);
            double longi_chunk = Double.parseDouble(coordinates_chunk[1]);
            gps_points[i] = new Point(lati_chunk, longi_chunk);
        }
        int countproc = 0;
        for (Point p2 : points_interest) {
            for (int i = 0; i < gps_points.length; i++) {
                countproc++;
                if (gps_points[i].distance(p2) <= distance) {
// Formato:         156;2014-02-01 00:00:00.739166+01;POINT(41.8836718276551 12.4877775603346)
                    String split = chunk[i].substring(chunk[i].indexOf(";") + 1, chunk[i].indexOf(".") - 3);

                    SimpleDateFormat sdf1 = new SimpleDateFormat();
                    sdf1.applyPattern("yyyy-MM-dd HH:mm");
                    Calendar cal = new GregorianCalendar();
                    cal.setTime(sdf1.parse(split));
                    if (output.containsKey(p2, cal)) {
                        int newUser = Integer.parseInt(chunk[i].substring(0, chunk[i].indexOf(";")));
                        ArrayList<Integer> tmp_array = ((ArrayList<Integer>) output.get(p2, cal));
                        if (!tmp_array.contains(newUser)) {
                            tmp_array.add(newUser);
                        }

                    } else {
                        output.put(p2, cal, new ArrayList<Integer>());
                        int newUser = Integer.parseInt(chunk[i].substring(0, chunk[i].indexOf(";")));
                        ((ArrayList<Integer>) output.get(p2, cal)).add(newUser);

                    }
                }
            }
        }
        System.out.println("Points Chunk: " + gps_points.length + " Points Interess: " + points_interest.size() + " Loops: " + countproc);
        return output;

    }

}
