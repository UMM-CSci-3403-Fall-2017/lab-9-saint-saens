package segmentedfilesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class PacketProcessor {
    public Byte id;
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
//        //the byte[] is the last data packet
//        if((byt[0] & 11) == 3) {
//            dataPackets.add(byt);
//            length = (byt[2] << 8) + byt[3] + 1;
//            checkDone();
//        }
        //the byte[] is just a normal data packet
        if ((byt[0] & 1) == 1){
            int index = (((byt[2] << 8) & 0xFFFF) ^ (byt[3]& 0xFF));

            //Loop backwards until you find the right spot
            int i;
            for (i = dataPackets.size() - 1; i >= 0; i--){
                byte[] packet = dataPackets.get(i);
                int packIndex = (((packet[2] << 8) & 0xFFFF) ^ (packet[3]& 0xFF));

                if (packIndex < index){
                    dataPackets.add(i + 1, byt);
                    break;
                }
            }
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

    public void checkDone(){
        int size = dataPackets.size();
        if (size == 0){
            return;
        }
        byte[] last = dataPackets.get(size - 1);
//        System.out.println((last[0] & 11) + " and packet index: " + (((last[2]<< 8) & 0xFFFF) ^ (last[3] & 0xFF)));
        if ((last[0] & 11) == 3){
            length = (((last[2]<< 8) & 0xFFFF) ^ (last[3] & 0xFF)) + 1;
        }

        if((length != -1) && (dataPackets.size() == length) && (header != null)){
            done = true;
        }
    }

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

    public void writeFile() throws IOException {
        System.out.println("File name: " + fileName);
        File file = new File(fileName);
//        FileWriter writer = new FileWriter(file);
        DataOutputStream writer = new DataOutputStream(new FileOutputStream(file));

        byte[] toWrite;
        for(byte[] packet : dataPackets){

            toWrite = new byte[packet.length - 4];
            int j;
            for (j = 4; j < packet.length; j++){
//                if (packet[j] == 0 || packet[j] == 4) {
//                    break;
//                }
//                writer.write(packet[j]);
                toWrite[j-4] = packet[j];
//                System.out.println("character: " + (char) packet[j] + " integer value: " + packet[j]);
            }

            writer.write(toWrite, 0, toWrite.length);
        }
        writer.flush();
        writer.close();
    }
    
}
