package com.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NtripProducer  implements Runnable{
    public static void main(String[] args) throws IOException {
        ServerSocket sockServer=new ServerSocket(5015);
        System.out.println("Ntrip producer listen on 5015");
        while (true){
            var sock=sockServer.accept();
            new Thread(new NtripProducer(sock)).start();
        }
    }

    private final Socket socket;
    public NtripProducer(Socket sock) {
        this.socket=sock;
    }

    @Override
    public void run() {
        int iteration=0;
        try {
            var out=socket.getOutputStream();
            while (true){
                System.out.println("iteration: "+iteration);
                out.write(("iteration: "+iteration+"\r\n"+ "-".repeat(750)).getBytes());
                iteration++;
                out.flush();

                Thread.sleep(500);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
