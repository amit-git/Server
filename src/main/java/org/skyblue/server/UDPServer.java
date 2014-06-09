package org.skyblue.server;


import javax.management.*;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {

    private DatagramSocket datagramSocket;

    public void start() throws Exception{

        final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        PacketMetricsMBean packetMetricsMBean = new PacketMetrics();
        ObjectName objectName = new ObjectName("org.skyblue.server:name=udp-packets");
        platformMBeanServer.registerMBean(packetMetricsMBean, objectName);

        datagramSocket = new DatagramSocket(5555);

        byte[] buf = new byte[256];

        while (true) {
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
            datagramSocket.receive(datagramPacket);
            System.out.println("Data received " + new String(buf));
            incrementPacketCount(packetMetricsMBean);
        }
    }

    private void incrementPacketCount(PacketMetricsMBean packetMetricsMBean) {
        packetMetricsMBean.setNumOfPackets(packetMetricsMBean.getNumOfPackets() + 1);
    }

    public static void main(String[] args) {
        UDPServer udpServer = new UDPServer();
        try {
            udpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
