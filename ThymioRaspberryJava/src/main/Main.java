package main;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import ch.epfl.mobots.AsebaNetwork;
import ch.epfl.mobots.Aseba.ThymioRemoteConnection;

public class Main {
	private static final int CAPTURE_TIME = 2;
	private int capturingSensor;
	
	private ThymioRemoteConnection controller;
	
	public Main(String[] args) {
		try {
			controller = thymioConnect();
			
			addShutdownHooks();
			
			ArrayList<Short> frontReadings = new ArrayList<Short>();
			
			boolean exit = false;
			
			if(args.length == 0){
				System.err.print("Nº of the sensor not specified on the arguments");
				System.exit(0);
			}
				
			capturingSensor = Integer.valueOf(args[0]);
			
			Scanner scanner = new Scanner(System.in);
			System.out.print("Distance to wall (cm): ");
			String distance = scanner.nextLine();
			scanner.close();
			
			System.out.println("Capturing during " + CAPTURE_TIME + "s at a distance to the wall of (cm): ");
			
			long startTime = System.currentTimeMillis();
			System.out.println("Starting Capture");
			
			while(!exit){
				
				List<Short> readings = controller.getProximitySensorValues();
				frontReadings.add(readings.get(capturingSensor));
				
				double passedTime = (System.currentTimeMillis() - startTime) / 1000;
				if(passedTime >= CAPTURE_TIME)
					exit = true;
			}
			
			System.out.println("Saving to File");
			
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new File(distance + ".txt"));
				for (Short r : frontReadings) {
					writer.println(r);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(writer != null)
					writer.close();
			}
			
			System.out.println("File Saved");
			System.exit(0);
		} catch (DBusException e1) {
			e1.printStackTrace();
		}
		
	}
	
	private ThymioRemoteConnection thymioConnect() throws DBusException {
		DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
		AsebaNetwork recvInterface = (AsebaNetwork) conn.getRemoteObject(
				"ch.epfl.mobots.Aseba", "/", AsebaNetwork.class);
		return new ThymioRemoteConnection(recvInterface);
	}

	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				controller.setTargetWheelSpeed((short) 0, (short) 0);
			}
		});
	}
	
	public static void main(String[] args) {
		new Main(args);
	}
	
}
