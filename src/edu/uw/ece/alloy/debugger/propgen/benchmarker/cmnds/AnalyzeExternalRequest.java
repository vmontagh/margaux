package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.io.File;
import java.io.IOException;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.debugger.pattern.PatternsAnalyzer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ExpressionPropertyGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;

/**
 * The command initiates a series of actions to analyze an expression. It should
 * be created in the Debugger and performs actions in the server
 * 
 * @author vajih
 *
 */
@Deprecated
public class AnalyzeExternalRequest extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7417512148361682413L;
	Boolean doVAC;
	Boolean doIFF;
	Boolean doIMPLY;
	Boolean doAND;
	String relationalPropModuleOriginal;
	String temporalPropModuleOriginal;
	String toBeAnalyzedCode;

	public AnalyzeExternalRequest(Boolean doVAC, Boolean doIFF, Boolean doIMPLY,
			Boolean doAND, String relationalPropModuleOriginal,
			String temporalPropModuleOriginal, String toBeAnalyzedCode) {
		super();
		this.doVAC = doVAC;
		this.doIFF = doIFF;
		this.doIMPLY = doIMPLY;
		this.doAND = doAND;
		this.relationalPropModuleOriginal = relationalPropModuleOriginal;
		this.temporalPropModuleOriginal = temporalPropModuleOriginal;
		this.toBeAnalyzedCode = toBeAnalyzedCode;
	}

	@Override
	public String toString() {
		return "AnalyzeAlloy [doVAC=" + doVAC + ", doIFF=" + doIFF + ", doIMPLY="
				+ doIMPLY + ", doAND=" + doAND + ", relationalPropModuleOriginal="
				+ relationalPropModuleOriginal + ", temporalPropModuleOriginal="
				+ temporalPropModuleOriginal + ", toBeAnalyzedCode=" + toBeAnalyzedCode
				+ "]";
	}

	public void doAnalyze(
			final GeneratedStorage<AlloyProcessingParam> generatedStorage) {

		try {
			ExpressionPropertyGenerator.Builder.getInstance().initiateAndCreate(
					generatedStorage, new File(toBeAnalyzedCode),
					new File(relationalPropModuleOriginal),
					new File(temporalPropModuleOriginal), doVAC, doIFF, doIMPLY, doAND).startThread();
			
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
