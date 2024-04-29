package com.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class NtripServer {
    private static final Map<String, MountPoint> mountPointMap=new HashMap<>();
    private static final AtomicInteger countClient=new AtomicInteger();

    public static final class MountPoint implements Runnable{
        private final Socket socket;
        private final List<Socket> clients=new ArrayList<>();

        public MountPoint(Socket socket){
            this.socket=socket;
        }

        public void addClient(Socket socket){
            this.clients.add(socket);
            System.out.println("Nb of client: "+countClient.incrementAndGet());
        }

        @Override
        public void run() {
            try {
                var buffer=new byte[1024];
                var baos = new ByteArrayOutputStream();
                var is = socket.getInputStream();
                socket.getOutputStream().write("ICY 200 OK\r\n\r\n".getBytes());
                while (true){
                    int len=is.read(buffer);
                    if (len>0){
                        this.clients.removeIf(c->{
                            try{
                                c.getOutputStream().write(buffer,0, len);
                                return false;
                            }catch (Exception e){
                                System.out.println("Nb of client: "+countClient.decrementAndGet());
                                return true;
                            }
                        });
                    }
                }
            }catch(Exception e){
                System.out.println("Nb of client: "+countClient.addAndGet(clients.size()));
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws IOException {
        System.out.println("Ntrip test server started");
        ServerSocket sockServer=new ServerSocket(2101);
        while (true){
            var sock=sockServer.accept();
            Thread.ofVirtual().start(()->processInit(sock));
        }
    }

    public static void processInit(Socket socket){
        try {
            var buffer = new byte[1024];
            var baos = new ByteArrayOutputStream();
            var is = socket.getInputStream();
            while (!baos.toString().contains("\r\n\r\n") && !baos.toString().contains("\n\n")) {
                int len = is.read(buffer);
                if (len==-1)return;
                baos.write(buffer, 0, len);
            }
            var req=baos.toString().lines().findFirst();
            if (req.isEmpty())return;
            var method=req.get().split(" ");
            if (method.length<3)return;
            if ("GET".equals(method[0])){
                String mp=method[1].replace("/","");
                if (mountPointMap.containsKey(mp)){
                    socket.getOutputStream().write("ICY 200 OK\r\n\r\n".getBytes());
                    mountPointMap.get(mp).addClient(socket);
                }else{
                    System.out.println("mountpoint not found for client "+mp);
                    socket.close();
                }
            } else if ("SOURCE".equals(method[0])) {
                var mp=new MountPoint(socket);
                mountPointMap.put(method[2], mp);
                System.out.println("Mountpoint size: "+mountPointMap.size()+" added "+method[2]);
                Thread.ofVirtual().name("MOUNT "+method[2]).start(mp);
            }else{
                System.out.println("Method "+method[0]+" not implemented");
                socket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
