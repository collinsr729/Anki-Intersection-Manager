package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.BatteryLevelRequestMessage;
import de.adesso.anki.messages.BatteryLevelResponseMessage;
import de.adesso.anki.messages.LightsPatternMessage;
import de.adesso.anki.messages.LightsPatternMessage.LightConfig;
import de.adesso.anki.messages.PingRequestMessage;
import de.adesso.anki.messages.PingResponseMessage;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetSpeedMessage;
//import de.adesso.anki.roadmap.Roadmap;
//import de.adesso.anki.roadmap.RoadmapScanner;
import de.adesso.anki.messages.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.net.*;
import java.io.*;

/**
 * A simple test program to test a connection to your Anki 'Supercars' and 'Supertrucks' using the NodeJS Bluetooth gateway.
 * Simple follow the installation instructions at http://github.com/adessoAG/anki-drive-java, build this project, start the
 * bluetooth gateway using ./gradlew server, and run this class.
 * 
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class AnkiConnectionTest {

    static long pingReceivedAt;
    static long pingSentAt;
    static Vehicle v;
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector("localhost", 5000);
        System.out.print("...looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS! They are:");

            Iterator<Vehicle> iter = vehicles.iterator();
            while (iter.hasNext()) {
                 v = iter.next();

                 VehicleInfo info = new VehicleInfo ();
                 info.isMaster = false;
                 info.isClear= false;
//                 roadpieceid
//                 info.

                System.out.println("   " + v);

                System.out.println("      ID: " + v.getAdvertisement().getIdentifier());
                System.out.println("      Model: " + v.getAdvertisement().getModel());
                System.out.println("      Model ID: " + v.getAdvertisement().getModelId());
                System.out.println("      Product ID: " + v.getAdvertisement().getProductId());
                System.out.println("      Address: " + v.getAddress());
                System.out.println("      Color: " + v.getColor());
                System.out.println("      charging? " + v.getAdvertisement().isCharging());
            }

            System.out.println("\nNow connecting to and doing stuff to your cars.\n\n");

            iter = vehicles.iterator();
            while (iter.hasNext()) {
                 v = iter.next();
                System.out.println("\nConnecting to " + v + " @ " + v.getAddress());
                v.connect();
                System.out.print("   Connected. Setting SDK mode...");   //always set the SDK mode FIRST!                
                v.sendMessage(new SdkModeMessage());
                System.out.println("   SDK Mode set.");

                System.out.println("   Sending asynchronous Battery Level Request. The Response will come in eventually.");
                //we have to set up a response handler first, in order to handle async responses
                BatteryLevelResponseHandler blrh = new BatteryLevelResponseHandler();
                //now we tell the car, who is listenening to the replies
                v.addMessageListener(BatteryLevelResponseMessage.class, blrh);
                //now we can actually send it.
                v.sendMessage(new BatteryLevelRequestMessage());

                System.out.println("   Sending Ping Request...");
                //again, some async set-up required...
                PingResponseHandler prh = new PingResponseHandler();
                v.addMessageListener(PingResponseMessage.class, prh);
                AnkiConnectionTest.pingSentAt = System.currentTimeMillis();
                v.sendMessage(new PingRequestMessage());
//
//                System.out.println("   Flashing lights...");
//                LightConfig lc = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STROBE, 0, 0, 0);
//                LightsPatternMessage lpm = new LightsPatternMessage();
//                lpm.add(lc);
//                v.sendMessage(lpm);
                System.out.println("   Setting Speed...");
                v.sendMessage(new SetSpeedMessage(500, 100));
//                RoadmapScanner myRoute = new RoadmapScanner();


                PositionHandler psrh = new PositionHandler();
                v.addMessageListener(LocalizationPositionUpdateMessage.class, psrh);


//                System.out.println(messenger.getLocationId());


                //Thread.sleep(1000);
                //gs.sendMessage(new TurnMessage());
                System.out.print("Running for 20secs... ");
                Thread.sleep(20000);
                v.disconnect();
                System.out.println("disconnected from " + v + "\n");
            }
        }
        anki.close();
        System.exit(0);
    }
    public static void stop(int num){
//        MyThread serverThread = new MyThread();
        if(num ==10 ){
        try{
                v.sendMessage(new SetSpeedMessage(0, 10000));
                //Server s = new Server(5001);
            //Client c = new Client("localhost",5001);

            Thread.sleep(3000);
            v.sendMessage(new SetSpeedMessage(500, 500));
            Thread.sleep(500);
            v.sendMessage(new SetSpeedMessage(500, 500));
        }catch (Exception e){
        e.printStackTrace();
        }
        }else{

        }

    }
//    public static class MyThread extends Thread {
//
//        public void run(){
//            System.out.println("MyThread running");
//            Client c = new Client("localhost",5001);
//            Server s = new Server(5001);
//        }
//    }
    public class Client
    {
        // initialize socket and input output streams
        private Socket socket            = null;
        private DataInputStream  input   = null;
        private DataOutputStream out     = null;

        // constructor to put ip address and port
        public Client(String address, int port)
        {
            // establish a connection
            try
            {
                socket = new Socket(address, port);
                System.out.println("Connected");

                // takes input from terminal
                input  = new DataInputStream(System.in);

                // sends output to the socket
                out    = new DataOutputStream(socket.getOutputStream());
            }
            catch(UnknownHostException u)
            {
                System.out.println(u);
            }
            catch(IOException i)
            {
                System.out.println(i);
            }

            // string to read message from input
            String line = "";

            // keep reading until "Over" is input
            while (!line.equals("Over"))
            {
                try
                {
                    line = input.readLine();
                    out.writeUTF(line);
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }

            // close the connection
            try
            {
                input.close();
                out.close();
                socket.close();
            }
            catch(IOException i)
            {
                System.out.println(i);
            }
        }
    }

    public static class Server
    {
        //initialize socket and input stream
        private Socket          socket   = null;
        private ServerSocket    server   = null;
        private DataInputStream in       =  null;

        // constructor with port
        public Server(int port)
        {
            // starts server and waits for a connection
            try
            {
                server = new ServerSocket(port);
                System.out.println("Server started");

                System.out.println("Waiting for a client ...");

                socket = server.accept();
                System.out.println("Client accepted");

                // takes input from the client socket
                in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));

                String line = "";

                // reads message from client until "Over" is sent
                while (!line.equals("Over"))
                {
                    try
                    {
                        line = in.readUTF();
                        System.out.println(line);

                    }
                    catch(IOException i)
                    {
                        System.out.println(i);
                    }
                }
                System.out.println("Closing connection");

                // close connection
                socket.close();
                in.close();
            }
            catch(IOException i)
            {
                System.out.println(i);
            }
        }
    }
    /**
     * Handles the response from the vehicle from the BatteryLevelRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     */
    private static class BatteryLevelResponseHandler implements MessageListener<BatteryLevelResponseMessage> {
        @Override
        public void messageReceived(BatteryLevelResponseMessage m) {
            System.out.println("   Battery Level is: " + m.getBatteryLevel() + " mV");
        }
    }

    /**
     * Handles the response from the vehicle from the PingRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     */
    private static class PingResponseHandler implements MessageListener<PingResponseMessage> {
        @Override
        public void messageReceived(PingResponseMessage m) {
            AnkiConnectionTest.pingReceivedAt = System.currentTimeMillis();
            System.out.println("   Ping response received. Roundtrip: " + (AnkiConnectionTest.pingReceivedAt - AnkiConnectionTest.pingSentAt) + " msec.");
        }
    }
    private static class PositionHandler implements MessageListener<LocalizationPositionUpdateMessage> {
        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            System.out.println("   Road piece ID: " + m.getRoadPieceId());
            stop(m.getRoadPieceId());
//            System.out.println(m.super.toString());
        }
    }
}
