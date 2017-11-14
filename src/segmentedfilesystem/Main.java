package segmentedfilesystem;


import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Main {
    
    public static void main(String[] args) {
        try {
            byte[] address = {1, 1, 1, 1};
            DatagramSocket socket = new DatagramSocket(1337);
            socket.connect(Inet4Address.getByAddress(address), 1337);

            byte[] emptyBuf = new byte[1];
            DatagramPacket start = new DatagramPacket(emptyBuf, 1, Inet4Address.getByAddress(address), 1337);

            socket.send(start);

            ArrayList<byte[]> file1 = new ArrayList<byte[]>();
            ArrayList<byte[]> file2 = new ArrayList<byte[]>();
            ArrayList<byte[]> file3 = new ArrayList<byte[]>();
            byte file1id = -1;
            byte file2id = -1;
            byte file3id = -1;

            while(notDone(file1) || notDone(file2) || notDone(file3)){
                byte[] buf = new byte[1000];
                DatagramPacket receive = new DatagramPacket(buf, buf.length);
                socket.receive(receive);

                buf = receive.getData();

                if (file1id == -1) {
                    file1id = buf[1];
                    file1.add(buf);
                } else if (file1id == buf[1]){
                    file1.add(buf);
                } else if (file2id == -1) {
                    file2id = buf[1];
                    file2.add(buf);
                } else if (file2id == buf[1]){
                    file2.add(buf);
                } else if (file3id == -1){
                    file3id = buf[1];
                    file3.add(buf);
                } else {
                    file3.add(buf);
                }
            }

            file1.sort(new Comp());
            file2.sort(new Comp());
            file3.sort(new Comp());

            

            socket.disconnect();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean notDone(ArrayList<byte[]> file){
        int length = 0;

        for (byte[] packet : file){
            if (packet[0] % 4 == 3){
                length = packet[2];
                break;
            }
        }

        return length != 0 && file.size() == length;
    }

    private static class Comp implements Comparator<byte[]> {
        public int compare(byte[] byte1, byte[] byte2){
            if(byte1[0] % 2 ==0){
                return -1;
            } else if (byte2[0] % 2 ==0){
                return 1;
            }
            return byte2[2] - byte1[2];
        }
    }
}
