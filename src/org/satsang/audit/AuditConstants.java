package org.satsang.audit;

public class AuditConstants {

	//play audit constants
	public static final String BH_START = "BHStart";
	public static final String BH_SEEK = "BHSeek";
	public static final String BH_END = "BHEnd";
	public static final String PV_START="PVStart";
	public static final String PV_SEEK="PVSeek";
	public static final String PV_END="PVEnd";
	public static final String NW_START="NWStart";
	public static final String NW_SEEK="NWSeek";
	public static final String NW_END="NWEnd";
	public static final String MN_START="MNStart";
	public static final String MN_SEEK="MNSeek";
	public static final String MN_END="MNEnd";
	
	//download audit constants
	public static final String DOWNLOAD_START = "DWStart";
	public static final String DOWNLOAD_END = "DWEnd";
	
	//NOT IN USE
	public static final String TC_OFF = "TCOFF";
	public static final String MAINT_START = "MaintStart";
	public static final String MAINT_END = "MaintEnd";
	
	//audit type constants
	public static final String AUDIT_TYPE_AUDIT="audit";
	public static final String AUDIT_TYPE_ERROR="error";
}
