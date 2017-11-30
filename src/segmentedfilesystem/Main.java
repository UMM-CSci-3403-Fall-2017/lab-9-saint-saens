package segmentedfilesystem;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Main {
    
    public static void main(String[] args) {
        try {
            String address = "146.57.33.55";
            DatagramSocket socket = new DatagramSocket(6014);
//            socket.connect(Inet4Address.getByAddress(address), 6014);

            socket.connect(Inet4Address.getByName(address), 6014);

            byte[] emptyBuf = new byte[1];
            DatagramPacket start = new DatagramPacket(emptyBuf, 1, Inet4Address.getByName(address), 6014);

            socket.send(start);

            PacketProcessor file1 = new PacketProcessor();
            PacketProcessor file2 = new PacketProcessor();
            PacketProcessor file3 = new PacketProcessor();

            while(!file1.done || !file2.done || !file3.done){
                byte[] buf = new byte[1028];
                DatagramPacket receive = new DatagramPacket(buf, buf.length);
                socket.receive(receive);

                byte[] packet = Arrays.copyOfRange(receive.getData(), 0, receive.getLength());
                //System.out.println(packet.length);

//                System.out.println("This packet has file id: " + buf[1]);

                //If none of the files (ArrayLists) have been written to
                if (file1.id == null) {
                    file1.id = packet[1];
                    file1.addPacket(packet);
//                    System.out.println("Starting file1 with id: " + file1.id);
                //If the first fileID matched the ID in the packet
                } else if (file1.id == packet[1]){
                    file1.addPacket(packet);
//                    System.out.println("Writing to file1");
                //If the second file hasn't been written to
                } else if (file2.id == null) {
                    file2.id = packet[1];
                    file2.addPacket(packet);
//                    System.out.println("Starting file2 with id: " + file2.id);
                //If the second fileID matches the packet ID
                } else if (file2.id == packet[1]){
                    file2.addPacket(packet);
//                    System.out.println("Writing to file2");
                // If the third file hasn't been written to
                } else if (file3.id == null){
                    file3.id = packet[1];
                    file3.addPacket(packet);
//                    System.out.println("Starting file3 with id: " + file3.id);
                // Here if
                } else {
                    file3.addPacket(packet);
//                    System.out.println("packets in file3: " + file3.dataPackets.size() + " length: " + file3.length +
//                            " done: " + file3.done + " packet index: " + (((packet[2] << 8) & 0xFFFF) ^ (packet[3]& 0xFF)));
//                    System.out.println("Writing to file3");
                }
            }

//            System.out.println("Done getting files");
            

            file1.writeFile();
            file2.writeFile();
            file3.writeFile();


            socket.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean done(ArrayList<byte[]> file){
        int length = 0;

        for (byte[] packet : file){
            System.out.println("Status byte: "+(packet[0] & 3));
            if ((packet[0] & 3) == 3){
                System.out.println("length should be "+(packet[2]<<8) + (packet[3]+1));
                length = (packet[2]<<8) + packet[3];
                break;
            }
        }
        System.out.println("length: "+length);
        if(length!= 0 && file.size()==length){
            System.out.println(length);
        }
        return length != 0 && file.size() == length;
    }

    private static class Comp implements Comparator<byte[]> {
        public int compare(byte[] byte1, byte[] byte2){
            if(byte1[0] % 2 == 0){
                return 1;
            } else if (byte2[0] % 2 == 0){
                return -1;
            }

            int shift1 = (byte1[2]<<8) + byte1[3];
            int shift2 = (byte2[2]<<8) + byte2[3];

            return shift2 - shift1;
        }
    }

    /*
     * Takes an ArrayList<byte[]> where each byte[] represents a single packet of a file. The arraylist is assumed to be
     * in the correct order (header first)
     *
     * Writes to a file with the name given in the header packet.
     */
    public static void writeFile(ArrayList<byte[]> packets) throws IOException{
        byte[] header = packets.get(0);
        String fileName = new String(Arrays.copyOfRange(header, 2, header.length));
//        File file = new File(fileName);
//        FileWriter writer = new FileWriter(file);
//
//        char[] toWrite;
//        for(int i = 1; i < packets.size(); i++){
//            byte[] packet = packets.get(i);
//            toWrite = new char[packet.length - 4];
//
//            for (int j = 4; j < packet.length; j++){
//                toWrite[j - 4] = (char) packet[j];
//            }
//
//            writer.write(toWrite);
//        }
//        writer.flush();
//        writer.close();
    }
}
