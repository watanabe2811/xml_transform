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

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.Prefix;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepMetaInterface.
 * Classes implementing this interface need to:
 * 
 * - keep track of the step settings
 * - serialize step settings both to xml and a repository
 * - provide new instances of objects implementing StepDialogInterface, StepInterface and StepDataInterface
 * - report on how the step modifies the meta-data of the row-stream (row structure and field types)
 * - perform a sanity-check on the settings provided by the user 
 * 
 */

@Step(
  id = "XMLTransform",
  name = "XMLTransform.Name",
  description = "XMLTransform.TooltipDesc",
  image = "vn/com/watanabe/etl/step/plugin/xml_transform/resources/xml_transform.svg",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform",
  i18nPackageName = "vn.com.watanabe.etl.step.plugin.xml_transform",
  documentationUrl = "XMLTransform.DocumentationURL",
  casesUrl = "XMLTransform.CasesURL",
  forumUrl = "XMLTransform.ForumURL"
  )
@InjectionSupported( localizationPrefix = "XMLTransformMetaData.Injection." )
public class XMLTransformMetaData extends BaseStepMeta implements StepMetaInterface {

  /**
   *  The PKG member is used when looking up internationalized strings.
   *  The properties file with localized keys is expected to reside in 
   *  {the package of the class specified}/messages/messages_{locale}.properties   
   */
  private static final Class<?> PKG = XMLTransformMetaData.class; // for i18n purposes
  public static String DEFAULT_PREFIX="XMLTransform";

   /** Is In fields */
   private String xmlField;
     /** The fields to import... */
  private List<XMLTransformField> inputFields=new ArrayList<XMLTransformField>();

  /**
   * Constructor should call super() to make sure the base class has a chance to initialize properly.
   */
  public XMLTransformMetaData() {
    super();
  }

  /**
   * Called by Spoon to get a new instance of the SWT dialog for the step.
   * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
   * 
   * @param shell    an SWT Shell
   * @param meta     description of the step 
   * @param transMeta  description of the the transformation 
   * @param name    the name of the step
   * @return       new instance of a dialog for this step 
   */
  public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
    // return new XMLTransformDialog( shell, meta, transMeta, name );
    return new XMLTransformDialog( shell, meta, transMeta, name );
  }

  /**
   * Called by PDI to get a new instance of the step implementation. 
   * A standard implementation passing the arguments to the constructor of the step class is recommended.
   * 
   * @param stepMeta        description of the step
   * @param stepDataInterface    instance of a step data class
   * @param cnr          copy number
   * @param transMeta        description of the transformation
   * @param disp          runtime implementation of the transformation
   * @return            the new instance of a step implementation 
   */
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans disp ) {
    return new XMLTransform( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  /**
   * Called by PDI to get a new instance of the step data class.
   */
  public StepDataInterface getStepData() {
    return new XMLTranformData();
  }

  /**
   * This method is called every time a new step is created and should allocate/set the step configuration
   * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
   */
  public void setDefault() {
    xmlField = "";
    this.inputFields.clear();
  }


  /**
   * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
   * step meta object. Be sure to create proper deep copies if the step configuration is stored in
   * modifiable objects.
   * 
   * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
   * a deep copy.
   * 
   * @return a deep copy of this
   */
  public Object clone() {
    XMLTransformMetaData retval = (XMLTransformMetaData) super.clone();
    int size = inputFields.size();
    for(int i = 0; i< size;i++){
      retval.inputFields.add(this.inputFields.get(i));
    }
    return retval;
  }

  /**
   * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
   * return value is an XML fragment consisting of one or more XML tags.  
   * 
   * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
   * 
   * @return a string containing the XML serialization of this step
   */
  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "    <fields>" ).append( Const.CR );
    int size = inputFields.size();
    for ( int i = 0; i < size; i++ ) {
      XMLTransformField field = inputFields.get(i);
      retval.append( field.getXML() );
    }
    retval.append( "    </fields>" ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( "XmlField", xmlField ) );
    return retval.toString();
  }

  /**
   * This method is called by PDI when a step needs to load its configuration from XML.
   * 
   * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
   * XML node passed in.
   * 
   * @param stepnode  the XML node containing the configuration
   * @param databases  the databases available in the transformation
   * @param metaStore the metaStore to optionally read from
   */
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      readData( stepnode );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Demo plugin unable to read step info from XML node", e );
    }
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      inputFields.clear();

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        XMLTransformField field = new XMLTransformField( fnode );
        inputFields.add(field);
      }

      xmlField = XMLHandler.getTagValue( stepnode, "XmlField" );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, DEFAULT_PREFIX+ ".Exception.ErrorLoadingXML", e
          .toString() ) );
    }
  }

  /**
   * This method is called by Spoon when a step needs to serialize its configuration to a repository.
   * The repository implementation provides the necessary methods to save the step attributes.
   *
   * @param rep                 the repository to save to
   * @param metaStore           the metaStore to optionally write to
   * @param id_transformation   the id to use for the transformation when saving
   * @param id_step             the id to use for the step  when saving
   */
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      int size = inputFields.size();
      for ( int i = 0; i < size; i++ ) {
        XMLTransformField field = inputFields.get(i);

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_xpath", field.getXPath() );
        rep.saveStepAttribute( id_transformation, id_step, i, "element_type", field.getElementTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, "result_type", field.getResultTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format", field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", field.getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_group", field.getGroupSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_length", field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", field.getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_trim_type", field.getTrimTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_repeat", field.isRepeated() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_delimiter", field.getDemlimiter());
      }

      rep.saveStepAttribute( id_transformation, id_step, "XmlField", xmlField );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, DEFAULT_PREFIX+ ".Exception.ErrorSavingToRepository", ""
          + id_step ), e );
    }
  }

  /**
   * This method is called by PDI when a step needs to read its configuration from a repository.
   * The repository implementation provides the necessary methods to read the step attributes.
   * 
   * @param rep        the repository to read from
   * @param metaStore  the metaStore to optionally read from
   * @param id_step    the id of the step being read
   * @param databases  the databases available in the transformation
   */
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {

    try {
      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );

      this.inputFields.clear();
      
      for ( int i = 0; i < nrFields; i++ ) {
        XMLTransformField field = new XMLTransformField();

        field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field.setXPath( rep.getStepAttributeString( id_step, i, "field_xpath" ) );
        field.setElementType( XMLTransformField.getElementTypeByCode( rep.getStepAttributeString( id_step, i,
            "element_type" ) ) );
        field.setResultType( XMLTransformField.getResultTypeByCode( rep
            .getStepAttributeString( id_step, i, "result_type" ) ) );
        field.setType( ValueMeta.getType( rep.getStepAttributeString( id_step, i, "field_type" ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, "field_currency" ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, "field_group" ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
        field.setTrimType( XMLTransformField.getTrimTypeByCode( rep
            .getStepAttributeString( id_step, i, "field_trim_type" ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, "field_repeat" ) );
        field.setDemlimiter(rep.getStepAttributeString( id_step, i, "field_delimiter" ));

        inputFields.add(field);
      }

      xmlField = rep.getStepAttributeString( id_step, "XmlField" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, DEFAULT_PREFIX+".Exception.ErrorReadingRepository" ), e );
    }
  }

  /**
   * This method is called to determine the changes the step is making to the row-stream.
   * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
   * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
   * row-stream.
   * 
   * @param inputRowMeta    the row structure coming in to the step
   * @param name         the name of the step making the changes
   * @param info        row structures of any info steps coming in
   * @param nextStep      the description of a step this step is passing rows to
   * @param space        the variable space for resolving variables
   * @param repository    the repository instance optionally read from
   * @param metaStore      the metaStore to optionally read from
   */
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    int i;
    int size = inputFields.size();
    for ( i = 0; i < size; i++ ) {
      XMLTransformField field = inputFields.get(i);

      int type = field.getType();
      if ( type == ValueMeta.TYPE_NONE ) {
        type = ValueMeta.TYPE_STRING;
      }
      try {
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( space.environmentSubstitute( field.getName() ), type );
        v.setLength( field.getLength() );
        v.setPrecision( field.getPrecision() );
        v.setOrigin( name );
        v.setConversionMask( field.getFormat() );
        v.setDecimalSymbol( field.getDecimalSymbol() );
        v.setGroupingSymbol( field.getGroupSymbol() );
        v.setCurrencySymbol( field.getCurrencySymbol() );
      
        r.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }

    // Add additional fields
  }

  /**
   * This method is called when the user selects the "Verify Transformation" option in Spoon. 
   * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
   * The method should perform as many checks as necessary to catch design-time errors.
   * 
   * Typical checks include:
   * - verify that all mandatory configuration is given
   * - verify that the step receives any input, unless it's a row generating step
   * - verify that the step does not receive any input if it does not take them into account
   * - verify that the step finds fields it relies on in the row-stream
   * 
   *   @param remarks    the list of remarks to append to
   *   @param transMeta  the description of the transformation
   *   @param stepMeta  the description of the step
   *   @param prev      the structure of the incoming row-stream
   *   @param input      names of steps sending input to the step
   *   @param output    names of steps this step is sending output to
   *   @param info      fields coming in from info steps 
   *   @param metaStore  metaStore to optionally read from
   */
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    // See if we get input...
    if ( input.length <= 0 ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              DEFAULT_PREFIX+".CheckResult.NoInputExpected" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
          DEFAULT_PREFIX+".CheckResult.NoInput" ), stepMeta );
      remarks.add( cr );
    }

    
    if ( Utils.isEmpty( getXMLField() ) ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "GetXMLDataMeta.CheckResult.NoField" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "GetXMLDataMeta.CheckResult.FieldOk" ), stepMeta );
      remarks.add( cr );
    }
  }
  /**
   * Get XML field.
   */
  public String getXMLField() {
    return xmlField;
  }

  /**
   * Set XML field.
   */
  public void setXMLField( String xmlField ) {
    this.xmlField = xmlField;
  }

  public List<XMLTransformField> getInputFields() {
    return inputFields;
  }

  public void setInputFields(List<XMLTransformField> inputFields) {
    this.inputFields = inputFields;
  }
  public void addInputField(XMLTransformField inputField){
    this.inputFields.add(inputField);
  }
  
}
