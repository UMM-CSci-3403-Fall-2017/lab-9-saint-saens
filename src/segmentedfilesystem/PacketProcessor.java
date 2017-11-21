package segmentedfilesystem;

import java.util.ArrayList;

public class PacketProcessor {
    public byte id;
    public String fileName;
    public byte[] header;
    public ArrayList<byte[]> dataPackets;
    public boolean done;
    public int length;

    public PacketProcessor(){
        done = false;
        dataPackets = new ArrayList<byte[]>();
        length = -1;
    }

    public void addPacket(byte[] byt){
        //the byte[] is the last data packet
        if((byt[0] & 11) == 3) {
            dataPackets.add(byt);
            length = (byt[2] << 8) + byt[3] + 1;
            checkDone();
        }
        //the byte[] is just a normal data packet
        else if ((byt[0] & 1) == 1){
            dataPackets.add(byt);
            checkDone();
        }
        //the byte[] is the header
        else {
            header = byt;
            //TODO: set fileName = to what it ought to be here
        }
    }

    public void checkDone(){
        if((length != -1) && (dataPackets.size() == length) && (header != null)){
            done = true;
        }
    }
}
