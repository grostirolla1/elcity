/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3lesautoelastic;

import java.util.ArrayList;

/**
 *
 * @author grostirolla
 */
public class Main {

    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();

        try {
            if (args.length > 0) {
                System.out.println("Tipo:" + args[0]);

               
//                String compath = "/Users/grostirolla/Dropbox/Dissertação/Implementacao/sharedir/";
//                String location_history = "/Users/grostirolla/Desktop/taxi_1k.txt";
//                String location_history = "/Users/grostirolla/Desktop/taxi_february.txt";

                //PARAMS MASTER - IP, CHUNKSIZE, NPOINTS
                if (args[0].equalsIgnoreCase("Master")) {
                    String ip = args[1];
                    int chunksize = Integer.parseInt(args[2]);
                    int npoints = Integer.parseInt(args[3]);
                    String compath = args[4]; //"/one/app/msg/";
                    String location_history = args[5];//"/one/app/3les/taxi_february.txt";
                    String logs_dir = args[6];
                    ArrayList<Point> point_interess = new ArrayList<Point>();
                    if(npoints == 25)
                        point_interess = fill25points();
                    else if(npoints == 50)
                        point_interess = fill50points();
                    else
                        point_interess = fill100points();
                    System.out.println("NPOINTS: " + point_interess.size());
                    System.out.println("IP Master:" + args[1]);

                    Master master = new Master(ip, 7000, chunksize, 1, 1, 10, 2, compath, location_history, logs_dir, point_interess);
                    master.start();
                }
                if (args[0].equalsIgnoreCase("Slave")) {
                    int npoints = Integer.parseInt(args[1]);
                    String compath = args[2]; //"/one/app/msg/";
                    String logs_dir = args[3];
                    double distance = Double.parseDouble(args[4]);

                    ArrayList<Point> point_interess = new ArrayList<Point>();
                    if(npoints == 25)
                        point_interess = fill25points();
                    else if(npoints == 50)
                        point_interess = fill50points();
                    else
                        point_interess = fill100points();

                    System.out.println("NPOINTS: " + point_interess.size());
                    Slave slave = new Slave(7000, compath, logs_dir, 1, point_interess, distance);
                    slave.start();
                }
            } else {
                System.out.println("Slave or Master?");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long t2 = System.currentTimeMillis();

        System.out.println("Execution Time: " + (t2 - t1));
    }

    private static ArrayList<Point> fill100points() {
        System.out.println("100 Points");
        ArrayList<Point> allpoints = new ArrayList<Point>();
        //100 POINTS
//33 LOW HEAT

        allpoints.add(new Point(41.89844397625824, 12.429313659667969, 0));
        allpoints.add(new Point(41.917862466995025, 12.446823120117188, 0));
        allpoints.add(new Point(41.9436594779771, 12.473602294921875, 0));
        allpoints.add(new Point(41.92271616673925, 12.50518798828125, 0));
        allpoints.add(new Point(41.911475457497446, 12.516860961914062, 0));
        allpoints.add(new Point(41.893332865411104, 12.511367797851562, 0));
        allpoints.add(new Point(41.88822134553516, 12.499351501464844, 0));
        allpoints.add(new Point(41.88336502279732, 12.486991882324219, 0));
        allpoints.add(new Point(41.87007214667565, 12.48046875, 0));
        allpoints.add(new Point(41.865214444575585, 12.468109130859375, 0));
        allpoints.add(new Point(41.895888471963794, 12.435836791992188, 0));
        allpoints.add(new Point(41.887965758804484, 12.425193786621094, 0));
        allpoints.add(new Point(41.8332466762872, 12.470512390136719, 0));
        allpoints.add(new Point(41.875952029043006, 12.459182739257812, 0));
        allpoints.add(new Point(41.870839118528714, 12.454719543457031, 0));
        allpoints.add(new Point(41.89384399490223, 12.458152770996094, 0));
        allpoints.add(new Point(41.895377358833876, 12.445106506347656, 0));
        allpoints.add(new Point(41.91224193238467, 12.464675903320312, 0));
        allpoints.add(new Point(41.91913979219502, 12.445106506347656, 0));
        allpoints.add(new Point(41.93242245865235, 12.511367797851562, 0));
        allpoints.add(new Point(41.92578147109541, 12.514801025390625, 0));
        allpoints.add(new Point(41.86316898589658, 12.486648559570312, 0));
        allpoints.add(new Point(41.87467383975801, 12.490768432617188, 0));
        allpoints.add(new Point(41.87390691391704, 12.457466125488281, 0));
        allpoints.add(new Point(41.863424671810066, 12.461929321289062, 0));
        allpoints.add(new Point(41.850639123649636, 12.478752136230469, 0));
        allpoints.add(new Point(41.83682786072715, 12.468452453613281, 0));
        allpoints.add(new Point(41.828897825796226, 12.472572326660156, 0));
        allpoints.add(new Point(41.894866241613634, 12.457122802734375, 0));
        allpoints.add(new Point(41.93318868195924, 12.51068115234375, 0));
        allpoints.add(new Point(41.91990617504297, 12.513771057128906, 0));
        allpoints.add(new Point(41.91045347666421, 12.515487670898438, 0));
        allpoints.add(new Point(41.90048830606728, 12.514801025390625, 0));

//34 MEDIUM HEAT
        allpoints.add(new Point(41.895377358833876, 12.468452453613281, 1));
        allpoints.add(new Point(41.89946614934354, 12.4639892578125, 1));
        allpoints.add(new Point(41.907642945005236, 12.457466125488281, 1));
        allpoints.add(new Point(41.906620902814, 12.468109130859375, 1));
        allpoints.add(new Point(41.91070897340646, 12.476005554199219, 1));
        allpoints.add(new Point(41.918117934080364, 12.491798400878906, 1));
        allpoints.add(new Point(41.91913979219502, 12.491798400878906, 1));
        allpoints.add(new Point(41.921438913126586, 12.481155395507812, 1));
        allpoints.add(new Point(41.9204170918282, 12.478065490722656, 1));
        allpoints.add(new Point(41.91173095014916, 12.500038146972656, 1));
        allpoints.add(new Point(41.911475457497446, 12.495574951171875, 1));
        allpoints.add(new Point(41.91224193238467, 12.529563903808594, 1));
        allpoints.add(new Point(41.908153959965105, 12.529220581054688, 1));
        allpoints.add(new Point(41.893332865411104, 12.482185363769531, 1));
        allpoints.add(new Point(41.89435512030309, 12.471542358398438, 1));
        allpoints.add(new Point(41.887965758804484, 12.473602294921875, 1));
        allpoints.add(new Point(41.8925661635051, 12.468795776367188, 1));
        allpoints.add(new Point(41.897166236893334, 12.466392517089844, 1));
        allpoints.add(new Point(41.90253257042991, 12.476005554199219, 1));
        allpoints.add(new Point(41.79409638608149, 12.274818420410156, 1));
        allpoints.add(new Point(41.79665595947719, 12.2772216796875, 1));
        allpoints.add(new Point(41.79563214238514, 12.252845764160156, 1));
        allpoints.add(new Point(41.79358445913614, 12.250099182128906, 1));
        allpoints.add(new Point(41.84219926167241, 12.472572326660156, 1));
        allpoints.add(new Point(41.906620902814, 12.469139099121094, 1));
        allpoints.add(new Point(41.90534332706592, 12.470855712890625, 1));
        allpoints.add(new Point(41.89793288358008, 12.497634887695312, 1));
        allpoints.add(new Point(41.90968698030153, 12.498321533203125, 1));
        allpoints.add(new Point(41.91249742196845, 12.498321533203125, 1));
        allpoints.add(new Point(41.918117934080364, 12.491455078125, 1));
        allpoints.add(new Point(41.919650715116326, 12.479438781738281, 1));
        allpoints.add(new Point(41.91045347666421, 12.476348876953125, 1));
        allpoints.add(new Point(41.906620902814, 12.469139099121094, 1));
        allpoints.add(new Point(41.908920474735154, 12.483901977539062, 1));
        allpoints.add(new Point(41.908153959965105, 12.48046875, 1));

//33 HIGH HEAT
        allpoints.add(new Point(41.90840946591109, 12.488365173339844, 2));
        allpoints.add(new Point(41.90840946591109, 12.490768432617188, 2));
        allpoints.add(new Point(41.90636538970964, 12.490768432617188, 2));
        allpoints.add(new Point(41.90636538970964, 12.488021850585938, 2));
        allpoints.add(new Point(41.90508780884847, 12.490081787109375, 2));
        allpoints.add(new Point(41.90406572575241, 12.488021850585938, 2));
        allpoints.add(new Point(41.90278809887352, 12.48870849609375, 2));
        allpoints.add(new Point(41.90508780884847, 12.4859619140625, 2));
        allpoints.add(new Point(41.903810202421845, 12.484931945800781, 2));
        allpoints.add(new Point(41.90329915269291, 12.482528686523438, 2));
        allpoints.add(new Point(41.90329915269291, 12.479782104492188, 2));
        allpoints.add(new Point(41.90457676934566, 12.478752136230469, 2));
        allpoints.add(new Point(41.896144026994854, 12.481842041015625, 2));
        allpoints.add(new Point(41.896144026994854, 12.479782104492188, 2));
        allpoints.add(new Point(41.895888471963794, 12.475318908691406, 2));
        allpoints.add(new Point(41.89742178681152, 12.473602294921875, 2));
        allpoints.add(new Point(41.900232768420246, 12.471885681152344, 2));
        allpoints.add(new Point(41.90202151047487, 12.497634887695312, 2));
        allpoints.add(new Point(41.90508780884847, 12.479438781738281, 2));
        allpoints.add(new Point(41.903810202421845, 12.48321533203125, 2));
        allpoints.add(new Point(41.90278809887352, 12.4859619140625, 2));
        allpoints.add(new Point(41.90508780884847, 12.4859619140625, 2));
        allpoints.add(new Point(41.907642945005236, 12.490425109863281, 2));
        allpoints.add(new Point(41.901254912872794, 12.481498718261719, 2));
        allpoints.add(new Point(41.90457676934566, 12.481155395507812, 2));
        allpoints.add(new Point(41.903810202421845, 12.478752136230469, 2));
        allpoints.add(new Point(41.90534332706592, 12.478065490722656, 2));
        allpoints.add(new Point(41.90534332706592, 12.482185363769531, 2));
        allpoints.add(new Point(41.90304362629451, 12.483901977539062, 2));
        allpoints.add(new Point(41.903810202421845, 12.48870849609375, 2));
        allpoints.add(new Point(41.895888471963794, 12.47772216796875, 2));
        allpoints.add(new Point(41.89742178681152, 12.473602294921875, 2));
//        allpoints.add(new Point(41.900743842691725, 12.47222900390625, 2));
        return allpoints;
    }
    
    
    private static ArrayList<Point> fill50points() {
                System.out.println("50 Points");

        ArrayList<Point> allpoints = new ArrayList<Point>();
        //100 POINTS
//33 LOW HEAT

        allpoints.add(new Point(41.89844397625824, 12.429313659667969, 0));
        allpoints.add(new Point(41.917862466995025, 12.446823120117188, 0));
        allpoints.add(new Point(41.9436594779771, 12.473602294921875, 0));
        allpoints.add(new Point(41.92271616673925, 12.50518798828125, 0));
        allpoints.add(new Point(41.911475457497446, 12.516860961914062, 0));
        allpoints.add(new Point(41.893332865411104, 12.511367797851562, 0));
        allpoints.add(new Point(41.88822134553516, 12.499351501464844, 0));
        allpoints.add(new Point(41.88336502279732, 12.486991882324219, 0));
        allpoints.add(new Point(41.87007214667565, 12.48046875, 0));
        allpoints.add(new Point(41.865214444575585, 12.468109130859375, 0));
        allpoints.add(new Point(41.895888471963794, 12.435836791992188, 0));
        allpoints.add(new Point(41.887965758804484, 12.425193786621094, 0));
        allpoints.add(new Point(41.8332466762872, 12.470512390136719, 0));
        allpoints.add(new Point(41.875952029043006, 12.459182739257812, 0));
        allpoints.add(new Point(41.870839118528714, 12.454719543457031, 0));
        allpoints.add(new Point(41.89384399490223, 12.458152770996094, 0));


//34 MEDIUM HEAT
        allpoints.add(new Point(41.895377358833876, 12.468452453613281, 1));
        allpoints.add(new Point(41.89946614934354, 12.4639892578125, 1));
        allpoints.add(new Point(41.907642945005236, 12.457466125488281, 1));
        allpoints.add(new Point(41.906620902814, 12.468109130859375, 1));
        allpoints.add(new Point(41.91070897340646, 12.476005554199219, 1));
        allpoints.add(new Point(41.918117934080364, 12.491798400878906, 1));
        allpoints.add(new Point(41.91913979219502, 12.491798400878906, 1));
        allpoints.add(new Point(41.921438913126586, 12.481155395507812, 1));
        allpoints.add(new Point(41.9204170918282, 12.478065490722656, 1));
        allpoints.add(new Point(41.91173095014916, 12.500038146972656, 1));
        allpoints.add(new Point(41.911475457497446, 12.495574951171875, 1));
        allpoints.add(new Point(41.91224193238467, 12.529563903808594, 1));
        allpoints.add(new Point(41.908153959965105, 12.529220581054688, 1));
        allpoints.add(new Point(41.893332865411104, 12.482185363769531, 1));
        allpoints.add(new Point(41.89435512030309, 12.471542358398438, 1));
        allpoints.add(new Point(41.887965758804484, 12.473602294921875, 1));
        allpoints.add(new Point(41.8925661635051, 12.468795776367188, 1));
        allpoints.add(new Point(41.897166236893334, 12.466392517089844, 1));

//33 HIGH HEAT
        allpoints.add(new Point(41.90840946591109, 12.488365173339844, 2));
        allpoints.add(new Point(41.90840946591109, 12.490768432617188, 2));
        allpoints.add(new Point(41.90636538970964, 12.490768432617188, 2));
        allpoints.add(new Point(41.90636538970964, 12.488021850585938, 2));
        allpoints.add(new Point(41.90508780884847, 12.490081787109375, 2));
        allpoints.add(new Point(41.90406572575241, 12.488021850585938, 2));
        allpoints.add(new Point(41.90278809887352, 12.48870849609375, 2));
        allpoints.add(new Point(41.90508780884847, 12.4859619140625, 2));
        allpoints.add(new Point(41.903810202421845, 12.484931945800781, 2));
        allpoints.add(new Point(41.90329915269291, 12.482528686523438, 2));
        allpoints.add(new Point(41.90329915269291, 12.479782104492188, 2));
        allpoints.add(new Point(41.90457676934566, 12.478752136230469, 2));
        allpoints.add(new Point(41.896144026994854, 12.481842041015625, 2));
        allpoints.add(new Point(41.896144026994854, 12.479782104492188, 2));
        allpoints.add(new Point(41.895888471963794, 12.475318908691406, 2));
        allpoints.add(new Point(41.89742178681152, 12.473602294921875, 2));
        return allpoints;
    }
    
    
    private static ArrayList<Point> fill25points() {
                System.out.println("25 Points");

        ArrayList<Point> allpoints = new ArrayList<Point>();
        //100 POINTS
//33 LOW HEAT

        allpoints.add(new Point(41.89844397625824, 12.429313659667969, 0));
        allpoints.add(new Point(41.917862466995025, 12.446823120117188, 0));
        allpoints.add(new Point(41.9436594779771, 12.473602294921875, 0));
        allpoints.add(new Point(41.92271616673925, 12.50518798828125, 0));
        allpoints.add(new Point(41.911475457497446, 12.516860961914062, 0));
        allpoints.add(new Point(41.893332865411104, 12.511367797851562, 0));
        allpoints.add(new Point(41.88822134553516, 12.499351501464844, 0));
        allpoints.add(new Point(41.88336502279732, 12.486991882324219, 0));
//34 MEDIUM HEAT
        allpoints.add(new Point(41.895377358833876, 12.468452453613281, 1));
        allpoints.add(new Point(41.89946614934354, 12.4639892578125, 1));
        allpoints.add(new Point(41.907642945005236, 12.457466125488281, 1));
        allpoints.add(new Point(41.906620902814, 12.468109130859375, 1));
        allpoints.add(new Point(41.91070897340646, 12.476005554199219, 1));
        allpoints.add(new Point(41.918117934080364, 12.491798400878906, 1));
        allpoints.add(new Point(41.91913979219502, 12.491798400878906, 1));
        allpoints.add(new Point(41.921438913126586, 12.481155395507812, 1));
        allpoints.add(new Point(41.9204170918282, 12.478065490722656, 1));
//33 HIGH HEAT
        allpoints.add(new Point(41.90840946591109, 12.488365173339844, 2));
        allpoints.add(new Point(41.90840946591109, 12.490768432617188, 2));
        allpoints.add(new Point(41.90636538970964, 12.490768432617188, 2));
        allpoints.add(new Point(41.90636538970964, 12.488021850585938, 2));
        allpoints.add(new Point(41.90508780884847, 12.490081787109375, 2));
        allpoints.add(new Point(41.90406572575241, 12.488021850585938, 2));
        allpoints.add(new Point(41.90278809887352, 12.48870849609375, 2));
        allpoints.add(new Point(41.90508780884847, 12.4859619140625, 2));
        return allpoints;
    }

}
