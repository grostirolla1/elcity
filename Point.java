/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3lesautoelastic;

import java.io.Serializable;

/**
 *
 * @author grostirolla
 */
public class Point implements Comparable<Point>, Serializable {

    private double latitude;
    private double longitude;
    private int heat;

    /**
     *
     * @param latitude a latitude point
     * @param longitude a longitude point
     */
    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.heat=0;
    }
    /**
     *
     * @param latitude a latitude point
     * @param longitude a longitude point
     * @param heat the heat of the point (higher means more users/time)
     */
    public Point(double latitude, double longitude,int heat) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.heat=heat;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int compareTo(Point o) {
        if (this.latitude == o.getLatitude() && this.longitude == o.longitude) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Latitude: " + this.latitude + " Longitude: " + this.longitude+ " Heat: " + this.heat;
    }

    //return the distance in meters
    public double distance(Point p2) {
        double theta = this.getLongitude() - p2.getLongitude();
        double dist = Math.sin(deg2rad(this.getLatitude())) * Math.sin(deg2rad(p2.getLatitude())) + Math.cos(deg2rad(this.getLatitude())) * Math.cos(deg2rad(p2.getLatitude())) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
//		if (unit == "K") {
//			dist = dist * 1.609344;
//		} else if (unit == "N") {
//			dist = dist * 0.8684;
//		} else if (unit == "M")
//                {
        dist = dist * 1.609344 * 1000;
//                }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
 /*::	This function converts decimal degrees to radians						 :*/
 /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
 /*::	This function converts radians to decimal degrees						 :*/
 /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    /**
     * @return the heat
     */
    public int getHeat() {
        return heat;
    }

    /**
     * @param heat the heat to set
     */
    public void setHeat(int heat) {
        this.heat = heat;
    }
}
