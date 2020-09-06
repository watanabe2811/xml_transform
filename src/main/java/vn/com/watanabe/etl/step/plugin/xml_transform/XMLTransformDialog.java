package vn.com.watanabe.etl.step.plugin.xml_transform;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.graphics.Cursor;


public class XMLTransformDialog extends BaseStepDialog implements StepDialogInterface {

    private CTabFolder wTabFolder;
    private FormData fdTabFolder;

    private CTabItem wFieldsTab;

    private Composite  wFieldsComp;
    private FormData fdFieldsComp;
    private TableView wFields;
    private FormData fdFields;

    // FIle tab 
    private CTabItem wFileTab;
    private Composite wFileComp;
    private Group wOutputField;
    private FormData fdFileComp;
    ///
    private Label wlXmlStreamField;
    private FormData fdlXMLStreamField;
    private Button wXMLStreamField;
    private FormData fdXSDFileField;

    /// 2
    private Label wlXMLField;
    private FormData fdlXMLField;
    ////
    private CCombo wXMLField;
    private FormData fdXMLField;


    //
    private int middle;
    private int margin;
    private ModifyListener lsMod;

    private XMLTransformMetaData input;

    private String XMLSource = null;
    private static String EMPTY_FIELDS = "<EMPTY>";

    ////////////////////////////////////////////
    private static Class<?> PKG = XMLTransformMetaData.class; // for i18n purposes, needed by Translator2!!
    private static String DEFAULT_PREFIX="XMLTransform";
    //////////////////////////////////////////////////
    public static final int[] dateLengths = new int[] { 23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6 };
    public XMLTransformDialog(Shell parent, StepMetaInterface baseStepMeta, TransMeta transMeta, String stepname) {
        super(parent,(BaseStepMeta) baseStepMeta, transMeta, stepname);
        // TODO Auto-generated constructor stub
        input = (XMLTransformMetaData) baseStepMeta;
    }

    @Override
    public String open() {
        // TODO Auto-generated method stub
        final Display display = initDialog();

        // Stepname line
        genStepNameLine();
        // File tab
        genFileTab();
        // Fields tab...
        //
        genFieldsTabs();

        // addAdditionalFieldsTab();

        setupLayouTabs();

        genButtons();

        wStepname.addSelectionListener(lsDef);
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(final ShellEvent e) {
                cancel();
            }
        });

        wTabFolder.setSelection(0);

        // Set the shell size, based upon previous time...
        setSize();
        loadData(input);
        // ActiveXmlStreamField();
        // setIncludeFilename();
        // setIncludeRownum();
        input.setChanged(changed);
        wFields.optWidth(true);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return stepname;
    }
    private Display initDialog() {
        final Shell parent = getParent();
        final Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, input);

        lsMod = new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                input.setChanged();
            }
        };
        changed = input.hasChanged();

        final FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, DEFAULT_PREFIX+".DialogTitle"));

        middle = props.getMiddlePct();
        margin = Const.MARGIN;
        return display;
    }

    private void genStepNameLine() {
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.top = new FormAttachment(0, margin);
        fdlStepname.right = new FormAttachment(middle, -margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
    }

    private void genFieldsTabs() {
        wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
        wFieldsTab.setText(BaseMessages.getString(PKG, DEFAULT_PREFIX+".Fields.Tab"));

        final FormLayout fieldsLayout = new FormLayout();
        fieldsLayout.marginWidth = Const.FORM_MARGIN;
        fieldsLayout.marginHeight = Const.FORM_MARGIN;

        wFieldsComp = new Composite(wTabFolder, SWT.NONE);
        wFieldsComp.setLayout(fieldsLayout);
        props.setLook(wFieldsComp);

        wGet = new Button(wFieldsComp, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, DEFAULT_PREFIX+".GetFields.Button"));
        fdGet = new FormData();
        fdGet.left = new FormAttachment(50, 0);
        fdGet.bottom = new FormAttachment(100, 0);
        wGet.setLayoutData(fdGet);
        
        final int FieldsRows = input.getInputFields().size();
        // int FieldsRows = 0;
        final ColumnInfo[] colinf = new ColumnInfo[] {
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Name.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.XPath.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Element.Column"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, XMLTransformField.ElementTypeDesc, true),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.ResultType.Column"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, XMLTransformField.ResultTypeDesc, true),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Type.Column"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes(), true),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Format.Column"),
                        ColumnInfo.COLUMN_TYPE_FORMAT, 4),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Length.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Precision.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Currency.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Decimal.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Group.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.TrimType.Column"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, XMLTransformField.trimTypeDesc, true),
                new ColumnInfo(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Repeat.Column"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"),
                                BaseMessages.getString(PKG, "System.Combo.No") },
                        true),

        };

        colinf[0].setUsingVariables(true);
        colinf[0].setToolTip(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.Name.Column.Tooltip"));
        colinf[1].setUsingVariables(true);
        colinf[1].setToolTip(BaseMessages.getString(PKG, DEFAULT_PREFIX+".FieldsTable.XPath.Column.Tooltip"));

        wFields = new TableView(transMeta, wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod,
                props);

        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(0, 0);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(wGet, -margin);
        wFields.setLayoutData(fdFields);

        fdFieldsComp = new FormData();
        fdFieldsComp.left = new FormAttachment(0, 0);
        fdFieldsComp.top = new FormAttachment(0, 0);
        fdFieldsComp.right = new FormAttachment(100, 0);
        fdFieldsComp.bottom = new FormAttachment(100, 0);
        wFieldsComp.setLayoutData(fdFieldsComp);

        wFieldsComp.layout();
        wFieldsTab.setControl(wFieldsComp);
    }

    private void setupLayouTabs() {
        fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(wStepname, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);
    }
    private void genButtons() {
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

        // wPreview = new Button(shell, SWT.PUSH);
        // wPreview.setText(BaseMessages.getString(PKG, DEFAULT_PREFIX+".Button.PreviewRows"));

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

        // Add listeners
        lsOK = new Listener() {
            public void handleEvent(final Event e) {
                ok();
            }
        };
        lsCancel = new Listener() {
            public void handleEvent(final Event e) {
                cancel();
            }
        };

        wOK.addListener(SWT.Selection, lsOK);
        // wGet.addListener(SWT.Selection, lsGet);
        // wPreview.addListener(SWT.Selection, lsPreview);
        wGet.setEnabled(false);

        wCancel.addListener(SWT.Selection, lsCancel);

        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(final SelectionEvent e) {
                ok();
            }
        };
    }


    private void cancel() {
        stepname = null;
        input.setChanged( changed );
        dispose();
      }
    
      public void dispose() {
        super.dispose();
      }
    
      private void ok() {
        try {
          saveData( input );
        } catch ( KettleException e ) {
          new ErrorDialog( shell, BaseMessages.getString( PKG, DEFAULT_PREFIX+".ErrorParsingData.DialogTitle" ),
              BaseMessages.getString( PKG, DEFAULT_PREFIX+".ErrorParsingData.DialogMessage" ), e );
        }
        dispose();
      }
      private void saveData( XMLTransformMetaData in ) throws KettleException {
        stepname = wStepname.getText(); // return value
        in.setXMLField( wXMLField.getText() );
    
        int nrFields = wFields.nrNonEmpty();
        for ( int i = 0; i < nrFields; i++ ) {
        XMLTransformField field = new XMLTransformField();
    
          TableItem item = wFields.getNonEmpty( i );
    
          field.setName( item.getText( 1 ) );
          field.setXPath( item.getText( 2 ) );
          field.setElementType( XMLTransformField.getElementTypeByDesc( item.getText( 3 ) ) );
          field.setResultType( XMLTransformField.getResultTypeByDesc( item.getText( 4 ) ) );
          field.setType( ValueMeta.getType( item.getText( 5 ) ) );
          field.setFormat( item.getText( 6 ) );
          field.setLength( Const.toInt( item.getText( 7 ), -1 ) );
          field.setPrecision( Const.toInt( item.getText( 8 ), -1 ) );
          field.setCurrencySymbol( item.getText( 9 ) );
          field.setDecimalSymbol( item.getText( 10 ) );
          field.setGroupSymbol( item.getText( 11 ) );
          field.setTrimType( XMLTransformField.getTrimTypeByDesc( item.getText( 12 ) ) );
          field.setRepeated( BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( 13 ) ) );
    
          // CHECKSTYLE:Indentation:OFF
          in.addInputField(field);
        }
      }
    public void genFileTab(){
        initFileTab();

        genXMLSourceForm();
        // isXMLInField();
        genSelectXMLFieldForm();

        wFileComp.layout();
        wFileTab.setControl(wFileComp);
    }
    private void initFileTab() {
        wFileTab = new CTabItem(wTabFolder, SWT.NONE);
        wFileTab.setText(BaseMessages.getString(PKG, DEFAULT_PREFIX+".File.Tab"));

        wFileComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wFileComp);

        final FormLayout fileLayout = new FormLayout();
        fileLayout.marginWidth = 3;
        fileLayout.marginHeight = 3;
        wFileComp.setLayout(fileLayout);


        fdFileComp = new FormData();
        fdFileComp.left = new FormAttachment(0, 0);
        fdFileComp.top = new FormAttachment(0, 0);
        fdFileComp.right = new FormAttachment(100, 0);
        fdFileComp.bottom = new FormAttachment(100, 0);
        wFileComp.setLayoutData(fdFileComp);

        
    }
    private void genXMLSourceForm() {
        wOutputField = new Group(wFileComp, SWT.SHADOW_NONE);
        props.setLook(wOutputField);
        wOutputField.setText(BaseMessages.getString(PKG, DEFAULT_PREFIX+".wOutputField.Label"));

        final FormLayout outputfieldgroupLayout = new FormLayout();
        outputfieldgroupLayout.marginWidth = 10;
        outputfieldgroupLayout.marginHeight = 10;
        wOutputField.setLayout(outputfieldgroupLayout);
    }

    private void genSelectXMLFieldForm() {
        wlXMLField = new Label(wOutputField, SWT.RIGHT);
        wlXMLField.setText(BaseMessages.getString(PKG, DEFAULT_PREFIX+".wlXMLField.Label"));
        props.setLook(wlXMLField);


        fdlXMLField = new FormData();
        fdlXMLField.left = new FormAttachment(0, -margin);
        // fdlXMLField.top = new FormAttachment(wreadUrl, margin);
        fdlXMLField.right = new FormAttachment(middle, -2 * margin);
        wlXMLField.setLayoutData(fdlXMLField);

        wXMLField = new CCombo(wOutputField, SWT.BORDER | SWT.READ_ONLY);
        wXMLField.setEditable(true);
        props.setLook(wXMLField);
        wXMLField.addModifyListener(lsMod);

        fdXMLField = new FormData();
        fdXMLField.left = new FormAttachment(middle, -margin);
        // fdXMLField.top = new FormAttachment(wreadUrl, margin);
        fdXMLField.right = new FormAttachment(100, -margin);        
        wXMLField.setLayoutData(fdXMLField);
        wXMLField.addFocusListener(new FocusListener() {
            public void focusLost(final org.eclipse.swt.events.FocusEvent e) {
            }

            public void focusGained(final org.eclipse.swt.events.FocusEvent e) {
                final Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                setXMLStreamField();
                shell.setCursor(null);
                busy.dispose();
            }
        });

        // fdOutputField = new FormData();
        // fdOutputField.left = new FormAttachment(0, margin);
        // fdOutputField.top = new FormAttachment(wFilenameList, margin);
        // fdOutputField.right = new FormAttachment(100, -margin);
        // wOutputField.setLayoutData(fdOutputField);
    }

    private void setXMLStreamField() {
        try {

            wXMLField.removeAll();

            final RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r != null) {
                final String[] fieldNames = r.getFieldNames();
                if (fieldNames != null) {

                    for (int i = 0; i < fieldNames.length; i++) {
                        wXMLField.add(fieldNames[i]);
                    }
                }
            }
        } catch (final KettleException ke) {
            if (!Const.isOSX()) { // see PDI-8871 for details
                shell.setFocus();
            }
            wXMLField.add(EMPTY_FIELDS);
            wXMLField.setText(EMPTY_FIELDS);
            new ErrorDialog(shell, BaseMessages.getString(PKG, DEFAULT_PREFIX+".FailedToGetFields.DialogTitle"),
                    BaseMessages.getString(PKG, DEFAULT_PREFIX+".FailedToGetFields.DialogMessage"), ke);
        }
    }

    /**
   * Read the data from the TextFileInputMeta object and show it in this dialog.
   * 
   * @param in
   *          The TextFileInputMeta object to obtain the data from.
   */
  public void loadData( XMLTransformMetaData in ) {

    if ( in.getXMLField() != null ) {
      wXMLField.setText( in.getXMLField() );
    }

    logDebug( BaseMessages.getString( PKG, DEFAULT_PREFIX+".Log.GettingFieldsInfo" ) );
    List<XMLTransformField> inputFields = in.getInputFields();
    int size = inputFields.size();
    for ( int i=0; i<size;i++) {
      XMLTransformField field = inputFields.get(i);
      if ( field != null ) {
        TableItem item = wFields.table.getItem( i );
        String name = field.getName();
        String xpath = field.getXPath();
        String element = field.getElementTypeDesc();
        String resulttype = field.getResultTypeDesc();
        String type = field.getTypeDesc();
        String format = field.getFormat();
        String length = "" + field.getLength();
        String prec = "" + field.getPrecision();
        String curr = field.getCurrencySymbol();
        String group = field.getGroupSymbol();
        String decim = field.getDecimalSymbol();
        String trim = field.getTrimTypeDesc();
        String rep =
            field.isRepeated() ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString( PKG,
                "System.Combo.No" );

        if ( name != null ) {
          item.setText( 1, name );
        }
        if ( xpath != null ) {
          item.setText( 2, xpath );
        }
        if ( element != null ) {
          item.setText( 3, element );
        }
        if ( resulttype != null ) {
          item.setText( 4, resulttype );
        }
        if ( type != null ) {
          item.setText( 5, type );
        }
        if ( format != null ) {
          item.setText( 6, format );
        }
        if ( length != null && !"-1".equals( length ) ) {
          item.setText( 7, length );
        }
        if ( prec != null && !"-1".equals( prec ) ) {
          item.setText( 8, prec );
        }
        if ( curr != null ) {
          item.setText( 9, curr );
        }
        if ( decim != null ) {
          item.setText( 10, decim );
        }
        if ( group != null ) {
          item.setText( 11, group );
        }
        if ( trim != null ) {
          item.setText( 12, trim );
        }
        if ( rep != null ) {
          item.setText( 13, rep );
        }

      }
    }

    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );


    wStepname.selectAll();
    wStepname.setFocus();
  }

}