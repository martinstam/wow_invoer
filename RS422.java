/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wow_invoer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author stam
 */
public class RS422 {
   
   
   
   
   /***********************************************************************************************/
   /*                                                                                             */
   /*                                                                                             */
   /*                                                                                             */
   /***********************************************************************************************/
   public void RS422_Check_Serial_Ports()
   {
      String info;
      
      
      SerialPort serialPort_test = new SerialPort(main_wow.COM_port_name);
      try 
      {
         // Open port
         serialPort_test.openPort();
         serialPort_test.closePort();
         
         // OK, no problems encoutered
         SIAM_defaultPort = main_wow.COM_port_name;
         info = "[SIAM] " + main_wow.COM_port_name + " SIAM connection OK";
         System.out.println(info);
         
         final JOptionPane pane_begin = new JOptionPane(info, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
         final JDialog checking_ports_begin_dialog = pane_begin.createDialog(main_wow.APPLICATION_NAME);
         Timer timer_begin = new Timer(1000, new ActionListener()
         {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               checking_ports_begin_dialog.dispose();
            }
         });
         timer_begin.setRepeats(false);
         timer_begin.start();
         checking_ports_begin_dialog.setVisible(true);            
      }
      catch (SerialPortException ex) 
      {
         // error opening predefined (by the user)port, reset/warnings
         info = "[SIAM] " + main_wow.COM_port_name + " SIAM not available";
         main_wow.jTextField1.setText(info + " (" + ex + ")");
         System.out.println(info + " (" + ex + ")");
         JOptionPane.showMessageDialog(null, info, main_wow.APPLICATION_NAME, JOptionPane.WARNING_MESSAGE);
         SIAM_defaultPort = null;
      }       
      
   }
   
   
   
   /***********************************************************************************************/
   /*                                                                                             */
   /*                                                                                             */
   /*                                                                                             */
   /***********************************************************************************************/
   public void RS422_initComponents()
   {   
      RS422_Check_Serial_Ports();
      
      
      if (SIAM_defaultPort != null)
      {   
         //System.out.println("+++ GPS_defaultPort = " + GPS_defaultPort);
      
         SIAM_serialPort = new SerialPort(SIAM_defaultPort);  // NB SIAM_defaultPort was determined in Function:  RS422_Check_Serial_Ports()

         new SwingWorker<String, String>()
         {
            @Override
            protected String doInBackground() throws Exception
            {
		         try
               {
                  SIAM_serialPort.openPort();               // NB will be closed in Function:  File_Exit_menu_actionPerformd() [main.java]
                  
                  //SIAM_serialPort.setParams(4800, 8, 1, 0);  // parity none = 0
                  SIAM_serialPort.setParams(main_wow.COM_bps, main_wow.COM_data_bits, main_wow.COM_stop_bits, main_wow.COM_parity);  // parity none = 0
                  
                  SIAM_serialPort.setFlowControlMode(0);
                  
                  // Preparing a mask. In a mask, we need to specify the types of events that we want to track.
                  // Well, for example, we need to know what came some data, thus in the mask must have the
                  // following value: MASK_RXCHAR. If we, for example, still need to know about changes in states 
                  // of lines CTS and DSR, the mask has to look like this: SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR
                  int mask = SerialPort.MASK_RXCHAR;
                  
                  //Set the prepared mask
                  SIAM_serialPort.setEventsMask(mask);   
                  
                  //System.out.println("+++ passed try block");
                  
    		      }
               catch (SerialPortException ex) 
               {
                  System.out.println("+++ " + ex);
               }               
   
	            SIAM_serialPort.addEventListener(new SerialPortEventListener()
               {
                  StringBuilder message = new StringBuilder();
                  
                  @Override
                  public void serialEvent(SerialPortEvent event)
                  {
                     String ontvangen_SIAM_string = "";
                        
                     if (event.isRXCHAR() && event.getEventValue() > 0)                               // NB event.getEventValue() gives number of received bytes
                     {
                        try 
                        {
                           // onderstaande werkt 
                           //ontvangen_GPS_string = GPS_serialPort.readString(event.getEventValue());
                           //System.out.println("+++ Received response: " + ontvangen_GPS_string);     // in case GPS always return 1 char
                           
                           // onderstaande werkt 
                           //int bytesCount = event.getEventValue();
                           //System.out.print(GPS_serialPort.readString(bytesCount)); // komplete zinnen op scherm                          
                           
                           byte buffer[] = SIAM_serialPort.readBytes();
                           for (byte b: buffer) 
                           {
                              if ((b == '\r' || b == '\n') && message.length() > 0) 
                              {
                                 ontvangen_SIAM_string = message.toString();
                            
                                 //if ((ontvangen_SIAM_string.indexOf("$GPRMC") != -1) || (ontvangen_SIAM_string.indexOf("$GLRMC") != -1))  // (GP for a GPS unit, GL for a GLONASS)
                                 //{
                                 //   RS232_GPS_NMEA_0183_RMC(ontvangen_SIAM_string);
                                 //}
                                 System.out.println(ontvangen_SIAM_string);
                                 main_wow.jTextField1.setText(ontvangen_SIAM_string);
                                 
                                 message.setLength(0);
                              }
                              else 
                              {
                                 message.append((char)b);
                              }
                           } // for (byte b: buffer)                           
                        } // try
                        catch (SerialPortException ex) 
                        {
                           String info = "Error in receiving string from COM-port: " + ex;
                           System.out.println(info);
                           main_wow.jTextField1.setText(info);
                        }                                                  
                     } // if (event.isRXCHAR() && event.getEventValue() > 0)                  
                        
                  } // public void serialEvent(SerialPortEvent spe)
               });

               return null;               
               
            } // protected Void doInBackground() throws Exception
         }.execute(); // new SwingWorker<String, String>()
      } // if (main.defaultPort != null)   
   }   
   
   
   
   /***********************************************************************************************/
   /*                                                                                             */
   /*                                                                                             */
   /*                                                                                             */
   /***********************************************************************************************/
   public void TCP_initComponents()
   {   
      //String ontvangen_SIAM_string = "";
      
      
      new SwingWorker<String, String>()
      {
         @Override
         protected String doInBackground() throws Exception
         {
            try 
            {
               String ontvangen_SIAM_string = "";
         
      
               Socket ftp_socket = new Socket(main_wow.TCP_host_name, Integer.parseInt(main_wow.TCP_port_name));
               //PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
               BufferedReader in = new BufferedReader(new InputStreamReader(ftp_socket.getInputStream()));

               while ((ontvangen_SIAM_string = in.readLine()) != null) 
               {
                  System.out.println(ontvangen_SIAM_string);
                  main_wow.jTextField1.setText(ontvangen_SIAM_string);
               }
            }
            catch (NumberFormatException | IOException ex)
            {
               String info = "TCP socket error: " + ex;
               System.out.println(info);  
               main_wow.jTextField1.setText(info);
            }
            
            return null; 
         }
      }.execute();           
      
   }
   
   
   
   private String SIAM_defaultPort                                                  = null;
   private SerialPort SIAM_serialPort;   

   

}
