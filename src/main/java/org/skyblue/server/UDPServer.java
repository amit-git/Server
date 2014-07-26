package org.skyblue.server;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPServer {
    private static Logger logger = Logger.getLogger(UDPServer.class.getName());

    private volatile boolean keepRunning;

    public void run() throws Exception {
        final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        PacketMetricsMBean packetMetricsMBean = new PacketMetrics();
        ObjectName objectName = new ObjectName("org.skyblue.server:name=udp-packets");
        platformMBeanServer.registerMBean(packetMetricsMBean, objectName);

        DatagramSocket datagramSocket = new DatagramSocket(5555);
        byte[] buf = new byte[256];
        keepRunning = true;

        while (keepRunning) {
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
            datagramSocket.receive(datagramPacket);
            logger.log(Level.INFO, "Data received " + new String(buf));
            incrementPacketCount(packetMetricsMBean);
        }

        datagramSocket.close();
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    private void incrementPacketCount(PacketMetricsMBean packetMetricsMBean) {
        packetMetricsMBean.setNumOfPackets(packetMetricsMBean.getNumOfPackets() + 1);
    }

    public static void main(String[] args) {
        UDPServer udpServer = new UDPServer();
        try {
            udpServer.run();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "UDPServer exception, shutting down due to ", e);
        }
    }
}
