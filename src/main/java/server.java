import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class server
{
    public static void main(String[] args)
    {
        Context context = ZMQ.context(1);

        //Gönderici soketi oluşturduk. Portunu belirledik.
        Socket socket = context.socket(ZMQ.PUB);
        socket.bind("tcp://localhost:5000");


        //Alıcı soketi oluşturuduk. Portunu belirledik.
        Socket receive = context.socket(ZMQ.PULL);
        receive.bind("tcp://localhost:5050");

        //İş parçacıgı olduğu sürece
        while (!Thread.currentThread().isInterrupted())
        {
            // Clientlerden gelen mesajları alır
            String alinan = receive.recvStr(0);
            System.out.println("Alınan: " + alinan);
            // Bütün clientlere gönderir.
            socket.send(alinan, 0);
        }

        receive.close();
        socket.close();
        //soketleri söküyor
        context.term();
    }
}