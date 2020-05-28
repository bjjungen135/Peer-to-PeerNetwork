package cs455.overlay.util;

import java.util.HashMap;

import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;

public class StatisticsCollectorAndDisplay {
	
	HashMap<Integer, OverlayNodeReportsTrafficSummary> trafficCollector;
	int nodeNumbers;
	int[] nodeIds;
	
	//Class that collects all the traffic summary and prints the data in a readable format when ready
	public StatisticsCollectorAndDisplay(int nodesInSystem, int[] nodeIds) {
		this.trafficCollector = new HashMap<>();
		this.nodeNumbers = nodesInSystem;
		this.nodeIds = nodeIds;
	}

	public void addTraffic(int id, OverlayNodeReportsTrafficSummary trafficSummary) {
		this.trafficCollector.put(id, trafficSummary);
	}
	
	public int getCollectorSize() {
		return this.trafficCollector.size();
	}

	public void printTrafficSummary() {
		int totalSent = 0;
		int totalReceived = 0;
		long totalRelayed = 0;
		long totalSentSum = 0;
		long totalReceivedSum = 0;
		OverlayNodeReportsTrafficSummary traffic;
		System.out.printf("%-10s %-20s %-20s %-20s %-20s %-20s\n", "", "Packets Sent", "Packets Received", "Packets Relayed", "Sum Values Sent", "Sum Values Received");
		for(int id : nodeIds) {
			traffic = trafficCollector.get(id);
			String temp = "Node " + id;
			System.out.printf("%-10s %-20d %-20d %-20d %-20d %-20d\n", temp, traffic.getTrafficPacketsSent(), traffic.getTrafficPacketsReceived(), traffic.getTrafficPacketsRelayed(),traffic.getTrafficSentSum(), 
					traffic.getTrafficReceivedSum());
			totalSent += traffic.getTrafficPacketsSent();
			totalReceived += traffic.getTrafficPacketsReceived();
			totalRelayed += traffic.getTrafficPacketsRelayed();
			totalSentSum += traffic.getTrafficSentSum();
			totalReceivedSum += traffic.getTrafficReceivedSum();
			
		}
		System.out.printf("%-10s %-20d %-20d %-20d %-20d %-20d\n", "Sum", totalSent, totalReceived, totalRelayed, totalSentSum, totalReceivedSum);
		trafficCollector.clear();
	}

}
