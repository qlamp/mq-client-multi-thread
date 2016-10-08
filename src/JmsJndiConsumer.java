


// SCCSID "@(#) MQMBID sn=p900-L160518.TRIAL su=_7Y4jrR0LEearh6Qyg9d9Dg pn=MQJavaSamples/jms/JmsJndiConsumer.java"
/*
 *   <copyright 
 *   notice="lm-source-program" 
 *   pids="5724-H72,5655-R36,5655-L82,5724-L26" 
 *   years="2008,2016" 
 *   crc="3278153789" > 
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

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsDestination;

/**
 * A JMS consumer (receiver or subscriber) application that receives a message from the named
 * destination (queue or topic) by looking up the connection factory instance and the destination
 * instance in an initial context (This sample supports file system context only).
 * 
 * Tip: A subscriber application must be started before the publisher application.
 * 
 * Notes:
 * 
 * API type: IBM JMS API (v1.1, unified domain)
 * 
 * Messaging domain: Point-to-point or Publish-Subscribe
 * 
 * Provider type: IBM MQ
 * 
 * Connection mode: Client connection or bindings connection
 * 
 * JNDI in use: Yes
 * 
 * Usage:
 * 
 * JmsJndiConsumer -i initialContext -c connectionFactory -d destination
 * 
 * for example:
 * 
 * JmsJndiConsumer -i file:/C:/JNDI-Directory -c myQCF -d myQueue
 */
public class JmsJndiConsumer {

  private static String initialContextUrl = null;
  private static String connectionFactoryFromJndi = null;
  private static String destinationFromJndi = null;

  private static int timeout = 15000; // in ms or 15 seconds

  // System exit status value (assume unset value to be 1)
  private static int status = 1;

  /**
   * Main method
   * 
   * @param args
   */
  public static void main(String[] args) {
    // Parse the arguments
    parseArgs(args);

    // Variables
    Connection connection = null;
    Session session = null;
    Destination destination = null;
    MessageConsumer consumer = null;

    try {
      // Instantiate the initial context
      String contextFactory = "com.sun.jndi.fscontext.RefFSContextFactory";
      Hashtable<String, String> environment = new Hashtable<String, String>();
      environment.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
      environment.put(Context.PROVIDER_URL, initialContextUrl);
      Context context = new InitialDirContext(environment);
      System.out.println("Initial context found!");

      // Lookup the connection factory
      JmsConnectionFactory cf = (JmsConnectionFactory) context.lookup(connectionFactoryFromJndi);

      // Lookup the destination
      destination = (JmsDestination) context.lookup(destinationFromJndi);

      // Create JMS objects
      connection = cf.createConnection();
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      consumer = session.createConsumer(destination);

      // Start the connection
      connection.start();

      // And, receive the message
      Message message = consumer.receive(timeout);
      if (message != null) {
        System.out.println("Received message:\n" + message);
      } else {
        System.out.println("No message received!\n");
        recordFailure(null);
      }

      recordSuccess();
    }
    catch (JMSException jmsex) {
      recordFailure(jmsex);
    }
    catch (NamingException ne) {
      System.out.println("The initial context could not be instantiated, or the lookup failed.");
      recordFailure(ne);
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
          case 'i' :
            initialContextUrl = args[++i];
            break;
          case 'c' :
            connectionFactoryFromJndi = args[++i];
            break;
          case 'd' :
            destinationFromJndi = args[++i];
            break;
          default : {
            throw new IllegalArgumentException("Unknown argument: " + opt);
          }
        }

        ++i;
      }

      if (initialContextUrl == null) {
        throw new IllegalArgumentException("An initial context must be specified.");
      }

      if (connectionFactoryFromJndi == null) {
        throw new IllegalArgumentException(
            "A connection factory to lookup in the initial context must be specified.");
      }

      if (destinationFromJndi == null) {
        throw new IllegalArgumentException(
            "A destination to lookup in the initial context must be specified.");
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
    System.out.println("JmsJndiConsumer -i initialContext -c connectionFactory -d destination");
    return;
  }

} // end class
