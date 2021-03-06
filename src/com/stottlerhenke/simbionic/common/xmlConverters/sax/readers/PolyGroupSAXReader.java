
package com.stottlerhenke.simbionic.common.xmlConverters.sax.readers;
 /*
 * class automatically generated using XSLT translator
 *
 */

import com.stottlerhenke.simbionic.common.xmlConverters.sax.Parser;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.StackParser;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.basicParsers.*;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.readers.*;

import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.awt.Color;
    

public class PolyGroupSAXReader extends Parser {


  /** sequence ,minOccurs=, type=Poly **/
  public static String poly = "poly";
   /** id to refer to internally refer to the poly tag **/
  public static int poly_ID = 1;
 
  protected String startTag;
  protected Hashtable startTagAttributes;
  List<com.stottlerhenke.simbionic.common.xmlConverters.model.Poly> readObjects;

    
  /** constructor **/
  public PolyGroupSAXReader (StackParser stackParserController,String tag, Hashtable tagAttributes, Parser client, int property) {
     super(stackParserController,client,property);
	 readObjects = new   ArrayList<com.stottlerhenke.simbionic.common.xmlConverters.model.Poly> ();
	 startTag = tag;
	 startTagAttributes = tagAttributes;
  }
   
  /** returns array of objects read by the parser **/
  public List<com.stottlerhenke.simbionic.common.xmlConverters.model.Poly> getValue () {
	  return readObjects;
  }
  
 /** given the start of a tag create a parser to transform the content of the tag into a DM object**/
  public void startElement(String tag, Hashtable tagAttributes) throws Exception { 
    stackParser.addParser(new PolySAXReader(stackParser,tag,tagAttributes,this,0)); 
  }
  
  public void endElement(String tag) throws Exception {
	  if (startTag.equals(tag)) {
			isDone = true;
	  }
	 else {
		//error
	 }
  }
  
  /** collect the result. the property argument is disregarded **/
  protected  void receiveParsingResult(int property, Object result) {
   try{
  	if (result == null) return;
  	readObjects.add((com.stottlerhenke.simbionic.common.xmlConverters.model.Poly)result);
   }
    catch(Exception e){
    	e.printStackTrace();
    }
  }


 } 
 
