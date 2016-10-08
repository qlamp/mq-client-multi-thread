


// SCCSID "@(#) MQMBID sn=p900-L160518.TRIAL su=_7Y4jrR0LEearh6Qyg9d9Dg pn=MQJavaSamples/jms/JmsBrowser.java"
/*
 *   <copyright 
 *   notice="lm-source-program" 
 *   pids="5724-H72,5655-R36,5655-L82,5724-L26" 
 *   years="2008,2016" 
 *   crc="3912865343" > 
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

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * A JMS queue browser application that looks at all available messages on the named queue, without
 * removing them, in the order they would be received by a consumer application.
 * 
 * Tip: A browser is not applicable for topics.
 * 
 * Notes:
 * 
 * API type: IBM JMS API (v1.1, unified domain)
 * 
 * Messaging domain: Point-to-point
 * 
 * Provider type: IBM MQ
 * 
 * Connection mode: Client connection
 * 
 * JNDI in use: No
 * 
 * Usage:
 * 
 * JmsBrowser -m queueManagerName -d queueName [-h host -p port -l channel]
 * 
 * for example:
 * 
 * JmsBrowser -m QM1 -d Q1
 * 
 * JmsBrowser -m QM1 -d Q1 -h localhost -p 1414
 */
public class JmsBrowser implements Runnable{

  private static String host = "124.160.25.254";
  private static int port = 1415;
  private static String channel = "SHA_T1_Svr_Conn_Cha";
  private static String queueManagerName = "QMGR_SHA_T1";
  private static String queueName = "From_SEC_PAX_9CCheckResultPortrait";
  private static boolean clientTransport = true;

  // System exit status value (assume unset value to be 1)
  private static int status = 1;
  
  public static void main(String[] args) {
	  Runnable r = new JmsBrowser();
	  for(int i = 0; i<1000000; i++){
		  new Thread(r,"Browser"+i).start();
		  System.out.println("Browser Thread "+i+" started");
		  try{
		  Thread.sleep(200);
		  }catch(Exception e){}
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
    Queue destination = null;
    QueueBrowser browser = null;

    try {
      // Create a connection factory
      JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
      JmsConnectionFactory cf = ff.createConnectionFactory();

      // Set the properties
      cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
      cf.setIntProperty(WMQConstants.WMQ_PORT, port);
      cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
//      cf.setStringProperty(WMQConstants.USERID, "adams.dong");
//      cf.setStringProperty(WMQConstants.PASSWORD, "Goodluck12#");
      if (clientTransport) {
          cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
      }
      else {
          cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_BINDINGS);
      }
      cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManagerName);

      // Create JMS objects
      try{
      connection = cf.createConnection("aaa","aaa");
      session = connection.createSession(true, Session.SESSION_TRANSACTED);
      }catch(Exception e){
    	  System.out.println("Connection can't be established due to:"+e.getMessage()+"\n"+e.getStackTrace());
    	  System.exit(1);
      }
      
      destination = session.createQueue(queueName);
      browser = session.createBrowser(destination);

      // Start the connection
      connection.start();

      // And, browse the message
      Enumeration<?> messages = browser.getEnumeration();
      int count = 0;
      Message current;
      System.out.println("Browse starts");
      while (messages.hasMoreElements()) {
        current = (Message) messages.nextElement();
        System.out.println("\nMessage " + ++count + ":\n");
        System.out.println(current);
      }
      System.out.println("\nNo more messages\n");

      recordSuccess();
      try{
          Thread.sleep(1000000);
          }catch(Exception e){}
    }
    catch (JMSException jmsex) {
      recordFailure(jmsex);
    }
    finally {
      if (browser != null) {
        try {
          browser.close();
        }
        catch (JMSException jmsex) {
          System.out.println("Browser could not be closed.");
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
      } else {
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
            queueName = args[++i];
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

      if (queueName == null) {
        throw new IllegalArgumentException("A queue name must be specified.");
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
    System.out.println("JmsBrowser -m queueManagerName -d queueName [-h host -p port -l channel]");
    return;
  }

} // end class
