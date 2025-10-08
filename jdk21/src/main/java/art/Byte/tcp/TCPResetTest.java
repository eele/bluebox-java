package art.Byte.tcp;

import java.io.IOException;
import java.net.Socket;

public class TCPResetTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < 3; i++) {
            Socket socket = new Socket("", 6379);
            socket.setSoLinger(true, 0);
        }
        Thread.sleep(6000);
        System.gc();
        System.out.println("gc over");
        Thread.sleep(10000000);
    }
}
