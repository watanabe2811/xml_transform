/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * This class is part of the demo step plug-in implementation. It demonstrates
 * the basics of developing a plug-in step for PDI.
 * 
 * The demo step adds a new string field to the row stream and sets its value to
 * "Hello World!". The user may select the name of the new field.
 * 
 * This class is the implementation of StepInterface. Classes implementing this
 * interface need to:
 * 
 * - initialize the step - execute the row processing logic - dispose of the
 * step
 * 
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data
 * interface instead.
 * 
 */

public class XMLTransform extends BaseStep implements StepInterface {

  private static final Class<?> PKG = XMLTransformMetaData.class; // for i18n purposes
  public static String DEFAULT_PREFIX = "XMLTransform";
  XMLTranformData data;
  XMLTransformMetaData meta;
  int numInputFields = 0;
  int indexOfXmlField;

  /**
   * The constructor should simply pass on its arguments to the parent class.
   * 
   * @param s                 step description
   * @param stepDataInterface step data class
   * @param c                 step copy
   * @param t                 transformation description
   * @param dis               transformation executing
   */
  public XMLTransform(final StepMeta s, final StepDataInterface stepDataInterface, final int c, final TransMeta t,
      final Trans dis) {
    super(s, stepDataInterface, c, t, dis);
  }

  /**
   * This method is called by PDI during transformation startup.
   * 
   * It should initialize required for step execution.
   * 
   * The meta and data implementations passed in can safely be cast to the step's
   * respective implementations.
   * 
   * It is mandatory that super.init() is called to ensure correct behavior.
   * 
   * Typical tasks executed here are establishing the connection to a database, as
   * wall as obtaining resources, like file handles.
   * 
   * @param smi step meta interface implementation, containing the step settings
   * @param sdi step data interface implementation, used to store runtime
   *            information
   * 
   * @return true if initialization completed successfully, false if there was an
   *         error preventing the step from working.
   * 
   */
  public boolean init(final StepMetaInterface smi, final StepDataInterface sdi) {
    // Casting to step-specific implementation classes is safe
    meta = (XMLTransformMetaData) smi;
    data = (XMLTranformData) sdi;
    if (!super.init(meta, data)) {
      return false;
    }

    // Add any step-specific initialization that may be needed here
    return true;
  }

  /**
   * Once the transformation starts executing, the processRow() method is called
   * repeatedly by PDI for as long as it returns true. To indicate that a step has
   * finished processing rows this method must call setOutputDone() and return
   * false;
   * 
   * Steps which process incoming rows typically call getRow() to read a single
   * row from the input stream, change or add row content, call putRow() to pass
   * the changed row on and return true. If getRow() returns null, no more rows
   * are expected to come in, and the processRow() implementation calls
   * setOutputDone() and returns false to indicate that it is done too.
   * 
   * Steps which generate rows typically construct a new row Object[] using a call
   * to RowDataUtil.allocateRowData(numberOfFields), add row content, and call
   * putRow() to pass the new row on. Above process may happen in a loop to
   * generate multiple rows, at the end of which processRow() would call
   * setOutputDone() and return false;
   * 
   * @param smi the step meta interface containing the step settings
   * @param sdi the step data interface that should be used to store
   * 
   * @return true to indicate that the function should be called again, false if
   *         the step is done
   */
  public boolean processRow(final StepMetaInterface smi, final StepDataInterface sdi) throws KettleException {

    // safely cast the step settings (meta) and runtime info (data) to specific
    // implementations
    meta = (XMLTransformMetaData) smi;
    data = (XMLTranformData) sdi;

    // get incoming row, getRow() potentially blocks waiting for more rows, returns
    // null if no more rows expected
    final Object[] r = getRow();

    // if no more rows are expected, indicate step is finished and processRow()
    // should not be called again
    if (r == null) {
      setOutputDone();
      return false;
    }

    // the "first" flag is inherited from the base step implementation
    // it is used to guard some processing tasks, like figuring out field indexes
    // in the row structure that only need to be done once
    if (first) {
      first = false;
      // clone the input row structure and place it in our data object
      RowMetaInterface inputRowMeta = getInputRowMeta();
      data.outputRowMeta = (RowMetaInterface) inputRowMeta.clone();
      this.numInputFields = inputRowMeta.size();
      indexOfXmlField = inputRowMeta.indexOfValue( meta.getXMLField() );
      // use meta.getFields() to change it, so it reflects the output row structure
      // add new fields
      meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);

      // Create convert meta-data objects that will contain Date & Number formatters
      // For String to <type> conversions, we allocate a conversion meta data row as
      // well...
      //
      data.convertRowMeta = data.outputRowMeta.cloneToType(ValueMetaInterface.TYPE_STRING);
    }
    String xml = getInputRowMeta().getString( r, indexOfXmlField );
    
    // Grab a row
    final Object[] outputRow = RowDataUtil.resizeArray(r, data.outputRowMeta.size());
    if(xml!=null){
      XMLReader reader = getReader();
      reader.bind(xml);
      processParseFields(outputRow, reader);
    }
    
    // Object[] r = getXMLRow();
    // put the row to the output row stream
    putRow(data.outputRowMeta, outputRow);
    return true;
  }
  public XMLReader getReader(){
    return new VTDXMLReader();
  }

  public void processParseFields(Object[] outputRow,XMLReader reader) throws KettleException {
    int currentIndex = numInputFields;
    List<XMLTransformField> inputFields = meta.getInputFields();
    int numFields = inputFields.size();
    for(int i =0; i< numFields; i++){
      XMLTransformField xmlDataField = inputFields.get(i);
      processPutRow(outputRow, reader, currentIndex, xmlDataField);
      currentIndex++;
    }
  }

  public void processPutRow(Object[] outputRow,XMLReader reader, int currentIndex, 
  XMLTransformField xmlDataField)
      throws KettleException {
      String nodevalue = getFieldValue(reader, xmlDataField);
      nodevalue=trim(nodevalue, xmlDataField);
      pushFieldToRow(outputRow, currentIndex, nodevalue);
  }
  public String getFieldValue(XMLReader reader, XMLTransformField xmlDataField) throws KettleException {
    String xpath = xmlDataField.getXPath();
    String nodevalue=null;
    int elementType = xmlDataField.getElementType();
    int type = xmlDataField.getResultType();
    if(elementType == XMLTransformField.ELEMENT_TYPE_ATTRIBUT.getId()){
      nodevalue = reader.getAttribute(xpath);
    }else if(elementType== XMLTransformField.ELEMENT_TYPE_NODE.getId()){
      if(type == XMLTransformField.RESULT_TYPE_TYPE_SINGLE_NODE.getId()){
        nodevalue = reader.getFirstNodeXML(xpath);
      }else{
        nodevalue = reader.getFirstNodeValue(xpath);
      }
    }else if(elementType == XMLTransformField.ELEMENT_TYPE_NODE_MULTI.getId()){
      if(type == XMLTransformField.RESULT_TYPE_TYPE_SINGLE_NODE.getId()){
        nodevalue = reader.getNodesXML(xpath);
      }else if(type== XMLTransformField.RESULT_TYPE_FIST_VALUE.getId()){
        nodevalue = reader.getFirstNodeValue(xpath);
      }else if(type== XMLTransformField.RESULT_TYPE_TYPE_SUM.getId()){
        nodevalue = String.valueOf(reader.getSum(xpath));
      }else if(type == XMLTransformField.RESULT_TYPE_VALUE_OF.getId()){
        nodevalue = reader.getNodeValues(xpath, xmlDataField.getDemlimiter());
      }else if(type == XMLTransformField.RESULT_TYPE_VALUE_OF_FIXED_SIZE.getId()){
        nodevalue = reader.getNodeValuesFixedSize(xpath, xmlDataField.getDemlimiter(), xmlDataField.getLength());
      }
    }
    return nodevalue;
  }

  public String trim(String nodevalue, XMLTransformField xmlDataField) {
    // Do trimming
    int trimType = xmlDataField.getTrimType();
    if (trimType == XMLTransformField.TYPE_TRIM_LEFT.getId()) {
      nodevalue = Const.ltrim(nodevalue);
    } else if (trimType == XMLTransformField.TYPE_TRIM_LEFT.getId()) {
      nodevalue = Const.rtrim(nodevalue);
    } else if (trimType == XMLTransformField.TYPE_TRIM_LEFT.getId()) {
      nodevalue = Const.trim(nodevalue);
    }
    return nodevalue;
  }

  public void pushFieldToRow(Object[] outputRowData, int currentIndex, String nodeValue)
      throws KettleValueException {
    ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(currentIndex);
    ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(currentIndex);
    outputRowData[currentIndex] = targetValueMeta.convertData(sourceValueMeta, nodeValue);
  }

  /**
   * This method is called by PDI once the step is done processing.
   * 
   * The dispose() method is the counterpart to init() and should release any
   * resources acquired for step execution like file handles or database
   * connections.
   * 
   * The meta and data implementations passed in can safely be cast to the step's
   * respective implementations.
   * 
   * It is mandatory that super.dispose() is called to ensure correct behavior.
   * 
   * @param smi step meta interface implementation, containing the step settings
   * @param sdi step data interface implementation, used to store runtime
   *            information
   */
  public void dispose(final StepMetaInterface smi, final StepDataInterface sdi) {

    // Casting to step-specific implementation classes is safe
    meta = (XMLTransformMetaData) smi;
    data = (XMLTranformData) sdi;

    // Add any step-specific initialization that may be needed here

    // Call superclass dispose()
    super.dispose( meta, data );
  }
}
