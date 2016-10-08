

// SCCSID "@(#) MQMBID sn=p900-L160518.TRIAL su=_7Y4jrR0LEearh6Qyg9d9Dg pn=MQJavaSamples/jms/JmsConsumer.java"
/*
 *   <copyright 
 *   notice="lm-source-program" 
 *   pids="5724-H72,5655-R36,5655-L82,5724-L26" 
 *   years="2008,2014" 
 *   crc="39457954" > 
 *   Licensed Materials - Property of IBM  
 *    
 *   5724-H72,5655-R36,5655-L82,5724-L26 
 *    
 *   (C) Copyright IBM Corp. 2008, 2014 All Rights Reserved.  
 *    
 *   US Government Users Restricted Rights - Use, duplication or  
 *   disclosure restricted by GSA ADP Schedule Contract with  
 *   IBM Corp.  
 *   </copyright> 
 */

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * A JMS consumer (receiver or subscriber) application that receives a message from the named
 * destination (queue or topic).
 *
 * Tip: A subscriber application must be started before the publisher application.
 *
 * Notes:
 *
 * API type: IBM JMS API (v1.1, unified domain)
 *
 * Messaging domain: Point-to-point or Publish-Subscribe
 *
 * Provider type: WebSphere MQ
 *
 * Connection mode: Client connection
 *
 * JNDI in use: No
 *
 * Usage:
 *
 * JmsConsumer -m queueManagerName -d destinationName [-h host -p port -l channel] [-u user -w passWord] [-t timeoutSeconds]
 *
 * for example:
 *
 * JmsConsumer -m QM1 -d Q1
 *
 * JmsConsumer -m QM1 -d topic://foo -h localhost -p 1414 -u tester -w testpw
 */
public class JmsConsumer implements Runnable{

  private static String host = "124.160.25.254";
  private static int port = 1414;
  private static String channel = "SvrConn3";//"SYSTEM.DEF.SVRCONN";//SvrConn2
  private static String user = "adams.dong";
  private static String password = "Goodluck12#";
  private static String queueManagerName = "QMG2";
  private static String destinationName = "LocalQueue1";
  private static boolean isTopic = false;
  private static boolean clientTransport = true;

  private static int timeout = 15000; // in ms or 15 seconds

  // System exit status value (assume unset value to be 1)
  private static int status = 1;

  public static void main(String[] args) {
	  Runnable r = new JmsConsumer();
	for(int i = 0; i<100000; i++){
		new Thread(r).start();
		System.out.println("Consumer Thread "+i+" started");
		try{Thread.sleep(200);}
		catch(Exception e){
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
    MessageConsumer consumer = null;

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

      // Create JMS objects
      try{
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
      consumer = session.createConsumer(destination);

      // Start the connection
      connection.start();

      // And, receive the message
      Message message;
      do {
        message = consumer.receive(timeout);
        if (message != null) {
          System.out.println("Received message:\n" + message);
        }
      }
      while (message != null);

      System.out.format("No message received in %d seconds!\n", timeout / 1000);
      recordSuccess();
      try{
          Thread.sleep(1000000);
          }catch(Exception e){}
    }
    catch (JMSException jmsex) {
      recordFailure(jmsex);
    }
    finally {
      if (consumer != null) {
        try {
          consumer.close();
        }
        catch (JMSException jmsex) {
          System.out.println("Consumer could not be closed.");
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
          case 't' :
            try {
              int timeoutSeconds = Integer.parseInt(args[++i]);
              timeout = timeoutSeconds * 1000;
            }
            catch (NumberFormatException nfe) {
              throw new IllegalArgumentException("Timeout must be a whole number of seconds");
            }
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

      if (((user == null) && (password != null)) || 
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
        .println("JmsConsumer -m queueManagerName -d destinationName [-h host -p port -l channel] [-u user -w passWord] [-t timeout_seconds]");
    return;
  }

} // end class
