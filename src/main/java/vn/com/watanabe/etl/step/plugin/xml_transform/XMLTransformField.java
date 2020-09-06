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
  public static String DEFAULT_PREFIX="XMLTransform";

  // ============= element types ==============
  public static final Item ELEMENT_TYPE_NODE =new BaseSelectList.Item(
    0,
    "node", 
    "node",
    BaseMessages.getString( PKG, DEFAULT_PREFIX+".ElementType.Node" )
  );

  public static final Item ELEMENT_TYPE_ATTRIBUT =new BaseSelectList.Item(
    1,
    "attribute",
    "attribut",
    BaseMessages.getString( PKG, DEFAULT_PREFIX+".ElementType.Attribute" ) 
  );

  public static final Item ELEMENT_TYPE_NODE_MULTI =new BaseSelectList.Item(
    2,
    "node_multi",
    "node_multi",
    "Node (Multi)"
  );


  public static final BaseSelectList ElementTypeCode = new BaseSelectList(new BaseSelectList.Item[]{
    ELEMENT_TYPE_NODE,
    ELEMENT_TYPE_ATTRIBUT,
    ELEMENT_TYPE_NODE_MULTI
  });


  // ======================== result type
  public static final Item RESULT_TYPE_VALUE_OF =new BaseSelectList.Item(
    0,
    "valueof",
    "valueof",
    BaseMessages.getString( PKG, DEFAULT_PREFIX+".ResultType.ValueOf" )
  );

  public static final Item RESULT_TYPE_TYPE_SINGLE_NODE =new BaseSelectList.Item(
    1,
    "singlenode",
    "singlenode",
    BaseMessages.getString( PKG, DEFAULT_PREFIX+".ResultType.SingleNode" )
  );

  public static final Item RESULT_TYPE_TYPE_SUM =new BaseSelectList.Item(
    2,
    "sum",
    "sum",
    "Sum"
  );
  public static final Item RESULT_TYPE_FIST_VALUE =new BaseSelectList.Item(
    3,
    "fist_value",
    "fist_value",
    "First Value"
  );
  public static final Item RESULT_TYPE_VALUE_OF_FIXED_SIZE =new BaseSelectList.Item(
    4,
    "value_of_fixed_size",
    "value_of_fixed_size",
    "Value of (Fixed Size)"
  );
  

  public static final BaseSelectList ResultTypeCode = new BaseSelectList(new Item[]{
    RESULT_TYPE_VALUE_OF,
    RESULT_TYPE_TYPE_SINGLE_NODE,
    RESULT_TYPE_TYPE_SUM,
    RESULT_TYPE_FIST_VALUE,
    RESULT_TYPE_VALUE_OF_FIXED_SIZE
  });


  // ========== type trim 

  public static final Item TYPE_TRIM_NONE = new BaseSelectList.Item(
    0,
    "none",
    "none",
    BaseMessages.getString( PKG, DEFAULT_PREFIX+".TrimType.None" )
  );
  public static final Item TYPE_TRIM_LEFT = new BaseSelectList.Item(
    1,
    "left",
    "left",
    BaseMessages.getString( PKG, DEFAULT_PREFIX+".TrimType.Left" )
  );
  public static final Item TYPE_TRIM_RIGHT = new BaseSelectList.Item(
    2,
    "right",
    "right",
    BaseMessages.getString( PKG, DEFAULT_PREFIX+".TrimType.Right" )
  );
  public static final Item TYPE_TRIM_BOTH = new BaseSelectList.Item(
    3,
    "both",
    "both",
    BaseMessages.getString( PKG, DEFAULT_PREFIX+".TrimType.Both" )
  );

  public static final BaseSelectList trimTypeCode = new BaseSelectList(new Item[]{
    TYPE_TRIM_NONE,
    TYPE_TRIM_LEFT,
    TYPE_TRIM_RIGHT,
    TYPE_TRIM_BOTH
  });

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
  private String demlimiter;

  public XMLTransformField( final String fieldname ) {
    this.name = fieldname;
    this.xpath = "";
    this.length = -1;
    this.type = ValueMetaInterface.TYPE_STRING;
    this.format = "";
    this.trimtype = TYPE_TRIM_NONE.getId();
    this.elementtype = ELEMENT_TYPE_NODE.getId();
    this.resulttype = RESULT_TYPE_VALUE_OF.getId();
    this.groupSymbol = "";
    this.decimalSymbol = "";
    this.currencySymbol = "";
    this.precision = -1;
    this.repeat = false;
    this.demlimiter = ",";
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
    retval.append( "        " ).append( XMLHandler.addTagValue( "delimiter", getDemlimiter() ) );

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
    setDemlimiter(XMLHandler.getTagValue( fnode, "delimiter" )  );
  }

  public static final int getTrimTypeByCode( final String tt ) {
    return trimTypeCode.getByName(tt).getId();
  }

  public static final int getElementTypeByCode( final String tt ) {
    return ElementTypeCode.getByName(tt).getId();
  }

  public static final int getTrimTypeByDesc( final String tt ) {
    return ElementTypeCode.getByDesc(tt).getId();
  }

  public static final int getElementTypeByDesc( final String tt ) {
    return ElementTypeCode.getByDesc(tt).getId();
  }

  public static final String getTrimTypeCode( final int i ) {
    return trimTypeCode.getById(i).getValue();
  }

  public static final String getElementTypeCode( final int i ) {
    return  ElementTypeCode.getById(i).getValue();
  }

  public static final String getTrimTypeDesc( final int i ) {
    return trimTypeCode.getById(i).getDesc();
  }

  public static final String getElementTypeDesc( final int i ) {
    return ElementTypeCode.getById(i).getDesc();
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
    return ResultTypeCode.getByDesc(tt).getId();
  }

  public String getResultTypeDesc() {
    return getResultTypeDesc( resulttype );
  }

  public static final String getResultTypeDesc( final int i ) {
    return ResultTypeCode.getById(i).getDesc();
  }

  public int getResultType() {
    return resulttype;
  }

  public void setResultType( final int resulttype ) {
    this.resulttype = resulttype;
  }

  public static final int getResultTypeByCode( final String tt ) {
    return ResultTypeCode.getByName(tt).getId();
  }

  public static final String getResultTypeCode( final int i ) {
    return ResultTypeCode.getById(i).getValue();
  }

  public String getResultTypeCode() {
    return getResultTypeCode( resulttype );
  }

  public String getDemlimiter() {
    return demlimiter;
  }

  public void setDemlimiter(String demlimiter) {
    this.demlimiter = demlimiter;
  }
}
