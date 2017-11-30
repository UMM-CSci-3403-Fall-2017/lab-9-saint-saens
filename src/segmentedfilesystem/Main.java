package segmentedfilesystem;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Main {
    
    public static void main(String[] args) {
        try {
            String address = "146.57.33.55";
            int port = 6014;
            DatagramSocket socket = new DatagramSocket(port);

            socket.connect(Inet4Address.getByName(address), port);

            // Send an empty packet to start the conversation
            byte[] emptyBuf = new byte[1];
            DatagramPacket start = new DatagramPacket(emptyBuf, 1, Inet4Address.getByName(address), port);

            socket.send(start);

            PacketProcessor file1 = new PacketProcessor();
            PacketProcessor file2 = new PacketProcessor();
            PacketProcessor file3 = new PacketProcessor();
            byte[] buf = new byte[1028];

            while(!file1.done || !file2.done || !file3.done){
                DatagramPacket receive = new DatagramPacket(buf, buf.length);
                socket.receive(receive);

                // This ensures the packets are of the right length
                byte[] packet = Arrays.copyOfRange(receive.getData(), 0, receive.getLength());

                // If none of the files (ArrayLists) have been written to
                if (file1.id == null) {
                    file1.id = packet[1];
                    file1.addPacket(packet);

                // If the first fileID matched the ID in the packet
                } else if (file1.id == packet[1]){
                    file1.addPacket(packet);

                // If the second file hasn't been written to
                } else if (file2.id == null) {
                    file2.id = packet[1];
                    file2.addPacket(packet);

                // If the second fileID matches the packet ID
                } else if (file2.id == packet[1]){
                    file2.addPacket(packet);

                // If the third file hasn't been written to
                } else if (file3.id == null){
                    file3.id = packet[1];
                    file3.addPacket(packet);

                // The only other option is a normal datapacket for the third file
                } else {
                    file3.addPacket(packet);
                }
            }

            file1.writeFile();
            file2.writeFile();
            file3.writeFile();

            socket.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
