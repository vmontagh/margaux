package edu.uw.ece.alloy.debugger.onborder;

import java.util.ArrayList;
import java.util.List;

public class SigFieldWrapper {

	private String sig;
	private List<FieldInfo> fields;

	public SigFieldWrapper() {
		this.fields = new ArrayList<>();
	}
	
	public SigFieldWrapper(String sig) {
		this();
		this.sig = sig;
	}
	
	public SigFieldWrapper(String sig, List<FieldInfo> fields) {
		this(sig);
		if(fields != null) this.fields = fields;
	}
	
	public String getSig() {
		return sig;
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
		private String[] typeComponents;
		
		public FieldInfo(String label, String type, String[] typeComponents) {
			this.label = label;
			this.type = type;
			this.typeComponents = typeComponents;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getType() {
			return type;
		}

		public String[] getTypeComponents() {
			return typeComponents;
		}
		
	}
	
}
