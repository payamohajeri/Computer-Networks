package com.androidituts.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UdpActivity extends Activity implements OnClickListener
{

    private static final String CLIENT_IP   = "10.0.2.2"; // Actually my host have this address as localhost!
    private static final int    SERVER_PORT = 6000;
    private static final int    CLIENT_PORT = 8000;

    private TextView            txt_caption;
    private EditText            txt_input;
    private Button              btn_send;
    private Handler             Handler;
    private Button              btn_exit;



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        txt_caption = (TextView) findViewById(R.id.textView1);
        txt_input = (EditText) findViewById(R.id.editText1);
        btn_send = (Button) findViewById(R.id.button1);
        btn_exit = (Button) findViewById(R.id.ext);
        btn_send.setOnClickListener(this);

        new Thread(new Server()).start();
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {}

        Handler = new Handler()
        {

            @Override
            public void handleMessage(Message msg)
            {
                String text = (String) msg.obj;
                txt_caption.append(text);
            }
        };

        btn_exit.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                finish();
                System.exit(0);
            }
        });
    }

    public class Server implements Runnable
    {

        private DatagramSocket socket;
        private DatagramPacket packet;
        private String         msg;



        public Server()
        {

            try
            {
                InetAddress client_address = InetAddress.getByName(CLIENT_IP);
                socket = new DatagramSocket(SERVER_PORT);
                byte[] buffer = new byte[17];
                packet = new DatagramPacket(buffer, buffer.length);
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            catch (SocketException e)
            {
                e.printStackTrace();
            }

        }



        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    socket.receive(packet);
                }
                catch (Exception e)
                {
                    updatetrack("Server: Error!\n");
                }
                msg = new String(packet.getData());
                updatetrack("server : " + msg);
                updatetrack("ip: " + packet.getAddress().getHostAddress());
                updatetrack("port: " + packet.getPort());
            }
        }
    }



    @Override
    public void onClick(View v)
    {
        //new Thread(new Client()).start();
    }



    public void updatetrack(String s)
    {
        Message msg = new Message();
        String textTochange = s;
        msg.obj = textTochange;
        Handler.sendMessage(msg);
    }
}