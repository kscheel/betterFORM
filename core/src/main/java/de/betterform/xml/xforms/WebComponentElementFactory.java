/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.xml.xforms;

import de.betterform.xml.config.Config;
import de.betterform.xml.dom.DOMUtil;
import de.betterform.xml.ns.NamespaceConstants;
import de.betterform.xml.ns.NamespaceResolver;
import de.betterform.xml.xforms.action.*;
import de.betterform.xml.xforms.exception.XFormsException;
import de.betterform.xml.xforms.model.Instance;
import de.betterform.xml.xforms.model.Model;
import de.betterform.xml.xforms.model.bind.Bind;
import de.betterform.xml.xforms.model.submission.Header;
import de.betterform.xml.xforms.model.submission.Submission;
import de.betterform.xml.xforms.ui.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;

/**
 * XFormsElementFactory creates objects for all DOM Nodes in the input Document
 * that are in the XForms namespace or are bound to some instance node by XForms
 * binding attributes.
 * <p/>
 * These objects holds the XForms semantics and are attached to their original
 * DOM equivalent via the Xerces-specific getUserData/setUserData methods in
 * ElementNSImpl.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: XFormsElementFactory.java,v 1.22 2005/02/01 00:26:58 joernt Exp
 */
public class WebComponentElementFactory implements XFormsConstants, WebComponentConstants {



    /**
     * Creates a new XFormsElementFactory object.
     */
    public WebComponentElementFactory() {
    }

    /**
     * returns true, if the given DOM Element is a XForms action Element.
     *
     * @param element the DOM Element to investigate
     * @return true, if the given DOM Element is a XForms action Element
     */
    public static boolean isActionElement(Element element) {
        return false; //todo
    }

    /**
     * returns true, if the given DOM Element is a XForms UI Element.
     *
     * @param element the DOM Element to investigate
     * @return true, if the given DOM Element is a XForms UI Element
     */
    public boolean isUIElement(Element element) {
        if (this.hasRepeatAttributes(element)) {
            return true;
        }

        if(element.hasAttribute("is") && element.getAttribute("is").startsWith("xf-")) return true;
        if(element.hasAttribute("bind")) return true;
        if(element.hasAttribute("ref")) return true;

        return false;
    }

    /**
     * factory method for XFormsElement objects.
     *
     * @param element - the DOM Element which will be annotated
     * @param model the owning model
     * @return the created object
     */
    public XFormsElement createXFormsElement(Element element, Model model) throws XFormsException {
        String localName = element.getLocalName();
        String component = element.getAttribute("is");

        XFormsElement xformsElement;

        // 3.3 The XForms Core Module
        if (localName.equals(MODEL)) {
            xformsElement = new Model(element);
        }
        else if (localName.equals(INSTANCE)) {
            xformsElement = new Instance(element, model);
        }
/*
        else if (localName.equals(BIND)) {
            xformsElement = new Bind(element, model);
        }
*/
        else if (localName.equals(SUBMISSION)) {
            xformsElement = new Submission(element, model);
        }
        else if (localName.equals(HEADER)) {
            xformsElement = new Header(element, model);
        }
        // 3.5 The XForms Extension module
        else if (localName.equals(EXTENSION)) {
            xformsElement = new Extension(element, model);
        }
        // 8.1 The XForms Form Controls Module
        else if (component.equals(INPUT_COMPONENT) ||
                component.equals(SECRET_COMPONENT) ||
                component.equals(TEXTAREA_COMPONENT)) {
            xformsElement = new Text(element, model);
        }
        else if (localName.equals(OUTPUT)) {
            xformsElement = new Output(element, model);
        }
        else if (localName.equals(UPLOAD)) {
            WebComponentElementFactory.generateDefaultAlert(element);
            xformsElement = new Upload(element, model);
        }
        else if (localName.equals(RANGE)) {
            WebComponentElementFactory.generateDefaultAlert(element);
            xformsElement = new Range(element, model);
        }
        else if (localName.equals(TRIGGER)) {
            WebComponentElementFactory.generateDefaultAlert(element);
            xformsElement = new Trigger(element, model);
        }
        else if (localName.equals(SUBMIT)) {
            WebComponentElementFactory.generateDefaultAlert(element);
            xformsElement = new Submit(element, model);
        }
        else if (localName.equals(SELECT)) {
            WebComponentElementFactory.generateDefaultAlert(element);
            Selector selector = new Selector(element, model);
            selector.setMultiple(true);
            xformsElement = selector;
        }
        else if (localName.equals(SELECT1)) {
            WebComponentElementFactory.generateDefaultAlert(element);
            Selector selector = new Selector(element, model);
            selector.setMultiple(false);
            xformsElement = selector;
        }
        // 8.2 Common Markup for Selection Controls
        else if (localName.equals(CHOICES)) {
            xformsElement = new Choices(element, model);
        }
        else if (localName.equals(ITEM)) {
            WebComponentElementFactory.generateDefaultAlert(element);
            xformsElement = new Item(element, model);
        }
        else if (localName.equals(VALUE)) {
            xformsElement = new Value(element, model);
        }
        // 8.3 Additional Elements
        else if (localName.equals(FILENAME)) {
            xformsElement = new Filename(element, model);
        }
        else if (localName.equals(MEDIATYPE)) {
            xformsElement = new Mediatype(element, model);
        }
        else if (localName.equals(LABEL) || localName.equals(HELP) || localName.equals(HINT) || localName.equals(ALERT)) {
            xformsElement = new Common(element, model);
        }
        // 9.1 The XForms Group Module
        else if (localName.equals(GROUP)) {
            //we've hit a Repeat Item
            if (element.getAttributeNS(null, APPEARANCE_ATTRIBUTE).equals("repeated")) {
                xformsElement = new RepeatItem(element, model);
            }
            else {
                //in case there are repeat attributes we handle the Group just like a normal Repeat
                if(hasRepeatAttributes(element)){
                    xformsElement = new Repeat(element,model);
                }else{
                    WebComponentElementFactory.generateDefaultAlert(element);
                    xformsElement = new Group(element, model);
                }
            }
        }
        // 9.2 The XForms Switch Module (w/o actions)
        else if (localName.equals(SWITCH)) {
            xformsElement = new Switch(element, model);
        }
        else if (localName.equals(CASE)) {
            xformsElement = new Case(element, model);
        }
        // 9.3 The XForms Repeat Module (w/o actions)
        else if (localName.equals(REPEAT) || hasRepeatAttributes(element)) {
            xformsElement = new Repeat(element, model);
        }
        else if (localName.equals(ITEMSET)) {
            WebComponentElementFactory.generateDefaultAlert(element);
            xformsElement = new Itemset(element, model);
        }
        else if (localName.equals(COPY)) {
            xformsElement = new Copy(element, model);
        }
        // 10.1 The XForms Action Module
        else if (localName.equals(ACTION)) {
            xformsElement = new ActionAction(element, model);
        }
        else if (localName.equals(DISPATCH)) {
            xformsElement = new DispatchAction(element, model);
        }
        else if (localName.equals(REBUILD)) {
            xformsElement = new RebuildAction(element, model);
        }
        else if (localName.equals(RECALCULATE)) {
            xformsElement = new RecalculateAction(element, model);
        }
        else if (localName.equals(REVALIDATE)) {
            xformsElement = new RevalidateAction(element, model);
        }
        else if (localName.equals(REFRESH)) {
            xformsElement = new RefreshAction(element, model);
        }
        else if (localName.equals(SETFOCUS)) {
            xformsElement = new SetFocusAction(element, model);
        }
        else if (localName.equals(LOAD)) {
            xformsElement = new LoadAction(element, model);
        }
        else if (localName.equals(UNLOAD)) {
            xformsElement = new UnloadAction(element, model);
        }
        else if (localName.equals(SETVALUE)) {
            xformsElement = new SetValueAction(element, model);
        }
        else if (localName.equals(SEND)) {
            xformsElement = new SendAction(element, model);
        }
        else if (localName.equals(RESET)) {
            xformsElement = new ResetAction(element, model);
        }
        else if (localName.equals(MESSAGE)) {
            xformsElement = new MessageAction(element, model);
        }
        else if (localName.equals(TOGGLE)) {
            xformsElement = new ToggleAction(element, model);
        }
        else if (localName.equals(INSERT)) {
            xformsElement = new InsertAction(element, model);
        }
        else if (localName.equals(DELETE)) {
            xformsElement = new DeleteAction(element, model);
        }
        else if (localName.equals(SETINDEX)) {
            xformsElement = new SetIndexAction(element, model);
        }else if(localName.equals("script")){
            xformsElement = new ScriptAction(element,model);
        }else if(localName.equals(SETVARIABLE)){
            xformsElement = new SetVariable(element,model);
        }
        else {
            // todo: try to find custom action
            // todo: throw exception
            return null;
        }

        // attach XFormsElement to DOM Element by using Xerces specific method
        element.setUserData("",xformsElement,null);

        return xformsElement;
    }

    /**
     * Checks wether the given element has XForms Repeat binding attributes.
     *
     * @param element the element to check.
     * @return <code>true</code> if the given element has XForms Repeat binding
     * attributes, otherwise <code>false</code>.
     */
    public static boolean hasRepeatAttributes(Element element) {
        return element.hasAttribute(REPEAT_MODEL_ATTRIBUTE) ||
                element.hasAttribute(REPEAT_BIND_ATTRIBUTE) ||
                element.hasAttribute(REPEAT_REF_ATTRIBUTE) ||
                element.hasAttribute(REPEAT_NODESET_ATTRIBUTE);
    }

    /**
     * Generates a default XForms Alert element if applicable. The element will
     * be generated if the corresponding configuration property is set to 'true'
     * and the specified element has no XForms Alert child.
     *
     * @param element the element to check.
     * @return
     * @return <code>true</code> if a default XForms Alert element has been
     * generated, otherwise <code>false</code>.
     */
    public static boolean generateDefaultAlert(Element element) throws XFormsException {
        if (Boolean.valueOf(Config.getInstance().getProperty("betterform.ui.generateDefaultAlerts", "false")).booleanValue() &&
            DOMUtil.findFirstChildNS(element, NamespaceConstants.XFORMS_NS, XFormsConstants.ALERT) == null) {

            String alertText = Config.getInstance().getProperty("betterform.ui.defaultAlertText", "The specified value is invalid");
            Document document = element.getOwnerDocument();
            String xformsPrefix = NamespaceResolver.getPrefix(element, NamespaceConstants.XFORMS_NS);
            Element alertElement = document.createElementNS(NamespaceConstants.XFORMS_NS, (xformsPrefix!=null?xformsPrefix:NamespaceConstants.XFORMS_PREFIX) + ":" + XFormsConstants.ALERT);
            alertElement.appendChild(document.createTextNode(alertText));
            element.appendChild(alertElement);

            return true;
        }
        return false;
    }

}

// end of class
