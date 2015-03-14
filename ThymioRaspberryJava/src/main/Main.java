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
	//time in seconds
	private static final int CAPTURE_TIME = 2;
	
	private ThymioRemoteConnection controller;
	
	public Main() {
		try {
			controller = thymioConnect();
			
			addShutdownHooks();
			
			ArrayList<Short> frontReadings = new ArrayList<Short>();
			
			long startTime = System.currentTimeMillis();
			boolean exit = false;
			
			Scanner scanner = new Scanner(System.in);
			System.out.print("Distance to the wall(cm): ");
			String distance = scanner.nextLine();
			scanner.close();
			
			while(!exit){
				System.out.println("Starting Capture");
				List<Short> readings = controller.getProximitySensorValues();
				frontReadings.add(readings.get(2));
				
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
		new Main();
	}
	
}
