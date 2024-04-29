package com.test;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NtripLoadTest {
    public static final String TARGETTED_SERVER="127.0.0.1";
    public static final String casterPassword="letmein";
    public static final int packetSize = 1500;
    public static final int mountNb=1000;


    public static void main(String[] args) throws Exception {
        System.out.println("Will launch "+mountNb+" mountpoint + 90%*3 clients");
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < mountNb; i++) {
            int mountId=i;
            threadList.add(Thread.ofVirtual().start(() -> sendThread(mountId)));
        }
        Thread.sleep(5000);
        for (int i = 0; i < mountNb; i++) {
            if (i % 10 == 0) {
                continue;
            }
            for (int j = 0; j < 3; j++) {
                int mountId=i;
                int multiplier=j;
                threadList.add(Thread.ofVirtual().start(() -> receiveThread(mountId, multiplier)));
            }
        }
        threadList.forEach(x -> {
            try {
                x.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public static void receiveThread(int mountId, int multiplier) {
        try {
            Thread.sleep(random.nextInt(1000));
            receive(mountId, multiplier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Random random=new Random();
    public static void sendThread(int mountId) {
        try {
            Thread.sleep(random.nextInt(1000));
            send(mountId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void receive(int mountId, int multiplier) throws Exception {
        String request = """
                GET /TEST$i HTTP/1.0
                User-Agent: NTRIP LOADTEST/0.0.0
                Authorization: Basic Y2VudGlwZWRlOmxldG1laW4=
                
                """.replace("$i", ""+mountId);

        Socket sock = new Socket(TARGETTED_SERVER, 2101);

        var os = sock.getOutputStream();
        var in = sock.getInputStream();
        os.write(request.getBytes());
        String s = "";
        byte[] b = new byte[10024];
        while (!s.endsWith("\n\n") && !s.endsWith("\r\n\r\n")) {
            int len = in.read(b);
            if (len < 0) {
                System.out.println("Error on receive: " + s);
                return;
            }
            s += new String(b, 0, len);
            //System.out.println(s);
        }
        System.out.println("ok");
        while (true) {
            int len = in.read(b);
            if (len==0){
                continue;
            }
            //System.out.println("<-"+mountId+" "+multiplier);
        }

    }


    public static void send(int mountId) throws Exception {
        Socket sock = new Socket(TARGETTED_SERVER, 2101);
        String request = """
                SOURCE $passwd TEST$i
                Source-Agent: NTRIP LOADTEST/0.0.0
                STR:
                
                """.replace("$i", ""+mountId)
                .replace("$passwd", casterPassword);
        var os = sock.getOutputStream();
        os.write(request.getBytes());
        String s = "";
        while (!s.endsWith("\n\n") && !s.endsWith("\r\n\r\n")) {
            byte[] b = new byte[1024];
            int len = sock.getInputStream().read(b);
            s += new String(b, 0, len);
            System.out.println(s);
        }
        System.out.println("ok");
        int iteration = 0;
        while (true) {
            os.write(("iteration: " + iteration + "\r\n" + "-".repeat(packetSize)).getBytes());
            iteration++;
            os.flush();
            //System.out.println("->"+mountId);
            Thread.sleep(1000);
        }
    }
}
