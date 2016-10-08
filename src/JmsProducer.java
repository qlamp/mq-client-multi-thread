

// SCCSID "@(#) MQMBID sn=p900-L160518.TRIAL su=_7Y4jrR0LEearh6Qyg9d9Dg pn=MQJavaSamples/jms/JmsProducer.java"
/*
 *   <copyright 
 *   notice="lm-source-program" 
 *   pids="5724-H72,5655-R36,5655-L82,5724-L26" 
 *   years="2008,2016" 
 *   crc="279216363" > 
 *   Licensed Materials - Property of IBM  
 *    
 *   5724-H72,5655-R36,5655-L82,5724-L26 
 *    
 *   (C) Copyright IBM Corp. 2008, 2016 All Rights Reserved.  
 *    
 *   US Government Users Restricted Rights - Use, duplication or  
 *   disclosure restricted by GSA ADP Schedule Contract with  
 *   IBM Corp.  
 *   </copyright> 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * A JMS producer (sender or publisher) application that sends a simple message to the named
 * destination (queue or topic).
 *
 * Notes:
 *
 * API type: IBM JMS API (v1.1, unified domain)
 *
 * Messaging domain: Point-to-point or Publish-Subscribe
 *
 * Provider type: IBM MQ
 *
 * Connection mode: Client connection
 *
 * JNDI in use: No
 *
 * Usage:
 *
 * JmsProducer -m queueManagerName -d destinationName [-h host -p port -l channel] [-u user -w passWord]
 *
 * for example:
 *
 * JmsProducer -m QM1 -d Q1
 *
 * JmsProducer -m QM1 -d topic://foo -h localhost -p 1414 -u tester -w testpw
 */
public class JmsProducer implements Runnable{

  private static String host = "124.160.25.254";
  private static int port = 1414;
  private static String channel = "SvrConn3";
  private static String user = "adams.dong";
  private static String password = "Goodluck12#";
  private static String queueManagerName = "QMG2";
  private static String destinationName = "LocalQueue1";
  private static boolean isTopic = false;
  private static boolean clientTransport = true;

  // System exit status value (assume unset value to be 1)
  private static int status = 1;

  public static void main(String[] args) {
	Runnable r = new JmsProducer();
	for(int i = 0; i<1000000; i++){
		new Thread(r).start();
		System.out.println("Producer Thread "+i+" started");
		try{
		Thread.sleep(200);
		}catch(Exception e){
			
		}
	}
}
  public void run(){
	  test();
  }
  /**
   * Main method
   *
   * @param args
   */
  public static void test() {
    // Parse the arguments
    //parseArgs(args);

    // Variables
    Connection connection = null;
    Session session = null;
    Destination destination = null;
    MessageProducer producer = null;

    try {
      // Create a connection factory
      JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
      JmsConnectionFactory cf = ff.createConnectionFactory();

      // Set the properties
      cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
      cf.setIntProperty(WMQConstants.WMQ_PORT, port);
      cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
      if (clientTransport) {
          cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
      }
      else {
    	  cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_BINDINGS);
      }
      cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManagerName);
      if (user != null) {
          cf.setStringProperty(WMQConstants.USERID, user);
          cf.setStringProperty(WMQConstants.PASSWORD, password);
          cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
        }

      try{
      // Create JMS objects
      connection = cf.createConnection();
      session = connection.createSession(true, Session.SESSION_TRANSACTED);
      }catch(Exception e){
    	  System.out.println("Resources has been used out: "+e.getMessage()+"\n"+e.getStackTrace());
    	  System.exit(1);
      }
      if (isTopic) {
        destination = session.createTopic(destinationName);
      }
      else {
        destination = session.createQueue(destinationName);
      }
      producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.PERSISTENT);

      // Start the connection
      connection.start();
      String line;
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      do {
        System.out.print("Enter some text to be sent in a message <ENTER to finish>:");
        System.out.flush();
        line = in.readLine();
        if (line!=null){
          if(line.trim().length()==0){
            break;
          }
            TextMessage message = session.createTextMessage(line);
            // And, send the message
            producer.send(message);
            System.out.println("Sent message:\n" + message);
          }
        }
      while (line != null);

      recordSuccess();
      try{
      Thread.sleep(1000000);
      }catch(Exception e){}
    }
    catch (JMSException | IOException jmsex) {
      recordFailure(jmsex);
    }
    finally {
      if (producer != null) {
        try {
          producer.close();
        }
        catch (JMSException jmsex) {
          System.out.println("Producer could not be closed.");
          recordFailure(jmsex);
        }
      }

      if (session != null) {
        try {
          session.close();
        }
        catch (JMSException jmsex) {
          System.out.println("Session could not be closed.");
          recordFailure(jmsex);
        }
      }

      if (connection != null) {
        try {
          connection.close();
        }
        catch (JMSException jmsex) {
          System.out.println("Connection could not be closed.");
          recordFailure(jmsex);
        }
      }
    }
    System.exit(status);
    return;
  } // end main()

  /**
   * Process a JMSException and any associated inner exceptions.
   *
   * @param jmsex
   */
  private static void processJMSException(JMSException jmsex) {
    System.out.println(jmsex);
    Throwable innerException = jmsex.getLinkedException();
    if (innerException != null) {
      System.out.println("Inner exception(s):");
    }
    while (innerException != null) {
      System.out.println(innerException);
      innerException = innerException.getCause();
    }
    return;
  }

  /**
   * Record this run as successful.
   */
  private static void recordSuccess() {
    System.out.println("SUCCESS");
    status = 0;
    return;
  }

  /**
   * Record this run as failure.
   *
   * @param ex
   */
  private static void recordFailure(Exception ex) {
    if (ex != null) {
      if (ex instanceof JMSException) {
        processJMSException((JMSException) ex);
      }
      else {
        System.out.println(ex);
      }
    }
    System.out.println("FAILURE");
    status = -1;
    return;
  }

  /**
   * Parse user supplied arguments.
   *
   * @param args
   */
  private static void parseArgs(String[] args) {
    try {
      int length = args.length;
      if (length == 0) {
        throw new IllegalArgumentException("No arguments! Mandatory arguments must be specified.");
      }
      if ((length % 2) != 0) {
        throw new IllegalArgumentException("Incorrect number of arguments!");
      }

      int i = 0;

      while (i < length) {
        if ((args[i]).charAt(0) != '-') {
          throw new IllegalArgumentException("Expected a '-' character next: " + args[i]);
        }

        char opt = (args[i]).toLowerCase().charAt(1);

        switch (opt) {
          case 'h' :
            host = args[++i];
            if (host != null && (host.length()>0) )
            	clientTransport = true;
            break;
          case 'p' :
            port = Integer.parseInt(args[++i]);
            break;
          case 'l' :
            channel = args[++i];
            break;
          case 'm' :
            queueManagerName = args[++i];
            break;
          case 'd' :
            destinationName = args[++i];
            break;
          case 'u' :
            user = args[++i];
            break;
          case 'w' :
            password = args[++i];
            break;
          default : {
            throw new IllegalArgumentException("Unknown argument: " + opt);
          }
        }

        ++i;
      }

      if (queueManagerName == null) {
        throw new IllegalArgumentException("A queueManager name must be specified.");
      }

      if (destinationName == null) {
        throw new IllegalArgumentException("A destination name must be specified.");
      }

      if (((user == null) && (password != null)) || //
          ((user != null) && (password == null))) {
        throw new IllegalArgumentException("A userid and password must be specified together");
      }

      // Whether the destination is a queue or a topic. Apply a simple check.
      if (destinationName.startsWith("topic://")) {
        isTopic = true;
      }
      else {
        // Otherwise, let's assume it is a queue.
        isTopic = false;
      }
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      printUsage();
      System.exit(-1);
    }
    return;
  }

  /**
   * Display usage help.
   */
  private static void printUsage() {
    System.out.println("\nUsage:");
    System.out
        .println("JmsProducer -m queueManagerName -d destinationName [-h host -p port -l channel] [-u userid -w passWord]");
    return;
  }

} // end class
