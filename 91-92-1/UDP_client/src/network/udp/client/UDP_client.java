package network.udp.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UDP_client extends Activity implements OnClickListener
{

    private static final String SERVER_IP   = "10.0.2.2"; // Actually my host have this address as localhost!
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

    public class Client implements Runnable
    {

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
            try
            {
                InetAddress server_address = InetAddress.getByName(SERVER_IP);
                DatagramSocket client_socket = new DatagramSocket(CLIENT_PORT);
                byte[] buffer;
                if ( !txt_input.getText().toString().isEmpty())
                {
                    buffer = txt_input.getText().toString().getBytes();
                }
                else
                {
                    buffer = (" faza khali bood:D ").getBytes();
                }
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, server_address, SERVER_PORT);
                client_socket.send(packet);
                client_socket.close();
            }
            catch (Exception e)
            {
                updatetrack("Client: Error!\n");
            }
        }
    }



    @Override
    public void onClick(View v)
    {
        new Thread(new Client()).start();
    }



    public void updatetrack(String s)
    {
        Message msg = new Message();
        String textTochange = s;
        msg.obj = textTochange;
        Handler.sendMessage(msg);
    }
}