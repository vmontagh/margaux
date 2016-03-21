package edu.uw.ece.alloy.debugger.onborder;

import java.util.ArrayList;
import java.util.List;

public class SigFieldWrapper {

	private String sig;
	private String paramDecl;
	private List<FieldInfo> fields;

	public SigFieldWrapper() {
		this.fields = new ArrayList<>();
	}
	
	public SigFieldWrapper(String sig) {
		this();
		this.sig = sig;
	}
	
	public SigFieldWrapper(String sig, String paramDecl, List<FieldInfo> fields) {
		this(sig);
		this.paramDecl = paramDecl;
		if(fields != null) this.fields = fields;
	}
	
	public String getSig() {
		return sig;
	}

	public String getParamDecl() {
		return this.paramDecl;
	}
	
	public List<FieldInfo> getFields() {
		return fields;
	}
	
	public void addField(FieldInfo info) {
		this.fields.add(info);
	}
	
	public class FieldInfo {
		
		private String label;
		private String type;
		private String paramDecl;
		private String[] typeComponents;
		
		public FieldInfo(String label, String type, String paramDecl, String[] typeComponents) {
			this.label = label;
			this.type = type;
			this.paramDecl = paramDecl;
			this.typeComponents = typeComponents;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getType() {
			return type;
		}

		public String getParamDecl() {
			return this.paramDecl;
		}
		
		public String[] getTypeComponents() {
			return typeComponents;
		}
		
	}
	
}
