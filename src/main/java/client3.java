import java.util.Scanner;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQ.Poller;
import java.util.Date;

public class client3
{
    private static Scanner in = new Scanner(System.in);

    private static class Sender extends Thread
    {
        private String name;
        private Socket socket;

        public Sender(String name, Socket socket)
        {
            this.name = name;
            this.socket = socket;
            Date dt= new Date();
            socket.send(dt.toString()+" "+name + " odaya baglandi");
        }

        public void run()
        {
            while (!Thread.currentThread().isInterrupted())
            {
                Date dt= new Date();
                String messageToSend = in.nextLine();
                socket.send(dt.toString()+" "+name + ": " + messageToSend);
            }
        }
    }

    private static class Receiver extends Thread
    {
        private  Poller poller;
        private  Socket receive;

        public Receiver(Poller poller, Socket receive)
        {
            this.poller = poller;
            this.receive = receive;
        }

        public void run()
        {
            while (!Thread.currentThread().isInterrupted())
            {
                int events = poller.poll();
                if (events > 0)
                {
                    String recvMessage = receive.recvStr(0);
                    System.out.println(recvMessage);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        Context context = ZMQ.context(1);

        Socket socket = context.socket(ZMQ.PUSH);
        socket.connect("tcp://localhost:5050");

        Socket receive = context.socket(ZMQ.SUB);
        receive.connect("tcp://localhost:5000");
        receive.subscribe("".getBytes());

        Poller poller = context.poller();
        poller.register(receive, Poller.POLLIN);

        System.out.print("Nickname : ");
        String name = in.nextLine();

        try
        {
            Sender sender = new Sender(name, socket);
            sender.start();

            Receiver receiver = new Receiver(poller, receive);
            receiver.start();

            //Geçerli iş parçacığı, çağrıldığı iş parçacığı ölünceye kadar bekletilir.
            sender.join();
            receiver.join();
        }
        catch (InterruptedException e) {}
        finally
        {
            receive.close();
            socket.close();
            context.term();// Soketleri söker
        }
    }
}