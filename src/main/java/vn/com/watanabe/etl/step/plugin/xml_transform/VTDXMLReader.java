package vn.com.watanabe.etl.step.plugin.xml_transform;

import com.ximpleware.*;

import org.pentaho.di.core.exception.KettleException;

public class VTDXMLReader implements XMLReader {
    VTDGen vtdGen;
    VTDNav vtdNav;
    AutoPilot autoPilot;
    String xml;

    public VTDXMLReader() {
        this.vtdGen = new VTDGen();
    }

    @Override
    public void bind(String xml) throws KettleException {
        // TODO Auto-generated method stub
        try {
            vtdGen.setDoc(xml.getBytes());
            this.xml = xml;
            vtdGen.parse(true);
            vtdNav = vtdGen.getNav();
            autoPilot = new AutoPilot();
            autoPilot.bind(vtdNav);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new KettleException("error in try parse xml", e);
        }

    }

    @Override
    public String getFirstNodeValue(String xpath) throws KettleException{
        // TODO Auto-generated method stub
        String result = "";
        int i;
        vtdNav.push();
        try {
            autoPilot.selectXPath(xpath);
            if ((i = autoPilot.evalXPath()) != -1) {    
                result=vtdNav.toString(vtdNav.getText());  
            }            
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new KettleException("error in try getFirstNodeXML", e);
        }finally{
            vtdNav.pop();
        }
    }

    @Override
    public String getFirstNodeXML(String xpath) throws KettleException {
        // TODO Auto-generated method stub
        String result = "";
        int i;
        vtdNav.push();
        try {
            autoPilot.selectXPath(xpath);
            if ((i = autoPilot.evalXPath()) != -1) {            
                long elementOffset = vtdNav.getElementFragment();
                int contentStartIndex = (int) elementOffset;    
                int contentEndIndex = contentStartIndex + (int) (elementOffset >> 32);
                result= this.xml.substring(contentStartIndex, contentEndIndex);           
    
            }
            
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new KettleException("error in try getFirstNodeXML", e);
        }finally{
            vtdNav.pop();
        }
        
    }

    @Override
    public String getAttribute(String xpath) throws KettleException {
        // TODO Auto-generated method stub
        String result = "";
        vtdNav.push();
        try {
            autoPilot.selectXPath(xpath);
            int i;
            if ((i = autoPilot.evalXPath()) != -1) {
                result = vtdNav.toString(i + 1);
            }            
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new KettleException("error in try get attribute", e);
        } finally{
            vtdNav.pop();
        }
        
    }

    @Override
    public String getNodeValues(String xpath, String delimiter) throws KettleException {
        // TODO Auto-generated method stub
        StringBuilder result = new StringBuilder();
        int i;
        int size=0;
        boolean first= true;
        vtdNav.push();
        try {
            autoPilot.selectXPath(xpath);
            while ((i = autoPilot.evalXPath()) != -1) {   
                String row=vtdNav.toString(vtdNav.getText());
                if(row!=null){
                    if(first){
                        first=false;
                    }else{
                        result.append(delimiter);
                    }
                    result.append(row);
                }                
            }            
            return result.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new KettleException("error in try getFirstNodeXML", e);
        }finally{
            vtdNav.pop();
        }
    }

    @Override
    public String getNodeValuesFixedSize(String xpath, String delimiter, int length) throws KettleException {
        // TODO Auto-generated method stub
        StringBuilder result = new StringBuilder();
        int i;
        int size=0;
        boolean first= true;
        vtdNav.push();
        try {
            autoPilot.selectXPath(xpath);
            while ((i = autoPilot.evalXPath()) != -1) {   
                String row=vtdNav.toString(vtdNav.getText());
                if(row!=null){
                    size+= row.length();                    
                    if(first){
                        first=false;
                        // if first and size> length then try substr
                        if(size>= length){
                            result.append(row.substring(0, length-1));
                        }else{
                            result.append(row);
                        }
                    }else{
                        if(size>= length){
                            break;
                        }
                        result.append(delimiter);
                        result.append(row);
                    }
                    
                }                
            }            
            return result.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new KettleException("error in try getFirstNodeXML", e);
        }finally{
            vtdNav.pop();
        }
    }

    @Override
    public String getNodesXML(String xpath) throws KettleException {
        // TODO Auto-generated method stub
        StringBuilder result = new StringBuilder();
        result.append("<row>");
        int i;
        vtdNav.push();
        try {
            autoPilot.selectXPath(xpath);
            while ((i = autoPilot.evalXPath()) != -1) {            
                long elementOffset = vtdNav.getElementFragment();
                int contentStartIndex = (int) elementOffset;    
                int contentEndIndex = contentStartIndex + (int) (elementOffset >> 32);
                result.append(this.xml.substring(contentStartIndex, contentEndIndex));           
    
            }
            result.append("</row>");   
            return result.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new KettleException("error in try getFirstNodeXML", e);
        }finally{
            vtdNav.pop();
        }
    }

    @Override
    public Float getSum(String xpath) throws KettleException {
        // TODO Auto-generated method stub
        float result=0;
        int i;
        int size=0;
        boolean first= true;
        vtdNav.push();
        try {
            autoPilot.selectXPath(xpath);
            while ((i = autoPilot.evalXPath()) != -1) {   
                String row=vtdNav.toString(vtdNav.getText());
                if(row!=null){
                    result+= Float.parseFloat(row);
                }                
            }            
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new KettleException("error in try getFirstNodeXML", e);
        }finally{
            vtdNav.pop();
        }
    }
}