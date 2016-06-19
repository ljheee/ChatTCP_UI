package com.ljheee.example.sercer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class Server extends JFrame
{
    boolean started = false;
    private ServerSocket ss = null;
    private List clientList = new LinkedList();
    private JLabel portLbl = null;
    private JTextField portTxt = null;
    private JButton portSetBtn = null;
    private String port = null;
    private JButton startBtn = null;
    private JButton stopBtn = null;
    private JPanel mainPanle = null;
    private JPanel headPanle = null;
    
    public static void main(String[] args)
    {
        new Server();
    }
    
    public Server()
    {
        headPanle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portLbl = new JLabel("PORT");
        portTxt = new JTextField(7);
        portSetBtn = new JButton("OK");
        
        portSetBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (portTxt.getText().matches("\\d+"))
                {
                    port = portTxt.getText();
                    startBtn.setEnabled(true);
                    stopBtn.setEnabled(true);
                }
                else
                {
                    javax.swing.JOptionPane.showMessageDialog(null, "port be inputted is illegal");
                }
            }
        });
        headPanle.add(portLbl);
        headPanle.add(portTxt);
        headPanle.add(portSetBtn);
        getContentPane().add(headPanle, BorderLayout.NORTH);
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        startBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        mainPanle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startBtn.addActionListener(new StartClickListener());
        stopBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                started = false;
                clientList.clear();
                try
                {
                    if (ss != null)
                    {
                        ss.close();
                    }
                }
                catch(IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });
        mainPanle.add(startBtn);
        mainPanle.add(stopBtn);
        getContentPane().add(mainPanle, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                started = false;
                clientList.clear();
                try
                {
                    if (ss != null)
                    {
                        ss.close();
                    }
                }
                catch(IOException e1)
                {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
        this.setSize(300, 300);
        setLocation(100, 100);
        pack();
        setVisible(true);
    }
    private void start()
    {
        try
        {
            ss = new ServerSocket(Integer.parseInt(port));
            started = true;
        }
        catch(BindException be)
        {
            javax.swing.JOptionPane.showMessageDialog(null, "port is useing by others");
        }
        catch(IOException e)
        {
            javax.swing.JOptionPane.showMessageDialog(null, "connect server fail");
        }
        try
        {
            while(started)
            {
                Socket s = ss.accept();
                ClientImpl cr = new ClientImpl(s);
                new Thread(cr).start();
                clientList.add(cr);
                System.out.println("System Info：" + s.getInetAddress() + "connect successfully");
            }
        }
        catch(IOException e)
        {
            System.out.println("Client closed!");
        }
    }
    
    class ClientImpl implements Runnable
    {
        private Socket s = null;
        private DataInputStream dis = null;
        private DataOutputStream dos = null;
        boolean bConnect = false;
        public ClientImpl(Socket s)
        {
            this.s = s;
            try
            {
                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());
                bConnect = true;
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        private void send(String str)
        {
            try
            {
                dos.writeUTF(str);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        /**
         * 给clientList里  每一个客户端socket发数据
         */
        public void run()
        {
            ClientImpl cr = null;
            try
            {
                while(bConnect)
                {
                    String str = dis.readUTF();
                    System.out.println(str);
                    for(int i = 0; i < clientList.size(); i++)
                    {
                        cr = (ClientImpl) clientList.get(i);
                        cr.send(str);
                    }
                }
            }
            catch(Exception e)
            {
                clientList.remove(cr);
                System.out.println(s.getInetAddress() + "has leaved");
            }
            finally
            {
                try
                {
                    if (dis != null)
                        dis.close();
                    if (dos != null)
                        dos.close();
                    if (s != null)
                    {
                        s.close();
                        s = null;
                    }
                }
                catch(IOException io)
                {
                    io.printStackTrace();
                }
            }
        }
    }
    class StartClickListener implements Runnable, ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            new Thread(this).start();
        }
        public void run()
        {
            start();
        }
    }
}