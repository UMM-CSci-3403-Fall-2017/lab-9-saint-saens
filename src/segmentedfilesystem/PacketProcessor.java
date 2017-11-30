package segmentedfilesystem;

import java.io.*;
import java.util.ArrayList;

public class PacketProcessor {
    public Byte id;
    public String fileName;
    public byte[] header;
    public ArrayList<byte[]> dataPackets;
    public boolean done;
    public int length;

    /* 
     * The constructor initializes all fields that need to not be null
     */
    public PacketProcessor(){
        done = false;
        dataPackets = new ArrayList<byte[]>();
        length = -1;
    }

    /*
     * takes a data packet represented as a byte[] and returns its index as an int.
     */
    public int getIndex(byte[] packet){
        return (((packet[2] << 8) & 0xFFFF) ^ (packet[3]& 0xFF));
    }

    /*
     * Adds a packet represented as a byte[] in the correct spot
     */
    public void addPacket(byte[] byt){
        // the byte[] is just a normal data packet
        if ((byt[0] & 1) == 1){
            int index = getIndex(byt);

            // Loop backwards until you find the right spot
            int i;
            for (i = dataPackets.size() - 1; i >= 0; i--){
                byte[] packet = dataPackets.get(i);
                int packIndex = getIndex(packet);

                // If the index we are at is less than the index of the packet we are processing, the next spot is the
                // correct spot
                if (packIndex < index){
                    dataPackets.add(i + 1, byt);
                    break;
                }
            }
            // The loop was never entered because this is the first data packet we've received.
            if(i == -1){
                dataPackets.add(0,byt);
            }

            checkDone();
        }
        
        //the byte[] is the header
        else {
            header = byt;
            checkDone();
            setFileName();
        }
    }

    /*
     * sets done = true if we have received all of the datapackets and the header. Also sets the length if we have
     * received the last packet.
     */
    public void checkDone(){
        int size = dataPackets.size();

        // If we haven't received any packets yet, we're definitely not done. Also we'd get an IndexOutOfBoundException
        // without this if.
        if (size == 0){
            return;
        }

        // If the last packet in the dataPackets array has a status divisible by 3, then it's the last data packet and
        // we set the length equal to its index as given in the packet
        byte[] last = dataPackets.get(size - 1);
        if ((last[0] & 11) == 3){
            length = getIndex(last) + 1;
        }

        // If we have the proper length and the header, we're good to go
        if((dataPackets.size() == length) && (header != null)){
            done = true;
        }
    }

    /*
     * extracts the file name from the header, assumes the header is not null
     */
    public void setFileName(){
        String name = "";
        for(int i = 2; i < header.length; i++){
            if (header[i] == 0){
                break;
            }
            name = name + (char) header[i];
        }
        fileName = name;
    }

    /*
     * Assumes all of the datapackets are in and the fileName is not null.
     * Writes a file from the datapackets.
     */
    public void writeFile() throws IOException {
        System.out.println("File name: " + fileName);
        File file = new File(fileName);
        DataOutputStream writer = new DataOutputStream(new FileOutputStream(file));

        for(byte[] packet : dataPackets){
            // Start at 4 because the first 4 bytes are status, file ID, and index
            writer.write(packet, 4, packet.length - 4);
        }
        
        writer.flush();
        writer.close();
    }

}
