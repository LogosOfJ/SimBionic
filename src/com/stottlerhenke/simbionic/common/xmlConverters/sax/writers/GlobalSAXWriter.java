

/*
 * Class automatically generated using XSLT translator
 * See Taskguide/xslt/Readme.doc describing how to run tthe XSLT translator and
 * an explanation of the generated code.
 *
 
 <pre> 
   &lt;xsd:all /> 
   &lt;xsd:element name="name" type="xsd:string" /> 
   &lt;xsd:element name="type" type="xsd:string" /> 
   &lt;xsd:element name="initial" type="xsd:string" /> 
   &lt;xsd:element name="polymorphic" type="xsd:boolean" /> 
  &lt;/xsd:all> 
   </pre>
*/


package com.stottlerhenke.simbionic.common.xmlConverters.sax.writers;
import com.stottlerhenke.simbionic.common.xmlConverters.model.*;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.Parser;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.StackParser;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.readers.*;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.writers.Utils;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
    

public class GlobalSAXWriter  {
 
  
  /** 
   * write the given taskguide object to the given xml file
   * 
   * @param dmObject -- object to be writen
   * @param writer -- xml output file
   * @param indent -- indent used to generate the xml tags associated with the input object
  **/
  
  public static void write (com.stottlerhenke.simbionic.common.xmlConverters.model.Global dmObject, PrintWriter writer, int indent) {
   
     Utils.writeField(GlobalSAXReader.name,dmObject.getName(),writer,indent+1);
     
     Utils.writeField(GlobalSAXReader.type,dmObject.getType(),writer,indent+1);
     
     Utils.writeField(GlobalSAXReader.initial,dmObject.getInitial(),writer,indent+1);
     
     Utils.writeField(GlobalSAXReader.polymorphic,dmObject.isPolymorphic(),writer,indent+1);
     
  }




 } 
 
