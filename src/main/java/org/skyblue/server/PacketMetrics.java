package org.skyblue.server;


public class PacketMetrics implements PacketMetricsMBean {
    private int numPackets;

    @Override
    public void setNumOfPackets(int num) {
        this.numPackets = num;
    }

    @Override
    public int getNumOfPackets() {
        return this.numPackets;
    }
}
