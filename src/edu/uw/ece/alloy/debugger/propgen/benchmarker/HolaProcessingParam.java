/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;

import edu.uw.ece.alloy.Compressor;

/**
 * @author fikayo
 *         
 */
public class HolaProcessingParam extends ProcessingParam {
    
    final public static HolaProcessingParam EMPTY_PARAM = new HolaProcessingParam();
    
    public static final int PRIORITY = 1;
    private final String holaFilepath;
    
    private HolaProcessingParam() {
        this("", Compressor.EMPTY_FILE);
    }
    
    private HolaProcessingParam(HolaProcessingParam param) {
        super(param.priority, param.tmpDirectory);
        
        this.holaFilepath = param.holaFilepath;
    }
    
    public HolaProcessingParam(String holaFilePath, final File tmpDirectory) {
        this(holaFilePath, HolaProcessingParam.PRIORITY, tmpDirectory);
    }
    
    /**
     * @param priority
     */
    public HolaProcessingParam(String holafilePath, final int priority, final File tmpDirectory) {
        super(priority, tmpDirectory);
        this.holaFilepath = holafilePath;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam#
     * isEmptyParam()
     */
    @Override
    public boolean isEmptyParam() {
        
        return this.equals(HolaProcessingParam.EMPTY_PARAM);
    }
    
    public String getHolaFilepath() {
        
        return this.holaFilepath;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam#
     * createItself()
     */
    @Override
    public ProcessingParam createItself() {
        
        return new HolaProcessingParam(this);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam#
     * changeTmpDirectory(java.io.File)
     */
    @Override
    public HolaProcessingParam changeTmpDirectory(File tmpDirectory) {
        
        return new HolaProcessingParam(this.holaFilepath, this.priority, tmpDirectory);
    }
}
