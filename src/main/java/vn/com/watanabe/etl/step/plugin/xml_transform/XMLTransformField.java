/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package vn.com.watanabe.etl.step.plugin.xml_transform;

import java.util.Arrays;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

import vn.com.watanabe.etl.step.plugin.xml_transform.BaseSelectList.Item;



/**
 * Describes an XML field and the position in an XML field.
 * 
 * @author Samatar, Brahim
 * @since 20-06-2007
 */
public class XMLTransformField implements Cloneable {
  private static Class<?> PKG = XMLTranformData.class; // for i18n purposes, needed by Translator2!!
  public static String PREFIX_TAG="XMLTransform";


  public static final Item ELEMENT_TYPE_NODE =new BaseSelectList.Item(
    0,
    "node", 
    "node",
    BaseMessages.getString( PKG, PREFIX_TAG+".ElementType.Node" )
  );

  public static final Item ELEMENT_TYPE_ATTRIBUT =new BaseSelectList.Item(
    1,
    "attribute",
    "attribut",
    BaseMessages.getString( PKG, PREFIX_TAG+".ElementType.Attribute" ) 
  );

  public static final Item ELEMENT_TYPE_NODE_MULTI =new BaseSelectList.Item(
    2,
    "node_multi",
    "node_multi",
    "Node (Multi)"
  );


  public static final BaseSelectList elementTypeCode = new BaseSelectList(new BaseSelectList.Item[]{
    ELEMENT_TYPE_NODE,
    ELEMENT_TYPE_ATTRIBUT,
    ELEMENT_TYPE_NODE_MULTI
  });

  public static final int RESULT_TYPE_VALUE_OF = 0;
  public static final int RESULT_TYPE_TYPE_SINGLE_NODE = 1;

  

  public static final String[] ResultTypeCode = { "valueof", "singlenode" };

  public static final String[] ResultTypeDesc = { BaseMessages.getString( PKG, PREFIX_TAG+".ResultType.ValueOf" ),
    BaseMessages.getString( PKG, PREFIX_TAG+".ResultType.SingleNode" ) };

  public static final int TYPE_TRIM_NONE = 0;
  public static final int TYPE_TRIM_LEFT = 1;
  public static final int TYPE_TRIM_RIGHT = 2;
  public static final int TYPE_TRIM_BOTH = 3;

  public static final String[] trimTypeCode = { "none", "left", "right", "both" };

  public static final String[] trimTypeDesc = { BaseMessages.getString( PKG, PREFIX_TAG+".TrimType.None" ),
    BaseMessages.getString( PKG, PREFIX_TAG+".TrimType.Left" ),
    BaseMessages.getString( PKG, PREFIX_TAG+".TrimType.Right" ),
    BaseMessages.getString( PKG, PREFIX_TAG+".TrimType.Both" ) };

  // //////////////////////////////////////////////////////////////
  //
  // Conversion to be done to go from "attribute" to "attribute"
  // - The output is written as "attribut" but both "attribut" and
  // "attribute" are accepted as input.
  // - When v3.1 is being deprecated all supported versions will
  // support "attribut" and "attribute". Then output "attribute"
  // as all version support it.
  // - In a distant future remove "attribut" all together in v5 or so.
  //
  // TODO Sven Boden
  //
  // //////////////////////////////////////////////////////////////
  // public static final String[] ElementTypeCode = { "node", "attribute" };

  
  private String name;
  private String xpath;
  private String resolvedXpath;

  private int type;
  private int length;
  private String format;
  private int trimtype;
  private int elementtype;
  private int resulttype;
  private int precision;
  private String currencySymbol;
  private String decimalSymbol;
  private String groupSymbol;
  private boolean repeat;

  public XMLTransformField( final String fieldname ) {
    this.name = fieldname;
    this.xpath = "";
    this.length = -1;
    this.type = ValueMetaInterface.TYPE_STRING;
    this.format = "";
    this.trimtype = TYPE_TRIM_NONE;
    this.elementtype = ELEMENT_TYPE_NODE.getId();
    this.resulttype = RESULT_TYPE_VALUE_OF;
    this.groupSymbol = "";
    this.decimalSymbol = "";
    this.currencySymbol = "";
    this.precision = -1;
    this.repeat = false;
  }

  public XMLTransformField() {
    this( "" );
  }

  public String getXML() {
    final StringBuffer retval = new StringBuffer( 400 );

    retval.append( "      <field>" ).append( Const.CR );
    retval.append( "        " ).append( XMLHandler.addTagValue( "name", getName() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "xpath", getXPath() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "element_type", getElementTypeCode() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "result_type", getResultTypeCode() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "type", getTypeDesc() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "format", getFormat() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "currency", getCurrencySymbol() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", getDecimalSymbol() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "group", getGroupSymbol() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "length", getLength() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "precision", getPrecision() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "trim_type", getTrimTypeCode() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "repeat", isRepeated() ) );

    retval.append( "      </field>" ).append( Const.CR );

    return retval.toString();
  }

  public XMLTransformField( final Node fnode ) throws KettleValueException {
    setName( XMLHandler.getTagValue( fnode, "name" ) );
    setXPath( XMLHandler.getTagValue( fnode, "xpath" ) );
    setElementType( getElementTypeByCode( XMLHandler.getTagValue( fnode, "element_type" ) ) );
    setResultType( getResultTypeByCode( XMLHandler.getTagValue( fnode, "result_type" ) ) );
    setType( ValueMeta.getType( XMLHandler.getTagValue( fnode, "type" ) ) );
    setFormat( XMLHandler.getTagValue( fnode, "format" ) );
    setCurrencySymbol( XMLHandler.getTagValue( fnode, "currency" ) );
    setDecimalSymbol( XMLHandler.getTagValue( fnode, "decimal" ) );
    setGroupSymbol( XMLHandler.getTagValue( fnode, "group" ) );
    setLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
    setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
    setTrimType( getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );
    setRepeated( !"N".equalsIgnoreCase( XMLHandler.getTagValue( fnode, "repeat" ) ) );
  }

  public static final int getTrimTypeByCode( final String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < trimTypeCode.length; i++ ) {
      if ( trimTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final int getElementTypeByCode( final String tt ) {
    Item typeCode = elementTypeCode.getByName(tt);
    if(typeCode!=null){
      return typeCode.getId();
    }
    return 0;
  }

  public static final int getTrimTypeByDesc( final String tt ) {
    Item typeCode = elementTypeCode.getByDesc(tt);
    if(typeCode!=null){
      return typeCode.getId();
    }
    return 0;
  }

  public static final int getElementTypeByDesc( final String tt ) {
    Item typeCode = elementTypeCode.getByDesc(tt);
    if(typeCode!=null){
      return typeCode.getId();
    }
    return 0;
  }

  public static final String getTrimTypeCode( final int i ) {
    if ( i < 0 || i >= trimTypeCode.length ) {
      return trimTypeCode[0];
    }
    return trimTypeCode[i];
  }

  public static final String getElementTypeCode( final int i ) {
    Item value = elementTypeCode.getById(i);
    if(value!=null){
      return value.toString();
    }else{
      return "";
    }
    
  }

  public static final String getTrimTypeDesc( final int i ) {
    if ( i < 0 || i >= trimTypeDesc.length ) {
      return trimTypeDesc[0];
    }
    return trimTypeDesc[i];
  }

  public static final String getElementTypeDesc( final int i ) {
    Item value = elementTypeCode.getById(i);
    if(value!=null){
      return value.getDesc();
    }else{
      return "";
    }
  }

  public Object clone() {
    try {
      final XMLTransformField retval = (XMLTransformField) super.clone();

      return retval;
    } catch ( final CloneNotSupportedException e ) {
      return null;
    }
  }

  public int getLength() {
    return length;
  }

  public void setLength( final int length ) {
    this.length = length;
  }

  public String getName() {
    return name;
  }

  public String getXPath() {
    return xpath;
  }

  protected String getResolvedXPath() {
    return resolvedXpath;
  }

  public void setXPath( final String fieldxpath ) {
    this.xpath = fieldxpath;
  }

  protected void setResolvedXPath( final String resolvedXpath ) {
    this.resolvedXpath = resolvedXpath;
  }

  public void setName( final String fieldname ) {
    this.name = fieldname;
  }

  public int getType() {
    return type;
  }

  public String getTypeDesc() {
    return ValueMeta.getTypeDesc( type );
  }

  public void setType( final int type ) {
    this.type = type;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( final String format ) {
    this.format = format;
  }

  public int getTrimType() {
    return trimtype;
  }

  public int getElementType() {
    return elementtype;
  }

  public String getTrimTypeCode() {
    return getTrimTypeCode( trimtype );
  }

  public String getElementTypeCode() {
    return getElementTypeCode( elementtype );
  }

  public String getTrimTypeDesc() {
    return getTrimTypeDesc( trimtype );
  }

  public String getElementTypeDesc() {
    return getElementTypeDesc( elementtype );
  }

  public void setTrimType( final int trimtype ) {
    this.trimtype = trimtype;
  }

  public void setElementType( final int element_type ) {
    this.elementtype = element_type;
  }

  public String getGroupSymbol() {
    return groupSymbol;
  }

  public void setGroupSymbol( final String group_symbol ) {
    this.groupSymbol = group_symbol;
  }

  public String getDecimalSymbol() {
    return decimalSymbol;
  }

  public void setDecimalSymbol( final String decimal_symbol ) {
    this.decimalSymbol = decimal_symbol;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol( final String currency_symbol ) {
    this.currencySymbol = currency_symbol;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision( final int precision ) {
    this.precision = precision;
  }

  public boolean isRepeated() {
    return repeat;
  }

  public void setRepeated( final boolean repeat ) {
    this.repeat = repeat;
  }

  public void flipRepeated() {
    repeat = !repeat;
  }

  public static final int getResultTypeByDesc( final String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ResultTypeDesc.length; i++ ) {
      if ( ResultTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public String getResultTypeDesc() {
    return getResultTypeDesc( resulttype );
  }

  public static final String getResultTypeDesc( final int i ) {
    if ( i < 0 || i >= ResultTypeDesc.length ) {
      return ResultTypeDesc[0];
    }
    return ResultTypeDesc[i];
  }

  public int getResultType() {
    return resulttype;
  }

  public void setResultType( final int resulttype ) {
    this.resulttype = resulttype;
  }

  public static final int getResultTypeByCode( final String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ResultTypeCode.length; i++ ) {
      if ( ResultTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    return 0;
  }

  public static final String getResultTypeCode( final int i ) {
    if ( i < 0 || i >= ResultTypeCode.length ) {
      return ResultTypeCode[0];
    }
    return ResultTypeCode[i];
  }

  public String getResultTypeCode() {
    return getResultTypeCode( resulttype );
  }
}
