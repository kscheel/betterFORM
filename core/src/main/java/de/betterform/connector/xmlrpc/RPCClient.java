/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.connector.xmlrpc;

import de.betterform.xml.xforms.exception.XFormsException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.w3c.dom.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

public class RPCClient {
    private String URL = null;
    private static final String DEFAULT_URL = "http://localhost:8088/";

    private XmlRpcClient xmlrpc = null;

    public RPCClient() throws MalformedURLException {
        this.URL = DEFAULT_URL;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(URL));
        xmlrpc = new XmlRpcClient();
        xmlrpc.setConfig(config);
    };
    
    public RPCClient(String URL) throws MalformedURLException {
        this.URL = URL;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(URL));
        xmlrpc = new XmlRpcClient();
        xmlrpc.setConfig(config);
    }

    public Document getDocument(String function, Hashtable params) throws XFormsException {

        Hashtable ret = runFunc(function, params);

        if (!ret.containsKey("status")) {
            throw new XFormsException("XML-RPC return hash has no status");
        }

        String status = (String) ret.get("status");

        if (status.equals("error")) {

            if (!ret.containsKey("error")) {
                throw new XFormsException("Unknown error: cannot find XML-RPC error code");
            }

            String s = (String) ret.get("error");
            throw new XFormsException(s);
        }


        byte[] docbytes = (byte[]) ret.get("doc");

        Document doc = null;
        try {
            DocTransformer dt = new DocTransformer(docbytes);
            doc = dt.getDoc();
        } catch (Exception e) {
            throw new XFormsException(e);
        }

        return doc;
    }

    public Hashtable runFunc(String function, Hashtable params) {
        Vector v = new Vector();
        v.addElement(params);

        return runFunc(function, v);
    }

    public Hashtable runFunc(String function, Vector params) {
        Hashtable h = null;
        Object o = null;
        try {
            System.out.println("RPCClient: running function: " + function);
            o = xmlrpc.execute(function, params);
        } catch (XmlRpcException e) {
            h = new Hashtable();
            h.put("status", "error");
            h.put("error", "Cannot execute XML-RPC query: (" + e.getCause() + ":" + e.toString() + ")");
            return h;
        } /*catch (IOException e) {
            h = new Hashtable();
            h.put("status", "error");
            h.put("error", "IO Exception executing XML-RPC query: " + e.toString());
            return h;
        }*/
        if (o == null) {
            h = new Hashtable();
            h.put("status", "error");
            h.put("error", "Object is NULL");
            return h;
        }

        if (!o.getClass().isInstance(new Hashtable())) {
            h = new Hashtable();
            h.put("status", "error");
            h.put("error", "Returned object is not a Hashtable - it is a " + o.getClass().getName());
            return h;
        }
        h = (Hashtable) o;

        if (!h.containsKey("status")) {
            h.put("status", "error");
            h.put("error", "No status in result hash");
            return h;
        }

        String status = (String) h.get("status");
        if (status.equals("error") && !h.containsKey("error")) {
            h.put("error", "Unknown error - no error key found in hash");
        }

        return h;
    }


}

