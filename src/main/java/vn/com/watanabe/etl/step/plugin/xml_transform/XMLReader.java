package vn.com.watanabe.etl.step.plugin.xml_transform;

import org.pentaho.di.core.exception.KettleException;

public interface XMLReader {
    public void bind(String xml) throws KettleException ;
    public String getFirstNodeValue(String xpath) throws KettleException ;
    public String getFirstNodeXML(String xpath) throws KettleException ;
    public String getAttribute(String xpath) throws KettleException ;
    public String getNodeValues(String xpath, String delimiter) throws KettleException ;
    public String getNodeValuesFixedSize(String xpath, String delimiter, int length) throws KettleException ;
    public String getNodesXML(String xpath) throws KettleException ;
    public Float getSum(String xpath) throws KettleException ;

    
}